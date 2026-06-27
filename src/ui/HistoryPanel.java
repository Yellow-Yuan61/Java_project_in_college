package ui;

import logic.model.DrawResult;
import logic.model.Ticket;
import logic.model.User;
import logic.service.LotteryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 历史记录面板，展示用户的购票记录、中奖历史和所有开奖结果。
 * 使用JTable以表格形式展示数据。
 *
 * @author Yuan
 * @version 1.0
 */
public class HistoryPanel extends JPanel {

    private final User currentUser;
    private final LotteryService lotteryService;

    private JTable ticketTable;
    private DefaultTableModel ticketTableModel;
    private JTable drawTable;
    private DefaultTableModel drawTableModel;

    public HistoryPanel(User currentUser) {
        this.currentUser = currentUser;
        this.lotteryService = new LotteryService();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 248, 252));

        // 选项卡
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.BOLD, 13));

        // 我的购票记录
        tabbedPane.addTab("我的购票记录", createMyTicketsPanel());
        // 全部开奖记录
        tabbedPane.addTab("开奖历史", createDrawHistoryPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // 刷新按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(new Color(245, 248, 252));
        JButton refreshButton = new JButton("刷新数据");
        refreshButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        refreshButton.addActionListener(e -> refreshAll());
        bottomPanel.add(refreshButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /** 创建购票记录面板 */
    private JPanel createMyTicketsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);

        // 表格列
        String[] columns = {"票号", "号码", "倍数", "是否中奖", "中奖等级", "匹配个数", "奖金", "开奖期号"};
        ticketTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ticketTable = new JTable(ticketTableModel);
        ticketTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        ticketTable.setRowHeight(25);
        ticketTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        ticketTable.getTableHeader().setBackground(new Color(220, 220, 220));

        JScrollPane scrollPane = new JScrollPane(ticketTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /** 创建开奖历史面板 */
    private JPanel createDrawHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);

        String[] columns = {"期号", "中奖号码", "开奖时间"};
        drawTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        drawTable = new JTable(drawTableModel);
        drawTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        drawTable.setRowHeight(28);
        drawTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        drawTable.getTableHeader().setBackground(new Color(220, 220, 220));

        JScrollPane scrollPane = new JScrollPane(drawTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /** 刷新所有数据 */
    public void refreshAll() {
        refreshMyTickets();
        refreshDrawHistory();
    }

    /** 刷新我的购票记录 */
    private void refreshMyTickets() {
        ticketTableModel.setRowCount(0);
        List<Ticket> tickets = lotteryService.getUserTicketHistory(currentUser.getUserId());
        // 使用Stream按时间倒序（最新的在前）
        tickets.stream()
                .sorted((a, b) -> b.getTicketId().compareTo(a.getTicketId()))
                .forEach(ticket -> {
                    String prizeName;
                    if (ticket.getPrizeLevel() == 2) {
                        prizeName = "特等奖";
                    } else if (ticket.getPrizeLevel() == 1) {
                        prizeName = "一等奖";
                    } else {
                        prizeName = "未中奖";
                    }
                    String drawId = ticket.getDrawId() != null ? ticket.getDrawId() : "待开奖";
                    ticketTableModel.addRow(new Object[]{
                            ticket.getTicketId(),
                            ticket.getNumbersString(),
                            ticket.getBetCount() + "倍",
                            ticket.isWinner() ? "是" : "否",
                            prizeName,
                            ticket.getMatchedCount() + "个",
                            "¥" + String.format("%.2f", ticket.getPrizeAmount()),
                            drawId
                    });
                });
    }

    /** 刷新开奖历史 */
    private void refreshDrawHistory() {
        drawTableModel.setRowCount(0);
        List<DrawResult> draws = lotteryService.getAllDrawResults();
        draws.stream()
                .sorted((a, b) -> Long.compare(b.getDrawTime(), a.getDrawTime()))
                .forEach(draw -> {
                    drawTableModel.addRow(new Object[]{
                            draw.getDrawId(),
                            draw.getWinningNumbersString(),
                            new java.util.Date(draw.getDrawTime()).toString()
                    });
                });
    }
}
