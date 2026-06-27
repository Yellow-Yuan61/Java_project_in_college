package logic.util;

import java.util.regex.Pattern;

/**
 * 输入校验工具类，使用正则表达式对用户名、密码、电话号码进行格式验证。
 *
 * @author Yuan
 * @version 1.0
 */
public class Validator {

    /** 用户名正则：字母开头，3-15位，字母数字下划线 */
    private static final String USERNAME_REGEX = "^[a-zA-Z][a-zA-Z0-9_]{2,15}$";
    private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);

    /** 密码正则：6-20位，字母数字及部分特殊字符 */
    private static final String PASSWORD_REGEX = "^[a-zA-Z0-9!@#$%^&*]{6,20}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    /** 电话号码正则：中国大陆手机号 */
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    /** 账户金额上限 */
    private static final double MAX_BALANCE = 99999999.99;
    /** 账户金额下限 */
    private static final double MIN_BALANCE = 0.0;

    /**
     * 校验用户名格式
     * @param username 待校验用户名
     * @return 合法返回true，否则返回false
     */
    public static boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * 校验密码格式
     * @param password 待校验密码
     * @return 合法返回true，否则返回false
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * 校验电话号码格式
     * @param phone 待校验电话号码
     * @return 合法返回true，否则返回false
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 校验账户金额是否在有效范围内
     * @param balance 待校验金额
     * @return 合法返回true，否则返回false
     */
    public static boolean isValidBalance(double balance) {
        return balance >= MIN_BALANCE && balance <= MAX_BALANCE;
    }
}
