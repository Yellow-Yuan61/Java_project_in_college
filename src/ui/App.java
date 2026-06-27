package ui;

import logic.service.DrawServer;

import javax.swing.*;

/**
 * 应用程序入口类，负责初始化系统（启动Socket服务器）并显示登录界面。
 *
 * @author Yuan
 * @version 1.0
 */
public class App {

    public static void main(String[] args) {
        // 设置Swing外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("无法设置系统外观: " + e.getMessage());
        }

        // 在EDT线程中启动GUI
        SwingUtilities.invokeLater(() -> {
            // 启动抽奖Socket服务器
            DrawServer drawServer = DrawServer.getInstance();
            if (!drawServer.isRunning()) {
                drawServer.start();
            }

            // 显示登录界面
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
