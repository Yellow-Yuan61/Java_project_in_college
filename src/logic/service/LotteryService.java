package logic.service;

import logic.model.DrawResult;
import logic.model.Ticket;
import logic.model.User;
import logic.storage.DataStore;
import logic.util.IDGenerator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 彩票服务类，处理购票、抽奖、兑奖等核心业务逻辑。
 * 实现观察者模式：抽奖完成后通知所有中奖用户。
 *
 * @author Yuan
 * @version 1.0
 */
public class LotteryService {

    /** 彩票选号范围：1-36 */
    public static final int NUMBER_POOL_SIZE = 36;

    /** 每张彩票选号数量 */
    public static final int NUMBERS_PER_TICKET = 7;

    /** 每注价格 */
    public static final double PRICE_PER_BET = 2.0;

    /** 特等奖奖金基数（7个全中） */
    public static final double SPECIAL_PRIZE_BASE = 5000000.0;

    /** 一等奖奖金基数（中6个） */
    public static final double FIRST_PRIZE_BASE = 50000.0;

    private final DataStore dataStore;
    private final UserService userService;

    /** 当期开奖ID（开奖前为null） */
    private String currentDrawId;

    /** 观察者回调：中奖通知 */
    private final List<DrawObserver> observers;

    /**
     * 开奖观察者接口（观察者模式）
     */
    public interface DrawObserver {
        /**
         * 开奖完成时的回调
         * @param drawResult 开奖结果
         * @param winners 中奖彩票列表
         */
        void onDrawComplete(DrawResult drawResult, List<Ticket> winners);
    }

    public LotteryService() {
        this.dataStore = DataStore.getInstance();
        this.userService = new UserService();
        this.observers = new ArrayList<>();
    }

    /**
     * 注册开奖观察者
     * @param observer 观察者
     */
    public void addObserver(DrawObserver observer) {
        observers.add(observer);
    }

    /**
     * 移除观察者
     * @param observer 观察者
     */
    public void removeObserver(DrawObserver observer) {
        observers.remove(observer);
    }

    // ==================== 购票 ====================

    /**
     * 手动选号购买彩票
     * @param userId 购买用户ID
     * @param numbers 用户选择的7个号码（1-36）
     * @param betCount 投注倍数
     * @return 购买的彩票对象
     * @throws IllegalArgumentException 参数非法时抛出
     */
    public Ticket buyTicket(String userId, int[] numbers, int betCount) {
        validateNumbers(numbers);
        validateBetCount(betCount);

        double cost = PRICE_PER_BET * betCount;
        userService.updateBalance(userId, -cost);

        String ticketId = IDGenerator.generateTicketId();
        Ticket ticket = new Ticket(ticketId, userId, null, numbers, betCount);
        dataStore.saveTicket(ticket);
        return ticket;
    }

    /**
     * 随机选号购买彩票
     * @param userId 购买用户ID
     * @param betCount 投注倍数
     * @return 购买的彩票对象
     */
    public Ticket buyRandomTicket(String userId, int betCount) {
        validateBetCount(betCount);

        double cost = PRICE_PER_BET * betCount;
        userService.updateBalance(userId, -cost);

        int[] numbers = generateRandomNumbers();
        String ticketId = IDGenerator.generateTicketId();
        Ticket ticket = new Ticket(ticketId, userId, null, numbers, betCount);
        dataStore.saveTicket(ticket);
        return ticket;
    }

    /**
     * 批量自动购票（用于AutoTest）
     * @param userId 用户ID
     * @param betCount 投注倍数
     * @param count 购买张数
     * @return 购买的彩票列表
     */
    public List<Ticket> buyRandomTickets(String userId, int betCount, int count) {
        List<Ticket> tickets = new ArrayList<>();
        double totalCost = PRICE_PER_BET * betCount * count;
        userService.updateBalance(userId, -totalCost);

        for (int i = 0; i < count; i++) {
            int[] numbers = generateRandomNumbers();
            String ticketId = IDGenerator.generateTicketId();
            Ticket ticket = new Ticket(ticketId, userId, null, numbers, betCount);
            tickets.add(ticket);
        }
        dataStore.saveTicketsBatch(tickets);
        return tickets;
    }

