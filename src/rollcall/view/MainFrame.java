package rollcall.view;

import rollcall.service.RollCallService;
import rollcall.service.StatisticsService;

import javax.swing.*;

/**
 * 主窗口 v0.3-Beta —— 公测版
 * MVC架构，三个功能面板均已稳定
 * 数据统计模块已通过测试，正式上线
 * 新增: 救场机制、加权随机算法优化
 */
public class MainFrame extends JFrame {

    private final RollCallService rollCallService = new RollCallService();
    private final StatisticsService statisticsService = new StatisticsService();

    public MainFrame() {
        setTitle("课堂点名系统 v0.3-Beta [公测版]");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("学生管理", new StudentPanel(rollCallService));
        tabbedPane.addTab("课堂点名", new RollCallPanel(rollCallService));
        tabbedPane.addTab("数据统计", new StatisticsPanel(statisticsService));

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
