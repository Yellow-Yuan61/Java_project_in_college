package logic.storage;

import logic.model.DrawResult;
import logic.model.Ticket;
import logic.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据存储类（单例模式），负责所有数据的文件持久化。
 * 使用自定义格式文件存储用户、彩票和开奖结果数据。
 *
 * @author Yuan
 * @version 1.0
 */
public class DataStore {

    /** 单例实例 */
    private static DataStore instance;

    /** 数据目录路径 */
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.json";
    private static final String TICKETS_FILE = DATA_DIR + "/tickets.json";
    private static final String DRAWS_FILE = DATA_DIR + "/draws.json";
    private static final String COUNTERS_FILE = DATA_DIR + "/counters.json";

    /** 内存中的数据缓存 */
    private ConcurrentHashMap<String, User> userMap;
    private ConcurrentHashMap<String, Ticket> ticketMap;
    private ConcurrentHashMap<String, DrawResult> drawMap;

    /** 自增计数器（用于AutoTest批量生成ID） */
    private long userCounter;
    private long ticketCounter;
    private long drawCounter;

    private DataStore() {
        userMap = new ConcurrentHashMap<>();
        ticketMap = new ConcurrentHashMap<>();
        drawMap = new ConcurrentHashMap<>();
        userCounter = 0;
        ticketCounter = 0;
        drawCounter = 0;
        ensureDataDir();
        loadAllData();
    }

    /**
     * 获取DataStore单例实例
     * @return DataStore实例
     */
    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    /** 确保数据目录存在 */
    private void ensureDataDir() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // ==================== 计数器操作 ====================

    /**
     * 获取并递增用户计数器
     * @return 下一个用户编号
     */
    public synchronized long nextUserCounter() {
        userCounter++;
        saveCounters();
        return userCounter;
    }

    /**
     * 获取并递增彩票计数器
     * @return 下一个彩票编号
     */
    public synchronized long nextTicketCounter() {
        ticketCounter++;
        saveCounters();
        return ticketCounter;
    }

    // ==================== 用户操作 ====================

    /**
     * 保存用户数据
     * @param user 用户对象
     */
    public void saveUser(User user) {
        userMap.put(user.getUserId(), user);
        saveAllUsers();
    }

    /**
     * 根据用户ID查找用户
     * @param userId 用户ID
     * @return 用户对象，不存在则返回null
     */
    public User findUserById(String userId) {
        return userMap.get(userId);
    }

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象，不存在则返回null
     */
    public User findUserByUsername(String username) {
        return userMap.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取所有用户列表
     * @return 用户列表
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(userMap.values());
    }

    /**
     * 批量保存用户（用于AutoTest）
     * @param users 用户列表
     */
    public void saveUsersBatch(List<User> users) {
        for (User user : users) {
            userMap.put(user.getUserId(), user);
        }
        saveAllUsers();
    }

    // ==================== 彩票操作 ====================

    /**
     * 保存彩票数据
     * @param ticket 彩票对象
     */
    public void saveTicket(Ticket ticket) {
        ticketMap.put(ticket.getTicketId(), ticket);
        saveAllTickets();
    }

