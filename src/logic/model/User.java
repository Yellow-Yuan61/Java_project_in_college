package logic.model;

import logic.storage.DataStore;

import java.io.Serializable;

/**
 * 用户实体类，包含用户id、用户名、密码、账户金额、电话号码等属性。
 * 实现序列化接口，支持文件存储。
 *
 * @author Yuan
 * @version 1.0
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户唯一标识 */
    private String userId;

    /** 用户名 */
    private String username;

    /** 密码（实际项目中应加密存储） */
    private String password;

    /** 账户金额 */
    private double balance;

    /** 电话号码 */
    private String phone;

    /** 是否有未读中奖通知 */
    private boolean hasUnreadNotification;

    /** 中奖通知内容 */
    private String notificationMessage;

    // ==================== 构造方法 ====================

    public User() {}

    public User(String userId, String username, String password, double balance, String phone) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.phone = phone;
        this.hasUnreadNotification = false;
        this.notificationMessage = "";
    }

    // ==================== Getter / Setter ====================

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean hasUnreadNotification() {
        return hasUnreadNotification;
    }

    public void setHasUnreadNotification(boolean hasUnreadNotification) {
        this.hasUnreadNotification = hasUnreadNotification;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    // ==================== 序列化 ====================

    /**
     * 将用户对象序列化为字符串，格式: userId|username|password|balance|phone|hasUnread|message
     * @return 序列化字符串
     */
    public String serialize() {
        return userId + "|" + username + "|" + password + "|"
                + balance + "|" + phone + "|"
                + (hasUnreadNotification ? "1" : "0") + "|"
                + (notificationMessage == null ? "" : notificationMessage.replace("|", " "));
    }

    /**
     * 从字符串反序列化为用户对象
     * @param line 序列化字符串
     * @return 用户对象，解析失败返回null
     */
    public static User deserialize(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length < 7) return null;
            User user = new User();
            user.userId = parts[0];
            user.username = parts[1];
            user.password = parts[2];
            user.balance = Double.parseDouble(parts[3]);
            user.phone = parts[4];
            user.hasUnreadNotification = "1".equals(parts[5]);
            user.notificationMessage = parts.length > 6 ? parts[6] : "";
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", balance=" + balance +
                ", phone='" + phone + '\'' +
                '}';
    }
}
