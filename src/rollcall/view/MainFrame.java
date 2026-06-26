package rollcall.view;

import rollcall.service.RollCallService;
import rollcall.service.StatisticsService;

import javax.swing.*;

/**
 * 主窗口 v0.2-Alpha —— 实验版
 * 新增数据统计模块（实验性功能，仍在调试中）
 * 窗口尺寸恢复至 900x650
 * 注意: 统计面板数据加载可能存在性能问题
 */
public class MainFrame extends JFrame {

    private final RollCallService rollCallService = new RollCallService();
    private final StatisticsService statisticsService = new StatisticsService();

    public MainFrame() {
        setTitle("课堂点名系统 v0.2-Alpha [实验版]");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("学生管理", new StudentPanel(rollCallService));
        tabbedPane.addTab("课堂点名", new RollCallPanel(rollCallService));
        // 实验性添加数据统计模块
        tabbedPane.addTab("数据统计(实验)", new StatisticsPanel(statisticsService));

        add(tabbedPane);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