    /**
     * 根据用户ID查找该用户的所有彩票
     * @param userId 用户ID
     * @return 彩票列表
     */
    public List<Ticket> findTicketsByUserId(String userId) {
        return ticketMap.values().stream()
                .filter(t -> t.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * 根据开奖期号查找彩票
     * @param drawId 开奖期号
     * @return 彩票列表
     */
    public List<Ticket> findTicketsByDrawId(String drawId) {
        return ticketMap.values().stream()
                .filter(t -> t.getDrawId().equals(drawId))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有彩票
     * @return 彩票列表
     */
    public List<Ticket> getAllTickets() {
        return new ArrayList<>(ticketMap.values());
    }

    /**
     * 将所有内存中的彩票数据刷写到文件
     */
    public void flushAllTicketChanges() {
        saveAllTickets();
    }

    /**
     * 批量保存彩票（用于AutoTest）
     * @param tickets 彩票列表
     */
    public void saveTicketsBatch(List<Ticket> tickets) {
        for (Ticket ticket : tickets) {
            ticketMap.put(ticket.getTicketId(), ticket);
        }
        saveAllTickets();
    }

    // ==================== 开奖结果操作 ====================

    /**
     * 保存开奖结果
     * @param drawResult 开奖结果
     */
    public void saveDrawResult(DrawResult drawResult) {
        drawMap.put(drawResult.getDrawId(), drawResult);
        saveAllDraws();
    }

    /**
     * 根据期号查找开奖结果
     * @param drawId 开奖期号
     * @return 开奖结果
     */
    public DrawResult findDrawResultById(String drawId) {
        return drawMap.get(drawId);
    }

    /**
     * 获取所有开奖结果
     * @return 开奖结果列表
     */
    public List<DrawResult> getAllDrawResults() {
        return new ArrayList<>(drawMap.values());
    }

    // ==================== 文件读写 ====================

    /** 加载所有数据 */
    private void loadAllData() {
        loadCounters();
        loadUsers();
        loadTickets();
        loadDraws();
    }

    // --- 计数器 ---
    private void loadCounters() {
        File file = new File(COUNTERS_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    userCounter = Long.parseLong(parts[0]);
                    ticketCounter = Long.parseLong(parts[1]);
                    drawCounter = Long.parseLong(parts[2]);
                }
            }
        } catch (Exception e) {
            System.err.println("[DataStore] 加载计数器失败: " + e.getMessage());
        }
    }

    void saveCounters() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(COUNTERS_FILE), "UTF-8"))) {
            writer.println(userCounter + "," + ticketCounter + "," + drawCounter);
        } catch (Exception e) {
            System.err.println("[DataStore] 保存计数器失败: " + e.getMessage());
        }
    }

    // --- 用户 ---
    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                User user = User.deserialize(line);
                if (user != null) {
                    userMap.put(user.getUserId(), user);
                }
            }
        } catch (Exception e) {
            System.err.println("[DataStore] 加载用户数据失败: " + e.getMessage());
        }
    }

    private void saveAllUsers() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(USERS_FILE), "UTF-8"))) {
            for (User user : userMap.values()) {
                writer.println(user.serialize());
            }
        } catch (Exception e) {
            System.err.println("[DataStore] 保存用户数据失败: " + e.getMessage());
        }
    }

    // --- 彩票 ---
    private void loadTickets() {
        File file = new File(TICKETS_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Ticket ticket = Ticket.deserialize(line);
                if (ticket != null) {
                    ticketMap.put(ticket.getTicketId(), ticket);
                }
            }
        } catch (Exception e) {
            System.err.println("[DataStore] 加载彩票数据失败: " + e.getMessage());
        }
    }

    private void saveAllTickets() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(TICKETS_FILE), "UTF-8"))) {
            for (Ticket ticket : ticketMap.values()) {
                writer.println(ticket.serialize());
            }
        } catch (Exception e) {
            System.err.println("[DataStore] 保存彩票数据失败: " + e.getMessage());
        }
    }

    // --- 开奖 ---
    private void loadDraws() {
        File file = new File(DRAWS_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                DrawResult drawResult = DrawResult.deserialize(line);
                if (drawResult != null) {
                    drawMap.put(drawResult.getDrawId(), drawResult);
                }
            }
        } catch (Exception e) {
            System.err.println("[DataStore] 加载开奖数据失败: " + e.getMessage());
        }
    }

    private void saveAllDraws() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(DRAWS_FILE), "UTF-8"))) {
            for (DrawResult drawResult : drawMap.values()) {
                writer.println(drawResult.serialize());
            }
        } catch (Exception e) {
            System.err.println("[DataStore] 保存开奖数据失败: " + e.getMessage());
        }
    }
}
