package test;

import logic.model.DrawResult;
import logic.model.Ticket;
import logic.model.User;
import logic.service.DrawServer;
import logic.service.LotteryService;
import logic.service.UserService;
import logic.storage.DataStore;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 自动化测试程序：批量注册10万用户、自动购票、模拟抽奖并统计中奖分布。
 * 用于验证彩票系统的正确性。
 *
 * <p>使用方法：</p>
 * <pre>java -cp classes test.AutoTest</pre>
 *
 * <p>测试流程：</p>
 * <ol>
 *   <li>多线程批量注册10万用户</li>
 *   <li>为每位用户自动购买1-3张随机彩票</li>
 *   <li>启动开奖服务器并进行抽奖</li>
 *   <li>统计中奖分布，验证概率正确性</li>
 * </ol>
 *
 * @author Yuan
 * @version 1.0
 */
public class AutoTest {

    /** 自动注册用户数 */
    private static final int USER_COUNT = 100_000;

    /** 每用户购票张数范围 */
    private static final int MIN_TICKETS_PER_USER = 1;
    private static final int MAX_TICKETS_PER_USER = 3;

    /** 线程池大小 */
    private static final int THREAD_POOL_SIZE = 20;

    /** 批量保存阈值 */
    private static final int BATCH_SIZE = 1000;

    private final DataStore dataStore;
    private final LotteryService lotteryService;
    private final UserService userService;

    /** 统计信息 */
    private final AtomicInteger userCount;
    private final AtomicLong ticketCount;
    private final AtomicLong totalTime;

    public AutoTest() {
        this.dataStore = DataStore.getInstance();
        this.lotteryService = new LotteryService();
        this.userService = new UserService();
        this.userCount = new AtomicInteger(0);
        this.ticketCount = new AtomicLong(0);
        this.totalTime = new AtomicLong(0);
    }

    public static void main(String[] args) {
        AutoTest test = new AutoTest();
        test.run();
    }

