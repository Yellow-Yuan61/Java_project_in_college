package ui;

import logic.model.User;
import logic.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * 用户登录/注册界面。
 * 提供用户登录和注册功能，登录成功后跳转至主界面，并检查中奖通知。
 *
 * @author Yuan
 * @version 1.0
 */
public class LoginFrame extends JFrame {

    private static final String TITLE = "福彩36选7 - 彩票购买抽奖系统";

    private final UserService userService;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField phoneField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;

    /** 是否处于注册模式 */
    private boolean registerMode;

    public LoginFrame() {
        this.userService = new UserService();
        this.registerMode = false;
        initUI();
    }

    /** 初始化界面 */
    private void initUI() {
        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        mainPanel.setBackground(new Color(240, 245, 250));

        // 标题
        JLabel titleLabel = new JLabel("福彩 36选7", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(new Color(200, 30, 30));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel subtitleLabel = new JLabel("彩票购买抽奖系统", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 100, 100));

        // 标题面板
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(240, 245, 250));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 245, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 用户名
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userLabel = new JLabel("用户名：");
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel passLabel = new JLabel("密  码：");
        passLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(passwordField, gbc);

        // 电话号码（仅注册模式显示）
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel phoneLabel = new JLabel("电  话：");
        phoneLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(phoneLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        phoneField = new JTextField(15);
        phoneField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        phoneField.setVisible(false);
        formPanel.add(phoneField, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(new Color(240, 245, 250));

        loginButton = new JButton("登  录");
        styleButton(loginButton, new Color(50, 130, 200));
        buttonPanel.add(loginButton);

        registerButton = new JButton("注  册");
        styleButton(registerButton, new Color(50, 160, 80));
        buttonPanel.add(registerButton);

        JButton switchButton = new JButton("切换注册/登录");
        switchButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        switchButton.setForeground(new Color(80, 80, 200));
        switchButton.setContentAreaFilled(false);
        switchButton.setBorderPainted(false);
        buttonPanel.add(switchButton);

        // 状态标签
        statusLabel = new JLabel("请输入用户名和密码登录", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(150, 150, 150));

        // 组装
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(new Color(240, 245, 250));
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        southPanel.add(statusLabel, BorderLayout.SOUTH);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);

        // 事件绑定
        bindEvents(switchButton);
    }

    /** 按钮样式 */
    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(100, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /** 绑定事件 */
    private void bindEvents(JButton switchButton) {
        loginButton.addActionListener((ActionEvent e) -> {
            if (registerMode) {
                statusLabel.setText("当前为注册模式，请点击切换按钮切换到登录");
                statusLabel.setForeground(Color.RED);
                return;
            }
            doLogin();
        });

        registerButton.addActionListener((ActionEvent e) -> {
            if (!registerMode) {
                statusLabel.setText("当前为登录模式，请点击切换按钮切换到注册");
                statusLabel.setForeground(Color.RED);
                return;
            }
            doRegister();
        });

        switchButton.addActionListener((ActionEvent e) -> {
            registerMode = !registerMode;
            if (registerMode) {
                phoneField.setVisible(true);
                loginButton.setText("返回登录");
                registerButton.setText("确认注册");
                statusLabel.setText("注册模式：请填写所有字段");
            } else {
                phoneField.setVisible(false);
                loginButton.setText("登  录");
                registerButton.setText("注  册");
                statusLabel.setText("登录模式：请输入用户名和密码");
            }
            statusLabel.setForeground(new Color(150, 150, 150));
            pack();
        });

        // 支持回车键登录
        getRootPane().setDefaultButton(loginButton);
    }

    /** 执行登录 */
    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("用户名和密码不能为空");
            statusLabel.setForeground(Color.RED);
            return;
        }

        User user = userService.login(username, password);
        if (user == null) {
            statusLabel.setText("用户名或密码错误");
            statusLabel.setForeground(Color.RED);
            return;
        }

        // 登录成功，检查中奖通知
        String notification = userService.checkNotification(user.getUserId());

        // 打开主界面
        MainFrame mainFrame = new MainFrame(user);
        mainFrame.setVisible(true);

        // 显示中奖通知
        if (notification != null && !notification.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame,
                    notification, "中奖通知",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        // 关闭登录窗口
        dispose();
    }

    /** 执行注册 */
    private void doRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String phone = phoneField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            statusLabel.setText("所有字段不能为空");
            statusLabel.setForeground(Color.RED);
            return;
        }

        try {
            User user = userService.register(username, password, phone);
            statusLabel.setText("注册成功！用户ID: " + user.getUserId() + "，初始余额: 1000元");
            statusLabel.setForeground(new Color(0, 150, 0));

            // 自动切换回登录模式
            registerMode = false;
            phoneField.setVisible(false);
            loginButton.setText("登  录");
            registerButton.setText("注  册");
            pack();

        } catch (IllegalArgumentException ex) {
            statusLabel.setText(ex.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }
}
