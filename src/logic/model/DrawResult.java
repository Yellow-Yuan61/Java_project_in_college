package logic.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

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

    // ==================== 序列化（JSON格式） ====================

    /**
     * 序列化为JSON字符串
     * @return JSON字符串
     */
    public String serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("drawId", drawId);
        map.put("winningNumbers", winningNumbers);
        map.put("drawTime", drawTime);
        map.put("status", status);
        return logic.util.JsonUtil.toJson(map);
    }

    /**
     * 从JSON字符串反序列化
     * @param line JSON字符串
     * @return 开奖结果对象
     */
    public static DrawResult deserialize(String line) {
        try {
            Map<String, Object> map = logic.util.JsonUtil.parseObject(line);
            DrawResult dr = new DrawResult();
            dr.drawId = str(map.get("drawId"));
            Object numObj = map.get("winningNumbers");
            if (numObj instanceof int[]) {
                dr.winningNumbers = (int[]) numObj;
            } else if (numObj instanceof String) {
                dr.winningNumbers = logic.util.JsonUtil.parseIntArray((String) numObj);
            }
            dr.drawTime = toLong(map.get("drawTime"));
            dr.status = toInt(map.get("status"));
            return dr;
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

    private static long toLong(Object obj) {
        if (obj instanceof Number) return ((Number) obj).longValue();
        if (obj instanceof String) {
            try { return Long.parseLong((String) obj); } catch (NumberFormatException ignored) {}
        }
        return 0L;
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
