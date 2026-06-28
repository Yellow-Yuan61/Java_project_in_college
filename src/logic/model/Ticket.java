package logic.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 彩票实体类，记录用户购买的彩票信息。
 * 包含彩票ID、用户ID、所选号码、投注倍数、是否中奖、中奖等级等属性。
 *
 * @author Yuan
 * @version 1.0
 */
public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 彩票唯一标识 */
    private String ticketId;

    /** 购买用户ID */
    private String userId;

    /** 关联的开奖期号 */
    private String drawId;

    /** 用户选择的7个号码（1-36） */
    private int[] numbers;

    /** 投注倍数 */
    private int betCount;

    /** 是否已中奖 */
    private boolean isWinner;

    /** 中奖等级: 0=未中奖, 1=一等奖(中6), 2=特等奖(中7) */
    private int prizeLevel;

    /** 匹配的号码个数 */
    private int matchedCount;

    /** 中奖金额 */
    private double prizeAmount;

    /** 是否已通知用户 */
    private boolean isNotified;

    // ==================== 构造方法 ====================

    public Ticket() {}

    public Ticket(String ticketId, String userId, String drawId, int[] numbers, int betCount) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.drawId = drawId;
        this.numbers = Arrays.copyOf(numbers, numbers.length);
        this.betCount = betCount;
        this.isWinner = false;
        this.prizeLevel = 0;
        this.matchedCount = 0;
        this.prizeAmount = 0;
        this.isNotified = false;
    }

    // ==================== Getter / Setter ====================

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDrawId() {
        return drawId;
    }

    public void setDrawId(String drawId) {
        this.drawId = drawId;
    }

    public int[] getNumbers() {
        return numbers;
    }

    public void setNumbers(int[] numbers) {
        this.numbers = Arrays.copyOf(numbers, numbers.length);
    }

    public int getBetCount() {
        return betCount;
    }

    public void setBetCount(int betCount) {
        this.betCount = betCount;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public void setWinner(boolean winner) {
        isWinner = winner;
    }

    public int getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(int prizeLevel) {
        this.prizeLevel = prizeLevel;
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public void setMatchedCount(int matchedCount) {
        this.matchedCount = matchedCount;
    }

    public double getPrizeAmount() {
        return prizeAmount;
    }

    public void setPrizeAmount(double prizeAmount) {
        this.prizeAmount = prizeAmount;
    }

    public boolean isNotified() {
        return isNotified;
    }

    public void setNotified(boolean notified) {
        isNotified = notified;
    }

    /**
     * 获取号码的字符串表示
     * @return 用逗号分隔的号码字符串
     */
    public String getNumbersString() {
        if (numbers == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numbers.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format("%02d", numbers[i]));
        }
        return sb.toString();
    }

    // ==================== 序列化（JSON格式） ====================

    /**
     * 序列化为JSON字符串
     * @return JSON字符串
     */
    public String serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("ticketId", ticketId);
        map.put("userId", userId);
        map.put("drawId", drawId == null ? "" : drawId);
        map.put("numbers", numbers);
        map.put("betCount", betCount);
        map.put("isWinner", isWinner);
        map.put("prizeLevel", prizeLevel);
        map.put("matchedCount", matchedCount);
        map.put("prizeAmount", prizeAmount);
        map.put("isNotified", isNotified);
        return logic.util.JsonUtil.toJson(map);
    }

    /**
     * 从JSON字符串反序列化
     * @param line JSON字符串
     * @return 彩票对象
     */
    public static Ticket deserialize(String line) {
        try {
            Map<String, Object> map = logic.util.JsonUtil.parseObject(line);
            Ticket ticket = new Ticket();
            ticket.ticketId = str(map.get("ticketId"));
            ticket.userId = str(map.get("userId"));
            ticket.drawId = str(map.get("drawId"));
            Object numObj = map.get("numbers");
            if (numObj instanceof int[]) {
                ticket.numbers = (int[]) numObj;
            } else if (numObj instanceof String) {
                ticket.numbers = logic.util.JsonUtil.parseIntArray((String) numObj);
            }
            ticket.betCount = toInt(map.get("betCount"));
            ticket.isWinner = Boolean.TRUE.equals(map.get("isWinner"));
            ticket.prizeLevel = toInt(map.get("prizeLevel"));
            ticket.matchedCount = toInt(map.get("matchedCount"));
            ticket.prizeAmount = toDouble(map.get("prizeAmount"));
            ticket.isNotified = Boolean.TRUE.equals(map.get("isNotified"));
            return ticket;
        } catch (Exception e) {
            return null;
        }
    }

    private static String str(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    private static int toInt(Object obj) {
        if (obj instanceof Number) return ((Number) obj).intValue();
        if (obj != null) {
            try { return Integer.parseInt(obj.toString()); } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private static double toDouble(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        if (obj != null) {
            try { return Double.parseDouble(obj.toString()); } catch (NumberFormatException ignored) {}
        }
        return 0.0;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ticketId='" + ticketId + '\'' +
                ", userId='" + userId + '\'' +
                ", numbers=" + Arrays.toString(numbers) +
                ", betCount=" + betCount +
                ", isWinner=" + isWinner +
                '}';
    }
}
