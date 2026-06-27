package logic.model;

import java.io.Serializable;
import java.util.Arrays;

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

    // ==================== 序列化 ====================

    /**
     * 序列化为字符串，格式: ticketId|userId|drawId|n1,n2,...n7|betCount|isWinner|prizeLevel|matchedCount|prizeAmount|isNotified
     * @return 序列化字符串
     */
    public String serialize() {
        return ticketId + "|" + userId + "|" + drawId + "|"
                + getNumbersString() + "|" + betCount + "|"
                + (isWinner ? "1" : "0") + "|" + prizeLevel + "|"
                + matchedCount + "|" + prizeAmount + "|"
                + (isNotified ? "1" : "0");
    }

    /**
     * 从字符串反序列化
     * @param line 序列化字符串
     * @return 彩票对象
     */
    public static Ticket deserialize(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length < 10) return null;
            Ticket ticket = new Ticket();
            ticket.ticketId = parts[0];
            ticket.userId = parts[1];
            ticket.drawId = parts[2];
            String[] numStrs = parts[3].split(",");
            ticket.numbers = new int[numStrs.length];
            for (int i = 0; i < numStrs.length; i++) {
                ticket.numbers[i] = Integer.parseInt(numStrs[i]);
            }
            ticket.betCount = Integer.parseInt(parts[4]);
            ticket.isWinner = "1".equals(parts[5]);
            ticket.prizeLevel = Integer.parseInt(parts[6]);
            ticket.matchedCount = Integer.parseInt(parts[7]);
            ticket.prizeAmount = Double.parseDouble(parts[8]);
            ticket.isNotified = "1".equals(parts[9]);
            return ticket;
        } catch (Exception e) {
            return null;
        }
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
