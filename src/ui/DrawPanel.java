package ui;

import logic.model.DrawResult;
import logic.model.Ticket;
import logic.model.User;
import logic.service.DrawServer;
import logic.service.LotteryService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

/**
 * 开奖面板，通过Socket连接到DrawServer实现号码滚动动画。
 * 用户点击"开始抽奖"后启动滚动，点击"停止"后获取最终开奖结果。
 * 开奖完毕后自动进行兑奖并在面板上展示中奖信息。
 *
 * @author Yuan
 * @version 1.0
 */
public class DrawPanel extends JPanel {

    private final User currentUser;
    private final JLabel balanceLabel;
    private final LotteryService lotteryService;

    /** 号码球显示标签 */
    private JLabel[] ballLabels;

    /** 按钮 */
    private JButton startDrawButton;
    private JButton stopDrawButton;

    /** 状态区 */
    private JLabel statusLabel;
    private JTextArea resultArea;

    /** Socket通信相关 */
    private Socket socket;
    private PrintWriter socketWriter;
    private BufferedReader socketReader;
    private volatile boolean isDrawing;
    private Thread receiveThread;

    public DrawPanel(User currentUser, JLabel balanceLabel) {
        this.currentUser = currentUser;
        this.balanceLabel = balanceLabel;
        this.lotteryService = new LotteryService();
        this.isDrawing = false;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 248, 252));

        // 顶部：号码显示区
        JPanel ballDisplayPanel = new JPanel(new BorderLayout());
        ballDisplayPanel.setBackground(new Color(245, 248, 252));
        ballDisplayPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 30, 30), 2),
                "开奖号码", 0, 0,
                new Font("微软雅黑", Font.BOLD, 16),
                new Color(200, 30, 30)));

        JPanel ballsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        ballsPanel.setBackground(new Color(245, 248, 252));

        ballLabels = new JLabel[LotteryService.NUMBERS_PER_TICKET];
        for (int i = 0; i < LotteryService.NUMBERS_PER_TICKET; i++) {
            JLabel ball = new JLabel("--", SwingConstants.CENTER);
            ball.setPreferredSize(new Dimension(65, 65));
            ball.setFont(new Font("Arial", Font.BOLD, 24));
            ball.setForeground(new Color(200, 30, 30));
            ball.setBackground(new Color(255, 240, 240));
            ball.setOpaque(true);
            ball.setBorder(BorderFactory.createLineBorder(new Color(200, 30, 30), 2));
            ballLabels[i] = ball;
            ballsPanel.add(ball);
        }
        ballDisplayPanel.add(ballsPanel, BorderLayout.CENTER);

        // 状态标签
        statusLabel = new JLabel("点击「开始抽奖」按钮启动开奖", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        statusLabel.setForeground(new Color(100, 100, 100));
        ballDisplayPanel.add(statusLabel, BorderLayout.SOUTH);

        // 中部：按钮区
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        buttonPanel.setBackground(new Color(245, 248, 252));

        startDrawButton = new JButton("开始抽奖");
        startDrawButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        startDrawButton.setBackground(new Color(200, 30, 30));
        startDrawButton.setForeground(Color.WHITE);
        startDrawButton.setFocusPainted(false);
        startDrawButton.setBorderPainted(false);
        startDrawButton.setPreferredSize(new Dimension(150, 45));
        startDrawButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        stopDrawButton = new JButton("停  止");
        stopDrawButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        stopDrawButton.setBackground(new Color(100, 100, 100));
        stopDrawButton.setForeground(Color.WHITE);
        stopDrawButton.setFocusPainted(false);
        stopDrawButton.setBorderPainted(false);
        stopDrawButton.setPreferredSize(new Dimension(150, 45));
        stopDrawButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        stopDrawButton.setEnabled(false);

        buttonPanel.add(startDrawButton);
        buttonPanel.add(stopDrawButton);

        // 底部：结果展示区
        resultArea = new JTextArea(10, 50);
        resultArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(255, 255, 240));
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createTitledBorder("抽奖结果"));

        // 组装
        add(ballDisplayPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(resultScroll, BorderLayout.SOUTH);

        bindEvents();
    }

    /** 绑定事件 */
    private void bindEvents() {
        startDrawButton.addActionListener((ActionEvent e) -> startDraw());
        stopDrawButton.addActionListener((ActionEvent e) -> stopDraw());
    }

    /**
     * 连接Socket服务器并开始抽奖滚动
     */
    private void startDraw() {
        if (isDrawing) return;

        try {
            // 连接DrawServer
            socket = new Socket("localhost", DrawServer.DEFAULT_PORT);
            socketWriter = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            socketReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));

            // 发送开始指令
            socketWriter.println("DRAW_START");

            // 更新UI状态
            isDrawing = true;
            startDrawButton.setEnabled(false);
            stopDrawButton.setEnabled(true);
            statusLabel.setText("号码滚动中... 请点击「停止」按钮");
            statusLabel.setForeground(new Color(200, 30, 30));

            // 启动接收线程
            receiveThread = new Thread(this::receiveMessages, "DrawPanel-Reader");
            receiveThread.setDaemon(true);
            receiveThread.start();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "无法连接抽奖服务器：" + ex.getMessage(),
                    "连接失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 停止抽奖
     */
    private void stopDraw() {
        if (!isDrawing) return;

        // 发送停止指令
        if (socketWriter != null) {
            socketWriter.println("DRAW_STOP");
        }

        stopDrawButton.setEnabled(false);
        statusLabel.setText("正在生成开奖结果...");
        statusLabel.setForeground(new Color(200, 120, 0));
    }

    /**
     * 接收服务器消息的线程
     */
    private void receiveMessages() {
        try {
            String line;
            while (isDrawing && (line = socketReader.readLine()) != null) {
                if (line.startsWith("ROLL:")) {
                    // 更新号码显示
                    String[] numStrs = line.substring(5).split(",");
                    SwingUtilities.invokeLater(() -> {
                        for (int i = 0; i < ballLabels.length && i < numStrs.length; i++) {
                            ballLabels[i].setText(numStrs[i]);
                        }
                    });
                } else if (line.startsWith("RESULT:")) {
                    // 最终结果
                    isDrawing = false;
                    String[] numStrs = line.substring(7).split(",");
                    int[] winningNumbers = new int[numStrs.length];
                    for (int i = 0; i < numStrs.length; i++) {
                        winningNumbers[i] = Integer.parseInt(numStrs[i]);
                    }

                    SwingUtilities.invokeLater(() -> processResult(winningNumbers));
                    break;
                }
            }
        } catch (IOException e) {
            if (isDrawing) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("与服务器连接断开");
                    statusLabel.setForeground(Color.RED);
                    resetButtons();
                });
            }
        } finally {
            closeSocket();
        }
    }

    /**
     * 处理开奖结果：保存结果、兑奖、显示
     */
    private void processResult(int[] winningNumbers) {
        // 更新号码显示
        for (int i = 0; i < ballLabels.length && i < winningNumbers.length; i++) {
            ballLabels[i].setText(String.format("%02d", winningNumbers[i]));
        }

        // 创建开奖结果
        DrawResult drawResult = lotteryService.startNewDraw();
        // 使用抽出的号码（覆盖随机生成的）
        drawResult.setWinningNumbers(winningNumbers);

        // 执行兑奖
        List<Ticket> winners = lotteryService.doPrizeDraw(drawResult);

        // 显示结果
        displayResult(drawResult, winners);

        statusLabel.setText("开奖完成！期号：" + drawResult.getDrawId());
        statusLabel.setForeground(new Color(0, 100, 0));
        resetButtons();

        // 更新余额显示
        balanceLabel.setText("余额: ¥" + String.format("%.2f", currentUser.getBalance()));
    }

    /**
     * 显示开奖结果和中奖信息
     */
    private void displayResult(DrawResult drawResult, List<Ticket> winners) {
        StringBuilder sb = new StringBuilder();
        sb.append("══════════════ 开奖结果 ══════════════\n");
        sb.append("期        号：").append(drawResult.getDrawId()).append("\n");
        sb.append("开奖号码：").append(drawResult.getWinningNumbersString()).append("\n");
        sb.append("开奖时间：").append(new java.util.Date(drawResult.getDrawTime())).append("\n");
        sb.append("──────────────────────────────────────\n");

        Map<String, Object> stats = lotteryService.getDrawStatistics(drawResult.getDrawId());
        sb.append("本期参与彩票数：").append(stats.get("totalTickets")).append(" 张\n");
        sb.append("特等奖(7中7)：").append(stats.get("specialPrizeCount")).append(" 注\n");
        sb.append("一等奖(7中6)：").append(stats.get("firstPrizeCount")).append(" 注\n");
        sb.append("派奖总额：¥").append(String.format("%.2f", stats.get("totalPayout"))).append("\n");

        if (!winners.isEmpty()) {
            sb.append("──────────────────────────────────────\n");
            sb.append("中奖明细：\n");
            for (Ticket winner : winners) {
                String prizeName = winner.getPrizeLevel() == 2 ? "特等奖" : "一等奖";
                sb.append("  用户ID: ").append(winner.getUserId())
                        .append(" | ").append(prizeName)
                        .append(" | 号码: ").append(winner.getNumbersString())
                        .append(" | 匹配: ").append(winner.getMatchedCount()).append("个")
                        .append(" | 奖金: ¥").append(String.format("%.2f", winner.getPrizeAmount()))
                        .append(" | 倍数: ").append(winner.getBetCount()).append("倍\n");
            }
        }

        sb.append("══════════════════════════════════════\n");
        resultArea.setText(sb.toString());
    }

    /** 重置按钮状态 */
    private void resetButtons() {
        startDrawButton.setEnabled(true);
        stopDrawButton.setEnabled(false);
    }

    /** 关闭Socket连接 */
    private void closeSocket() {
        try {
            if (socketWriter != null) {
                socketWriter.println("QUIT");
                socketWriter.close();
            }
            if (socketReader != null) socketReader.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}
