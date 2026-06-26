package rollcall.view;

import rollcall.model.Student;
import rollcall.service.StatisticsService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * 数据统计面板 —— 汇总卡片 + 排名表 + 频次分布
 */
public class StatisticsPanel extends JPanel {

    private final StatisticsService service;
    private JLabel totalStu, totalCalls, answered, rate;
    private DefaultTableModel rankModel;

    public StatisticsPanel(StatisticsService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 汇总卡片
        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 0));
        totalStu = makeCard(cards, "学生总数", "0");
        totalCalls = makeCard(cards, "总点名次数", "0");
        answered = makeCard(cards, "总答出次数", "0");
        rate = makeCard(cards, "总体成功率", "0%");
        add(cards, BorderLayout.NORTH);

        // 排名表
        String[] cols = {"排名", "学号", "姓名", "班级", "被点名", "答出", "成功率"};
        rankModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(rankModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("宋体", Font.BOLD, 13));
        table.setFont(new Font("宋体", Font.PLAIN, 13));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("学生点名统计排名"));
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        // 刷新
        JButton refreshBtn = new JButton("刷新数据");
        refreshBtn.addActionListener(e -> refreshAll());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(refreshBtn);
        add(bottom, BorderLayout.SOUTH);

        refreshAll();
    }

    private JLabel makeCard(JPanel parent, String label, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        card.setBackground(Color.WHITE);

        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font("Arial", Font.BOLD, 28));
        val.setForeground(new Color(67, 97, 238));
        card.add(val, BorderLayout.CENTER);

        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("宋体", Font.PLAIN, 12));
        lbl.setForeground(Color.GRAY);
        card.add(lbl, BorderLayout.SOUTH);
        parent.add(card);
        return val;
    }

    private void refreshAll() {
        Map<String, Object> s = service.getSummary();
        totalStu.setText(String.valueOf(s.get("totalStudents")));
        totalCalls.setText(String.valueOf(s.get("totalCalls")));
        answered.setText(String.valueOf(s.get("totalAnswered")));
        rate.setText(s.get("overallRate") + "%");

        rankModel.setRowCount(0);
        List<Student> students = service.getStudents();
        students.sort((a, b) -> b.getTotalCalled() - a.getTotalCalled());
        int rank = 1;
        for (Student st : students) {
            rankModel.addRow(new Object[]{
                    rank++, st.getStudentNo(), st.getName(), st.getClassName(),
                    st.getTotalCalled(), st.getTotalAnswered(),
                    String.format("%.1f%%", st.getAnswerRate())
            });
        }
    }
}
