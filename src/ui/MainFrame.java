package ui;

import logic.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * 系统主界面，采用选项卡布局，包含购彩、开奖、历史记录、个人中心四个面板。
 *
 * @author Yuan
 * @version 1.0
 */
public class MainFrame extends JFrame {

    private final User currentUser;
    private JTabbedPane tabbedPane;
    private BuyPanel buyPanel;
    private DrawPanel drawPanel;
    private HistoryPanel historyPanel;
    private UserPanel userPanel;
    private JLabel userInfoLabel;

    public MainFrame(User user) {
        this.currentUser = user;
        initUI();
    }

    private void initUI() {
        setTitle("福彩36选7 - 欢迎 " + currentUser.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 650);
        setMinimumSize(new Dimension(750, 550));
        setLocationRelativeTo(null);

        // 顶部欢迎面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(200, 30, 30));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel welcomeLabel = new JLabel("福彩 36选7 彩票系统");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        userInfoLabel = new JLabel("用户: " + currentUser.getUsername()
                + " | 余额: ¥" + String.format("%.2f", currentUser.getBalance()));
        userInfoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        userInfoLabel.setForeground(new Color(255, 255, 200));
        headerPanel.add(userInfoLabel, BorderLayout.EAST);

        // 选项卡面板
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.BOLD, 14));

        buyPanel = new BuyPanel(currentUser, this);
        drawPanel = new DrawPanel(currentUser, userInfoLabel);
        historyPanel = new HistoryPanel(currentUser);
        userPanel = new UserPanel(currentUser, this);

        tabbedPane.addTab("购买彩票", buyPanel);
        tabbedPane.addTab("开奖区", drawPanel);
        tabbedPane.addTab("历史记录", historyPanel);
        tabbedPane.addTab("个人中心", userPanel);

        // 组装
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * 刷新用户余额显示（更新内存和顶部标签）
     * @param newBalance 新余额
     */
    public void refreshBalance(double newBalance) {
        currentUser.setBalance(newBalance);
        if (userInfoLabel != null) {
            userInfoLabel.setText("用户: " + currentUser.getUsername()
                    + " | 余额: ¥" + String.format("%.2f", newBalance));
        }
    }

    /**
     * 获取当前用户
     * @return 当前登录用户
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * 切换到开奖标签页
     */
    public void switchToDrawTab() {
        tabbedPane.setSelectedIndex(1);
    }
}