    // ==================== 开奖 ====================

    /**
     * 开始新一期抽奖，生成开奖号码
     * @return 开奖结果
     */
    public DrawResult startNewDraw() {
        String drawId = IDGenerator.generateDrawId();
        currentDrawId = drawId;
        int[] winningNumbers = generateRandomNumbers();
        long drawTime = System.currentTimeMillis();
        DrawResult drawResult = new DrawResult(drawId, winningNumbers, drawTime);
        dataStore.saveDrawResult(drawResult);
        return drawResult;
    }

    /**
     * 兑奖：对本期的所有彩票进行中奖检查
     * 使用Stream API统计中奖信息。
     * @param drawResult 开奖结果
     * @return 中奖彩票列表
     */
    public List<Ticket> doPrizeDraw(DrawResult drawResult) {
        int[] winningNumbers = drawResult.getWinningNumbers();
        List<Ticket> allTickets = dataStore.getAllTickets();

        // 筛选出属于当前期的彩票
        List<Ticket> currentDrawTickets = allTickets.stream()
                .filter(t -> t.getDrawId() == null || t.getDrawId().isEmpty())
                .collect(Collectors.toList());

        // 对每张彩票进行中奖检查
        List<Ticket> winners = currentDrawTickets.stream()
                .map(ticket -> checkTicketWin(ticket, winningNumbers))
                .filter(t -> t != null && t.isWinner())
                .collect(Collectors.toList());

        // 将本期彩票关联到开奖期号（只更新内存，最后统一刷盘）
        for (Ticket ticket : currentDrawTickets) {
            ticket.setDrawId(drawResult.getDrawId());
        }
        dataStore.flushAllTicketChanges();

        // 通知中奖用户
        for (Ticket winner : winners) {
            String prizeName = winner.getPrizeLevel() == 2 ? "特等奖" : "一等奖";
            String msg = String.format(
                    "恭喜！您在期号 %s 中获得%s！\n中奖号码：%s\n匹配号码数：%d\n奖金：%.2f 元",
                    drawResult.getDrawId(), prizeName,
                    drawResult.getWinningNumbersString(),
                    winner.getMatchedCount(),
                    winner.getPrizeAmount()
            );
            userService.setNotification(winner.getUserId(), msg);
        }

        // 通知所有注册的观察者
        for (DrawObserver observer : observers) {
            observer.onDrawComplete(drawResult, winners);
        }

        return winners;
    }

    /**
     * 检查单张彩票是否中奖
     * @param ticket 彩票
     * @param winningNumbers 中奖号码
     * @return 更新后的彩票对象（包含中奖信息），未中奖返回原彩票
     */
    private Ticket checkTicketWin(Ticket ticket, int[] winningNumbers) {
        int[] ticketNumbers = ticket.getNumbers();
        Set<Integer> winningSet = new HashSet<>();
        for (int num : winningNumbers) {
            winningSet.add(num);
        }

        int matchedCount = 0;
        for (int num : ticketNumbers) {
            if (winningSet.contains(num)) {
                matchedCount++;
            }
        }

        ticket.setMatchedCount(matchedCount);

        if (matchedCount == 7) {
            // 特等奖
            ticket.setWinner(true);
            ticket.setPrizeLevel(2);
            ticket.setPrizeAmount(SPECIAL_PRIZE_BASE * ticket.getBetCount());
        } else if (matchedCount == 6) {
            // 一等奖
            ticket.setWinner(true);
            ticket.setPrizeLevel(1);
            ticket.setPrizeAmount(FIRST_PRIZE_BASE * ticket.getBetCount());
        } else {
            ticket.setWinner(false);
            ticket.setPrizeLevel(0);
            ticket.setPrizeAmount(0);
        }

        return ticket;
    }

    // ==================== 中奖统计 ====================

