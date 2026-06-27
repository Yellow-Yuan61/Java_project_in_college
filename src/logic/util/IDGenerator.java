package logic.util;

import java.util.UUID;

/**
 * ID生成工具类，使用UUID生成唯一标识符。
 *
 * @author Yuan
 * @version 1.0
 */
public class IDGenerator {

    /**
     * 生成用户ID，格式 "U_" + 8位UUID缩写
     * @return 用户ID字符串
     */
    public static String generateUserId() {
        return "U_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 生成彩票ID，格式 "T_" + 8位UUID缩写
     * @return 彩票ID字符串
     */
    public static String generateTicketId() {
        return "T_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 生成开奖期号ID，格式 "D_" + 8位UUID缩写
     * @return 开奖ID字符串
     */
    public static String generateDrawId() {
        return "D_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