    /**
     * 运行完整测试流程
     */
    public void run() {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   福彩36选7 自动测试程序                  ║");
        System.out.println("║   注册" + USER_COUNT + "用户 + 自动购票 + 模拟抽奖      ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();

        long startTime = System.currentTimeMillis();

        // Step 1: 启动开奖服务器
        startDrawServer();

        // Step 2: 批量注册用户
        registerUsers();

        // Step 3: 批量购票
        buyTickets();

        // Step 4: 模拟抽奖并统计
        doDrawAndStatistics();

        long endTime = System.currentTimeMillis();
        System.out.println();
        System.out.println("══════════════════════════════════════════");
        System.out.println("  测试完成！总耗时：" + formatTime(endTime - startTime));
        System.out.println("══════════════════════════════════════════");
    }

    /** 启动开奖服务器 */
    private void startDrawServer() {
        System.out.print("[1/3] 启动开奖服务器... ");
        DrawServer drawServer = DrawServer.getInstance();
        if (!drawServer.isRunning()) {
            drawServer.start();
        }
        // 等待服务器就绪
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        System.out.println("OK (端口 " + DrawServer.DEFAULT_PORT + ")");
    }

    /** 多线程批量注册用户 */
    private void registerUsers() {
        System.out.println("[2/3] 批量注册 " + USER_COUNT + " 用户...");

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Future<List<User>>> futures = new ArrayList<>();

        int batchSize = USER_COUNT / THREAD_POOL_SIZE;
        for (int t = 0; t < THREAD_POOL_SIZE; t++) {
            final int threadId = t;
            final int start = threadId * batchSize;
            final int end = (threadId == THREAD_POOL_SIZE - 1) ? USER_COUNT : start + batchSize;

            futures.add(executor.submit(() -> {
                List<User> users = new ArrayList<>();
                Random rand = new Random();
                for (int i = start; i < end; i++) {
                    String username = String.format("test_%06d", i);
                    String password = "123456";
                    String phone = generatePhone(rand, i);
                    String userId = "U_AUTO" + String.format("%06d", i);
                    User user = new User(userId, username, password, 1000.0, phone);
                    users.add(user);
                }
                return users;
            }));
        }

        // 收集结果并批量保存
        int saved = 0;
        for (Future<List<User>> future : futures) {
            try {
                List<User> users = future.get();
                dataStore.saveUsersBatch(users);
                saved += users.size();
                System.out.print("\r  注册进度: " + saved + "/" + USER_COUNT
                        + " (" + String.format("%.1f", saved * 100.0 / USER_COUNT) + "%)");
            } catch (Exception e) {
                System.err.println("\n注册失败: " + e.getMessage());
            }
        }

        executor.shutdown();
        System.out.println("\n  用户注册完成！共 " + saved + " 人");
    }

    /** 多线程批量购票 */
    private void buyTickets() {
        System.out.print("  开始自动购票... ");
        long start = System.currentTimeMillis();

        List<User> allUsers = dataStore.getAllUsers();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Future<Integer>> futures = new ArrayList<>();

        int usersPerThread = allUsers.size() / THREAD_POOL_SIZE;
        for (int t = 0; t < THREAD_POOL_SIZE; t++) {
            final int startIdx = t * usersPerThread;
            final int endIdx = (t == THREAD_POOL_SIZE - 1) ? allUsers.size() : startIdx + usersPerThread;
            final List<User> subList = allUsers.subList(startIdx, endIdx);

            futures.add(executor.submit(() -> {
                List<Ticket> tickets = new ArrayList<>();
                Random rand = new Random();
                for (User user : subList) {
                    int ticketCount = rand.nextInt(MAX_TICKETS_PER_USER - MIN_TICKETS_PER_USER + 1)
                            + MIN_TICKETS_PER_USER;
                    for (int i = 0; i < ticketCount; i++) {
                        int betCount = rand.nextInt(5) + 1;
                        int[] numbers = LotteryService.generateRandomNumbers();
                        String ticketId = "T_AUTO" + String.format("%08d", rand.nextInt(99999999));
                        Ticket ticket = new Ticket(ticketId, user.getUserId(), null, numbers, betCount);
                        // 扣款
                        double cost = LotteryService.PRICE_PER_BET * betCount;
                        user.setBalance(user.getBalance() - cost);
                        tickets.add(ticket);

                        // 分批保存
                        if (tickets.size() >= BATCH_SIZE) {
                            dataStore.saveTicketsBatch(new ArrayList<>(tickets));
                            tickets.clear();
                        }
                    }
                }
                if (!tickets.isEmpty()) {
                    dataStore.saveTicketsBatch(tickets);
                }
                return subList.size();
            }));
        }

        int totalProcessed = 0;
        for (Future<Integer> future : futures) {
            try {
                totalProcessed += future.get();
            } catch (Exception e) {
                System.err.println("购票失败: " + e.getMessage());
            }
        }

        executor.shutdown();
        long end = System.currentTimeMillis();
        long totalTickets = dataStore.getAllTickets().size();
        System.out.println("完成！共购买 " + totalTickets + " 张彩票，"
                + "耗时 " + formatTime(end - start));
    }

    /** 模拟抽奖并统计 */
    private void doDrawAndStatistics() {
        System.out.println("[3/3] 模拟抽奖并统计结果...");
        System.out.println();

        // 生成开奖号码
        int[] winningNumbers = LotteryService.generateRandomNumbers();

        System.out.println("  开奖号码：");
        System.out.print("  ");
        for (int num : winningNumbers) {
            System.out.print(" [" + String.format("%02d", num) + "] ");
        }
        System.out.println();
        System.out.println();

        // 执行开奖
        DrawResult drawResult = lotteryService.startNewDraw();
        drawResult.setWinningNumbers(winningNumbers);
        List<Ticket> winners = lotteryService.doPrizeDraw(drawResult);

        // 统计
        Map<String, Object> stats = lotteryService.getDrawStatistics(drawResult.getDrawId());
        List<Ticket> allTickets = dataStore.getAllTickets();

        System.out.println("  ═════════ 统计结果 ═════════");
        System.out.println("  总彩票数：    " + stats.get("totalTickets") + " 张");
        System.out.println("  特等奖(7中7)：" + stats.get("specialPrizeCount") + " 注");
        System.out.println("  一等奖(7中6)：" + stats.get("firstPrizeCount") + " 注");
        System.out.println("  总中奖注数：  " + winners.size() + " 注");
        System.out.println("  中奖率：      " + String.format("%.4f%%",
                winners.size() * 100.0 / allTickets.size()));
        System.out.println("  派奖总金额：  ¥" + String.format("%.2f", stats.get("totalPayout")));
        System.out.println();

        // 理论概率对比
        System.out.println("  ═════════ 概率验证 ═════════");
        // 36选7: C(36,7) = 8347680
        long totalCombinations = combination(36, 7);
        System.out.println("  36选7总组合数：" + totalCombinations);
        System.out.println("  特等奖理论概率：1 / " + totalCombinations);
        double actualSpecialRate = (long) stats.get("specialPrizeCount") * 1.0 / allTickets.size();
        System.out.println("  特等奖实际概率：" + String.format("%.8f", actualSpecialRate));

        // 中6个的概率：C(7,6) * C(29,1) / C(36,7) = 7 * 29 / 8347680
        double win6Theory = (7.0 * 29.0) / totalCombinations;
        double actualWin6Rate = (long) stats.get("firstPrizeCount") * 1.0 / allTickets.size();
        System.out.println("  一等奖理论概率：" + String.format("%.8f", win6Theory));
        System.out.println("  一等奖实际概率：" + String.format("%.8f", actualWin6Rate));
    }

    /** 生成唯一手机号 */
    private String generatePhone(Random rand, int index) {
        String[] prefixes = {"138", "139", "150", "151", "152", "158", "159", "186", "187", "188"};
        String prefix = prefixes[rand.nextInt(prefixes.length)];
        return prefix + String.format("%08d", index);
    }

    /** 计算组合数 C(n, k) */
    private long combination(int n, int k) {
        if (k > n) return 0;
        if (k == 0 || k == n) return 1;
        k = Math.min(k, n - k);
        long result = 1;
        for (int i = 0; i < k; i++) {
            result = result * (n - i) / (i + 1);
        }
        return result;
    }

    /** 格式化时间 */
    private String formatTime(long millis) {
        if (millis < 1000) return millis + "ms";
        if (millis < 60_000) return String.format("%.2fs", millis / 1000.0);
        long minutes = millis / 60_000;
        long seconds = (millis % 60_000) / 1000;
        return minutes + "m " + seconds + "s";
    }
}