    /**
     * 获取某期开奖的中奖统计信息
     * @param drawId 开奖期号
     * @return 统计信息Map
     */
    public Map<String, Object> getDrawStatistics(String drawId) {
        List<Ticket> drawTickets = dataStore.findTicketsByDrawId(drawId);
        Map<String, Object> stats = new HashMap<>();

        long totalTickets = drawTickets.size();
        long specialPrizeCount = drawTickets.stream().filter(t -> t.getPrizeLevel() == 2).count();
        long firstPrizeCount = drawTickets.stream().filter(t -> t.getPrizeLevel() == 1).count();
        double totalPayout = drawTickets.stream()
                .filter(Ticket::isWinner)
                .mapToDouble(Ticket::getPrizeAmount)
                .sum();

        // 获取中奖用户ID列表
        List<String> winnerUserIds = drawTickets.stream()
                .filter(Ticket::isWinner)
                .map(Ticket::getUserId)
                .distinct()
                .collect(Collectors.toList());

        stats.put("totalTickets", totalTickets);
        stats.put("specialPrizeCount", specialPrizeCount);
        stats.put("firstPrizeCount", firstPrizeCount);
        stats.put("totalPayout", totalPayout);
        stats.put("winnerUserIds", winnerUserIds);
        stats.put("drawId", drawId);

        return stats;
    }

    /**
     * 获取某个用户的中奖历史
     * @param userId 用户ID
     * @return 中奖彩票列表
     */
    public List<Ticket> getUserWinHistory(String userId) {
        return dataStore.findTicketsByUserId(userId).stream()
                .filter(Ticket::isWinner)
                .collect(Collectors.toList());
    }

    /**
     * 获取某个用户的所有购票记录
     * @param userId 用户ID
     * @return 彩票列表
     */
    public List<Ticket> getUserTicketHistory(String userId) {
        return dataStore.findTicketsByUserId(userId);
    }

    /**
     * 获取当前开奖期号
     * @return 当前开奖期号
     */
    public String getCurrentDrawId() {
        return currentDrawId;
    }

    /**
     * 设置当前开奖期号
     * @param drawId 开奖期号
     */
    public void setCurrentDrawId(String drawId) {
        this.currentDrawId = drawId;
    }

    /**
     * 获取所有开奖记录
     * @return 开奖结果列表
     */
    public List<DrawResult> getAllDrawResults() {
        return dataStore.getAllDrawResults();
    }

    /**
     * 获取指定期号的开奖结果
     * @param drawId 开奖期号
     * @return 开奖结果
     */
    public DrawResult getDrawResultById(String drawId) {
        return dataStore.findDrawResultById(drawId);
    }

    // ==================== 工具方法 ====================

    /**
     * 生成一组不重复的随机号码（1-36中选7个）
     * @return 排序后的号码数组
     */
    public static int[] generateRandomNumbers() {
        Random random = new Random();
        Set<Integer> numSet = new HashSet<>();
        while (numSet.size() < NUMBERS_PER_TICKET) {
            numSet.add(random.nextInt(NUMBER_POOL_SIZE) + 1);
        }
        int[] numbers = numSet.stream().mapToInt(Integer::intValue).toArray();
        Arrays.sort(numbers);
        return numbers;
    }

    /**
     * 生成一组随机号码用于动画滚动
     * @return 7个未排序的随机号码
     */
    public int[] generateRandomNumbersUnsorted() {
        Random random = new Random();
        Set<Integer> numSet = new HashSet<>();
        while (numSet.size() < NUMBERS_PER_TICKET) {
            numSet.add(random.nextInt(NUMBER_POOL_SIZE) + 1);
        }
        return numSet.stream().mapToInt(Integer::intValue).toArray();
    }

    /** 校验号码合法性 */
    private void validateNumbers(int[] numbers) {
        if (numbers == null || numbers.length != NUMBERS_PER_TICKET) {
            throw new IllegalArgumentException("必须选择7个号码");
        }
        Set<Integer> set = new HashSet<>();
        for (int num : numbers) {
            if (num < 1 || num > NUMBER_POOL_SIZE) {
                throw new IllegalArgumentException("号码必须在1-" + NUMBER_POOL_SIZE + "之间");
            }
            if (!set.add(num)) {
                throw new IllegalArgumentException("号码不能重复");
            }
        }
    }

    /** 校验投注倍数 */
    private void validateBetCount(int betCount) {
        if (betCount < 1 || betCount > 100) {
            throw new IllegalArgumentException("投注倍数必须在1-100之间");
        }
    }
}
