package rollcall.view;

import rollcall.service.RollCallService;

import javax.swing.*;

/**
 * 主窗口 v0.1 —— 初始简化版
 * 包含学生管理和课堂点名两个核心功能
 * TODO: 后续版本添加数据统计模块
 */
public class MainFrame extends JFrame {

    private final RollCallService rollCallService = new RollCallService();

    public MainFrame() {
        setTitle("课堂点名系统 v0.1");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("学生管理", new StudentPanel(rollCallService));
        tabbedPane.addTab("课堂点名", new RollCallPanel(rollCallService));

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
