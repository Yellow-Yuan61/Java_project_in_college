package ui;

import logic.model.Ticket;
import logic.model.User;
import logic.service.LotteryService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * 购彩面板，提供手动选号（36选7）和随机选号功能，并支持设置投注倍数。
 * 使用GridBagLayout实现号码球排列，支持点击切换选中状态。
 *
 * @author Yuan
 * @version 1.0
 */
public class BuyPanel extends JPanel {

    private static final int BALL_SIZE = 50;
    private static final int BALL_GAP = 5;
    private static final int BALLS_PER_ROW = 9;

    private final User currentUser;
    private final MainFrame mainFrame;
    private final LotteryService lotteryService;

    private JToggleButton[] ballButtons;
    private JSpinner betCountSpinner;
    private JLabel selectedLabel;
    private JLabel balanceLabel;
    private JLabel costLabel;
    private JButton manualBuyButton;
    private JButton randomBuyButton;
    private JTextArea infoArea;

    /** 当前已选中的号码集合 */
    private final Set<Integer> selectedNumbers;

    public BuyPanel(User currentUser, MainFrame mainFrame) {
        this.currentUser = currentUser;
        this.mainFrame = mainFrame;
        this.lotteryService = new LotteryService();
        this.selectedNumbers = new HashSet<>();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 248, 252));

        // 左侧：号码选择区
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(new Color(245, 248, 252));

        // 号码球面板
        JPanel ballPanel = new JPanel(new GridLayout(4, BALLS_PER_ROW, BALL_GAP, BALL_GAP));
        ballPanel.setBackground(new Color(245, 248, 252));
        ballPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 30, 30), 1),
                "请选择7个号码 (1-36)", 0, 0,
                new Font("微软雅黑", Font.BOLD, 12),
                new Color(200, 30, 30)));

        ballButtons = new JToggleButton[LotteryService.NUMBER_POOL_SIZE];
        for (int i = 0; i < LotteryService.NUMBER_POOL_SIZE; i++) {
            final int num = i + 1;
            JToggleButton btn = new JToggleButton(String.format("%02d", num));
            btn.setFont(new Font("微软雅黑", Font.BOLD, 13));
            btn.setPreferredSize(new Dimension(BALL_SIZE, BALL_SIZE));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setToolTipText("号码 " + num);
            updateBallStyle(btn);
            btn.addActionListener((ActionEvent e) -> {
                if (btn.isSelected()) {
                    if (selectedNumbers.size() >= LotteryService.NUMBERS_PER_TICKET) {
                        btn.setSelected(false);
                        JOptionPane.showMessageDialog(this,
                                "最多只能选择" + LotteryService.NUMBERS_PER_TICKET + "个号码",
                                "提示", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    selectedNumbers.add(num);
                } else {
                    selectedNumbers.remove(num);
                }
                updateBallStyle(btn);
                updateSelectionDisplay();
            });
            ballButtons[i] = btn;
            ballPanel.add(btn);
        }
        leftPanel.add(ballPanel, BorderLayout.CENTER);

        // 已选号码显示
        selectedLabel = new JLabel("已选号码：无");
        selectedLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        selectedLabel.setForeground(new Color(200, 30, 30));
        leftPanel.add(selectedLabel, BorderLayout.SOUTH);

        // 右侧：控制面板
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(245, 248, 252));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        rightPanel.setPreferredSize(new Dimension(240, 400));

        // 投注倍数
        JPanel betPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        betPanel.setBackground(new Color(245, 248, 252));
        JLabel betLabel = new JLabel("投注倍数：");
        betLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        betPanel.add(betLabel);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 100, 1);
        betCountSpinner = new JSpinner(spinnerModel);
        betCountSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        betCountSpinner.setPreferredSize(new Dimension(60, 28));
        betPanel.add(betCountSpinner);
        betPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(betPanel);
        rightPanel.add(Box.createVerticalStrut(10));

        // 金额显示
        balanceLabel = new JLabel("账户余额：¥" + String.format("%.2f", currentUser.getBalance()));
        balanceLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        balanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(balanceLabel);
        rightPanel.add(Box.createVerticalStrut(5));

        costLabel = new JLabel("预计消费：¥2.00");
        costLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        costLabel.setForeground(new Color(200, 30, 30));
        costLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(costLabel);
        rightPanel.add(Box.createVerticalStrut(15));

        // 倍数变更监听
        betCountSpinner.addChangeListener(e -> updateCostDisplay());

        // 购买按钮
        manualBuyButton = new JButton("手动选号购买");
        styleBuyButton(manualBuyButton, new Color(50, 130, 200));
        manualBuyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(manualBuyButton);
        rightPanel.add(Box.createVerticalStrut(10));

        randomBuyButton = new JButton("随机选号购买");
        styleBuyButton(randomBuyButton, new Color(50, 160, 80));
        randomBuyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(randomBuyButton);
        rightPanel.add(Box.createVerticalStrut(5));

        JLabel tipLabel = new JLabel("每注 ¥2.00");
        tipLabel.setFont(new Font("微软雅黑", Font.ITALIC, 11));
        tipLabel.setForeground(new Color(150, 150, 150));
        tipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(tipLabel);
        rightPanel.add(Box.createVerticalStrut(10));

        // 快捷操作
        JButton clearButton = new JButton("清除选择");
        clearButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        clearButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        clearButton.addActionListener(e -> clearSelection());
        rightPanel.add(clearButton);

        rightPanel.add(Box.createVerticalStrut(15));

        // 信息区域
        infoArea = new JTextArea(8, 24);
        infoArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        infoArea.setEditable(false);
        infoArea.setBackground(new Color(255, 255, 240));
        infoArea.setBorder(BorderFactory.createTitledBorder("购票记录"));
        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(infoScroll);

        // 布局
        add(leftPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // 事件
        bindEvents();
    }

    /** 更新号码球样式 */
    private void updateBallStyle(JToggleButton btn) {
        if (btn.isSelected()) {
            btn.setBackground(new Color(255, 60, 60));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(new Color(180, 20, 20), 2));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(50, 50, 50));
            btn.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
        }
    }

    /** 更新选中号码显示 */
    private void updateSelectionDisplay() {
        if (selectedNumbers.isEmpty()) {
            selectedLabel.setText("已选号码：无");
        } else {
            java.util.List<Integer> sorted = new java.util.ArrayList<>(selectedNumbers);
            java.util.Collections.sort(sorted);
            StringBuilder sb = new StringBuilder("已选号码：");
            for (int i = 0; i < sorted.size(); i++) {
                if (i > 0) sb.append("  ");
                sb.append(String.format("%02d", sorted.get(i)));
            }
            sb.append("  （").append(sorted.size()).append("/").append(LotteryService.NUMBERS_PER_TICKET).append("）");
            selectedLabel.setText(sb.toString());
        }
        updateCostDisplay();
    }

    /** 更新消费金额显示 */
    private void updateCostDisplay() {
        int betCount = (int) betCountSpinner.getValue();
        double cost = LotteryService.PRICE_PER_BET * betCount;
        costLabel.setText("预计消费：¥" + String.format("%.2f", cost));
    }

    /** 刷新余额显示 */
    public void refreshBalance() {
        balanceLabel.setText("账户余额：¥" + String.format("%.2f", currentUser.getBalance()));
    }

    /** 清除所有选中 */
    private void clearSelection() {
        selectedNumbers.clear();
        for (JToggleButton btn : ballButtons) {
            btn.setSelected(false);
            updateBallStyle(btn);
        }
        updateSelectionDisplay();
    }

    /** 绑定事件 */
    private void bindEvents() {
        manualBuyButton.addActionListener((ActionEvent e) -> {
            if (selectedNumbers.size() != LotteryService.NUMBERS_PER_TICKET) {
                JOptionPane.showMessageDialog(this,
                        "请选择" + LotteryService.NUMBERS_PER_TICKET + "个号码",
                        "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int betCount = (int) betCountSpinner.getValue();
            int[] numbers = new int[LotteryService.NUMBERS_PER_TICKET];
            int i = 0;
            for (int num : selectedNumbers) {
                numbers[i++] = num;
            }
            java.util.Arrays.sort(numbers);

            try {
                Ticket ticket = lotteryService.buyTicket(currentUser.getUserId(), numbers, betCount);
                mainFrame.refreshBalance(currentUser.getBalance());
                refreshBalance();
                infoArea.append("✓ 手动购票成功！\n");
                infoArea.append("  票号：" + ticket.getTicketId() + "\n");
                infoArea.append("  号码：" + ticket.getNumbersString() + "\n");
                infoArea.append("  倍数：" + betCount + "  金额：¥"
                        + String.format("%.2f", betCount * LotteryService.PRICE_PER_BET) + "\n\n");
                clearSelection();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        randomBuyButton.addActionListener((ActionEvent e) -> {
            int betCount = (int) betCountSpinner.getValue();
            try {
                Ticket ticket = lotteryService.buyRandomTicket(currentUser.getUserId(), betCount);
                mainFrame.refreshBalance(currentUser.getBalance());
                refreshBalance();
                infoArea.append("✓ 随机购票成功！\n");
                infoArea.append("  票号：" + ticket.getTicketId() + "\n");
                infoArea.append("  号码：" + ticket.getNumbersString() + "\n");
                infoArea.append("  倍数：" + betCount + "  金额：¥"
                        + String.format("%.2f", betCount * LotteryService.PRICE_PER_BET) + "\n\n");

                // 在号码球上高亮选中的号码
                clearSelection();
                for (int num : ticket.getNumbers()) {
                    ballButtons[num - 1].setSelected(true);
                    updateBallStyle(ballButtons[num - 1]);
                    selectedNumbers.add(num);
                }
                updateSelectionDisplay();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /** 购买按钮样式 */
    private void styleBuyButton(JButton button, Color bgColor) {
        button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 40));
        button.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
