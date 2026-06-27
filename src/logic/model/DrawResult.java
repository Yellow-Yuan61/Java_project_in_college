package logic.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 开奖结果实体类，记录每期开奖的中奖号码和开奖时间。
 *
 * @author Yuan
 * @version 1.0
 */
public class DrawResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 开奖期号ID */
    private String drawId;

    /** 开奖号码（7个，1-36） */
    private int[] winningNumbers;

    /** 开奖时间戳 */
    private long drawTime;

    /** 开奖状态: 0=已生成, 1=已开奖 */
    private int status;

    // ==================== 构造方法 ====================

    public DrawResult() {}

    public DrawResult(String drawId, int[] winningNumbers, long drawTime) {
        this.drawId = drawId;
        this.winningNumbers = Arrays.copyOf(winningNumbers, winningNumbers.length);
        this.drawTime = drawTime;
        this.status = 1;
    }

    // ==================== Getter / Setter ====================

    public String getDrawId() {
        return drawId;
    }

    public void setDrawId(String drawId) {
        this.drawId = drawId;
    }

    public int[] getWinningNumbers() {
        return winningNumbers;
    }

    public void setWinningNumbers(int[] winningNumbers) {
        this.winningNumbers = Arrays.copyOf(winningNumbers, winningNumbers.length);
    }

    public long getDrawTime() {
        return drawTime;
    }

    public void setDrawTime(long drawTime) {
        this.drawTime = drawTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * 获取中奖号码的字符串表示
     * @return 用逗号分隔的号码字符串
     */
    public String getWinningNumbersString() {
        if (winningNumbers == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < winningNumbers.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format("%02d", winningNumbers[i]));
        }
        return sb.toString();
    }

    // ==================== 序列化 ====================

    /**
     * 序列化为字符串: drawId|n1,n2,...,n7|drawTime|status
     * @return 序列化字符串
     */
    public String serialize() {
        return drawId + "|" + getWinningNumbersString() + "|" + drawTime + "|" + status;
    }

    /**
     * 从字符串反序列化
     * @param line 序列化字符串
     * @return 开奖结果对象
     */
    public static DrawResult deserialize(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length < 4) return null;
            DrawResult dr = new DrawResult();
            dr.drawId = parts[0];
            String[] numStrs = parts[1].split(",");
            dr.winningNumbers = new int[numStrs.length];
            for (int i = 0; i < numStrs.length; i++) {
                dr.winningNumbers[i] = Integer.parseInt(numStrs[i]);
            }
            dr.drawTime = Long.parseLong(parts[2]);
            dr.status = Integer.parseInt(parts[3]);
            return dr;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "DrawResult{" +
                "drawId='" + drawId + '\'' +
                ", winningNumbers=" + Arrays.toString(winningNumbers) +
                ", drawTime=" + drawTime +
                '}';
    }
}
