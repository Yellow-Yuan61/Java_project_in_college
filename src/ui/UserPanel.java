package ui;

import logic.model.Ticket;
import logic.model.User;
import logic.service.LotteryService;
import logic.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * 个人中心面板，展示用户信息、账户余额，并提供充值和中奖记录查询功能。
 *
 * @author Yuan
 * @version 1.0
 */
public class UserPanel extends JPanel {

    private final User currentUser;
    private final MainFrame mainFrame;
    private final UserService userService;
    private final LotteryService lotteryService;

    private JLabel userIdLabel;
    private JLabel usernameLabel;
    private JLabel balanceLabel;
    private JLabel phoneLabel;
    private JTextArea winHistoryArea;

    public UserPanel(User currentUser, MainFrame mainFrame) {
        this.currentUser = currentUser;
        this.mainFrame = mainFrame;
        this.userService = new UserService();
        this.lotteryService = new LotteryService();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        setBackground(new Color(245, 248, 252));

        // 左侧：用户信息
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(50, 130, 200), 1),
                        "用户信息", 0, 0,
                        new Font("微软雅黑", Font.BOLD, 14),
                        new Color(50, 130, 200)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        infoPanel.setPreferredSize(new Dimension(320, 400));

        Font labelFont = new Font("微软雅黑", Font.PLAIN, 14);
        Font valueFont = new Font("微软雅黑", Font.BOLD, 14);

        userIdLabel = createInfoRow(infoPanel, "用户ID：", currentUser.getUserId(), labelFont, valueFont);
        infoPanel.add(Box.createVerticalStrut(12));
        usernameLabel = createInfoRow(infoPanel, "用户名：", currentUser.getUsername(), labelFont, valueFont);
        infoPanel.add(Box.createVerticalStrut(12));
        balanceLabel = createInfoRow(infoPanel, "账户余额：",
                "¥" + String.format("%.2f", currentUser.getBalance()), labelFont,
                new Font("微软雅黑", Font.BOLD, 18));
        balanceLabel.setForeground(new Color(200, 30, 30));
        infoPanel.add(Box.createVerticalStrut(12));
        phoneLabel = createInfoRow(infoPanel, "电话号码：", currentUser.getPhone(), labelFont, valueFont);

        infoPanel.add(Box.createVerticalStrut(20));

        // 充值区
        JPanel rechargePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rechargePanel.setBackground(Color.WHITE);
        JLabel rechargeLabel = new JLabel("充值金额：");
        rechargeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        rechargePanel.add(rechargeLabel);

        JTextField amountField = new JTextField(8);
        amountField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        rechargePanel.add(amountField);

        JButton rechargeButton = new JButton("充值");
        rechargeButton.setFont(new Font("微软雅黑", Font.BOLD, 13));
        rechargeButton.setBackground(new Color(50, 160, 80));
        rechargeButton.setForeground(Color.WHITE);
        rechargeButton.setFocusPainted(false);
        rechargeButton.setBorderPainted(false);
        rechargeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rechargeButton.addActionListener((ActionEvent e) -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "充值金额必须大于0", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (amount > 100000) {
                    JOptionPane.showMessageDialog(this, "单次充值不能超过100000元", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double newBalance = userService.updateBalance(currentUser.getUserId(), amount);
                currentUser.setBalance(newBalance);
                mainFrame.refreshBalance(newBalance);
                refreshInfo();
                JOptionPane.showMessageDialog(this,
                        "充值成功！¥" + String.format("%.2f", amount),
                        "充值", JOptionPane.INFORMATION_MESSAGE);
                amountField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的金额", "错误", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        rechargePanel.add(rechargeButton);
        infoPanel.add(rechargePanel);

        // 右侧：中奖记录
        JPanel winPanel = new JPanel(new BorderLayout(5, 5));
        winPanel.setBackground(Color.WHITE);
        winPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 30, 30), 1),
                        "我的中奖记录", 0, 0,
                        new Font("微软雅黑", Font.BOLD, 14),
                        new Color(200, 30, 30)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        winHistoryArea = new JTextArea();
        winHistoryArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        winHistoryArea.setEditable(false);
        winHistoryArea.setBackground(new Color(255, 255, 245));
        JScrollPane winScroll = new JScrollPane(winHistoryArea);
        winPanel.add(winScroll, BorderLayout.CENTER);

        JButton refreshWinButton = new JButton("刷新中奖记录");
        refreshWinButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        refreshWinButton.addActionListener(e -> refreshWinHistory());
        winPanel.add(refreshWinButton, BorderLayout.SOUTH);

        // 布局
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(245, 248, 252));
        leftPanel.add(infoPanel, BorderLayout.NORTH);
        add(leftPanel, BorderLayout.WEST);
        add(winPanel, BorderLayout.CENTER);

        // 初始加载
        refreshWinHistory();
    }

    /** 创建信息行 */
    private JLabel createInfoRow(JPanel panel, String labelText, String valueText,
                                  Font labelFont, Font valueFont) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setBackground(Color.WHITE);
        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        row.add(label);
        JLabel value = new JLabel(valueText);
        value.setFont(valueFont);
        row.add(value);
        panel.add(row);
        return value;
    }

    /** 刷新用户信息显示（从DataStore获取最新数据） */
    public void refreshInfo() {
        // 从DataStore获取最新余额，同步到currentUser
        User freshUser = userService.getUserById(currentUser.getUserId());
        if (freshUser != null) {
            currentUser.setBalance(freshUser.getBalance());
            currentUser.setPhone(freshUser.getPhone());
        }
        userIdLabel.setText(currentUser.getUserId());
        usernameLabel.setText(currentUser.getUsername());
        balanceLabel.setText("¥" + String.format("%.2f", currentUser.getBalance()));
        phoneLabel.setText(currentUser.getPhone());
    }

    /** 刷新中奖记录 */
    public void refreshWinHistory() {
        List<Ticket> wins = lotteryService.getUserWinHistory(currentUser.getUserId());
        if (wins.isEmpty()) {
            winHistoryArea.setText("暂无中奖记录\n\n继续购买彩票，好运等着您！");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("共中奖 ").append(wins.size()).append(" 次\n");
            sb.append("──────────────────────────────────\n");
            double totalWin = 0;
            for (Ticket ticket : wins) {
                String prizeName = ticket.getPrizeLevel() == 2 ? "特等奖" : "一等奖";
                sb.append("期号：").append(ticket.getDrawId() != null ? ticket.getDrawId() : "未知").append("\n");
                sb.append("  号码：").append(ticket.getNumbersString()).append("\n");
                sb.append("  等级：").append(prizeName)
                        .append(" | 匹配").append(ticket.getMatchedCount()).append("个")
                        .append(" | ").append(ticket.getBetCount()).append("倍\n");
                sb.append("  奖金：¥").append(String.format("%.2f", ticket.getPrizeAmount())).append("\n");
                sb.append("──────────────────────────────────\n");
                totalWin += ticket.getPrizeAmount();
            }
            sb.append("累计中奖金额：¥").append(String.format("%.2f", totalWin)).append("\n");
            winHistoryArea.setText(sb.toString());
        }
    }
}
