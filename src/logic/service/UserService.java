package logic.service;

import logic.model.User;
import logic.storage.DataStore;
import logic.util.IDGenerator;
import logic.util.Validator;

/**
 * 用户服务类，处理用户注册、登录、账户管理等业务逻辑。
 * 采用MVC架构中的Controller角色，协调Model和Storage之间的交互。
 *
 * @author Yuan
 * @version 1.0
 */
public class UserService {

    /** 新用户默认账户金额 */
    private static final double DEFAULT_BALANCE = 1000.0;

    private final DataStore dataStore;

    public UserService() {
        this.dataStore = DataStore.getInstance();
    }

    /**
     * 用户注册
     * @param username 用户名（字母开头，3-16位）
     * @param password 密码（6-20位）
     * @param phone 电话号码（中国大陆手机号格式）
     * @return 注册成功返回User对象，失败返回null
     */
    public User register(String username, String password, String phone) {
        if (!Validator.isValidUsername(username)) {
            throw new IllegalArgumentException("用户名格式非法：需字母开头，3-16位，字母数字下划线");
        }
        if (!Validator.isValidPassword(password)) {
            throw new IllegalArgumentException("密码格式非法：需6-20位，字母数字及!@#$%^&*");
        }
        if (!Validator.isValidPhone(phone)) {
            throw new IllegalArgumentException("电话号码格式非法：需中国大陆手机号格式");
        }

        User existing = dataStore.findUserByUsername(username);
        if (existing != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        String userId = IDGenerator.generateUserId();
        User user = new User(userId, username, password, DEFAULT_BALANCE, phone);
        dataStore.saveUser(user);
        return user;
    }

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回User对象，失败返回null
     */
    public User login(String username, String password) {
        User user = dataStore.findUserByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        return user;
    }

    /**
     * 根据ID获取用户信息
     * @param userId 用户ID
     * @return 用户对象
     */
    public User getUserById(String userId) {
        return dataStore.findUserById(userId);
    }

    /**
     * 更新用户账户余额
     * @param userId 用户ID
     * @param amount 变动金额（正数为充值，负数为扣款）
     * @return 更新后的余额
     */
    public double updateBalance(String userId, double amount) {
        User user = dataStore.findUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        double newBalance = user.getBalance() + amount;
        if (newBalance < 0) {
            throw new IllegalArgumentException("余额不足");
        }
        user.setBalance(newBalance);
        dataStore.saveUser(user);
        return newBalance;
    }

    /**
     * 检查用户是否有未读中奖通知
     * @param userId 用户ID
     * @return 有通知返回通知内容，否则返回null
     */
    public String checkNotification(String userId) {
        User user = dataStore.findUserById(userId);
        if (user != null && user.hasUnreadNotification()) {
            String msg = user.getNotificationMessage();
            // 清除通知标记
            user.setHasUnreadNotification(false);
            user.setNotificationMessage("");
            dataStore.saveUser(user);
            return msg;
        }
        return null;
    }

    /**
     * 设置用户中奖通知
     * @param userId 用户ID
     * @param message 通知内容
     */
    public void setNotification(String userId, String message) {
        User user = dataStore.findUserById(userId);
        if (user != null) {
            user.setHasUnreadNotification(true);
            user.setNotificationMessage(message);
            dataStore.saveUser(user);
        }
    }

    /**
     * 获取系统用户总数
     * @return 用户数量
     */
    public int getUserCount() {
        return dataStore.getAllUsers().size();
    }
}
