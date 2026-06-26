package rollcall.view;

import rollcall.model.RollCallRecord;
import rollcall.model.Student;
import rollcall.service.RollCallService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 课堂点名面板 —— 核心交互
 * 点名→显示结果→记录答出/未答出→更新状态
 */
public class RollCallPanel extends JPanel {

    private final RollCallService service;
    private Student currentStudent;

    private JLabel nameLabel;
    private JLabel infoLabel;
    private JLabel unansweredLabel;
    private JTextArea historyArea;
    private JButton callButton;
    private JButton correctButton;
    private JButton wrongButton;
    private JComboBox<String> courseCombo;

    public RollCallPanel(RollCallService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("课程:"));
        courseCombo = new JComboBox<>(new String[]{
                "默认课程", "Java程序设计", "数据结构", "操作系统"});
        courseCombo.setEditable(true);
        top.add(courseCombo);
        JButton setBtn = new JButton("设置");
        top.add(setBtn);
        JButton resetBtn = new JButton("重置本轮");
        top.add(resetBtn);
        add(top, BorderLayout.NORTH);

        // 中间
        JPanel center = new JPanel(new BorderLayout());
        JPanel result = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(5, 0, 5, 0);

        nameLabel = new JLabel("点击按钮开始点名", SwingConstants.CENTER);
        nameLabel.setFont(new Font("黑体", Font.BOLD, 48));
        nameLabel.setForeground(new Color(220, 50, 50));
        result.add(nameLabel, gbc);

        infoLabel = new JLabel(" ", SwingConstants.CENTER);
        infoLabel.setFont(new Font("宋体", Font.PLAIN, 16));
        result.add(infoLabel, gbc);

        unansweredLabel = new JLabel("本轮连续未答: 0人", SwingConstants.CENTER);
        unansweredLabel.setFont(new Font("宋体", Font.PLAIN, 14));
        unansweredLabel.setForeground(Color.GRAY);
        result.add(unansweredLabel, gbc);

        center.add(result, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        callButton = new JButton("点 名");
        callButton.setFont(new Font("黑体", Font.BOLD, 24));
        callButton.setPreferredSize(new Dimension(160, 60));

        correctButton = new JButton("\u2713 答出");
        correctButton.setFont(new Font("黑体", Font.BOLD, 18));
        correctButton.setEnabled(false);

        wrongButton = new JButton("\u2717 未答出");
        wrongButton.setFont(new Font("黑体", Font.BOLD, 18));
        wrongButton.setEnabled(false);

        btnPanel.add(callButton); btnPanel.add(correctButton); btnPanel.add(wrongButton);
        center.add(btnPanel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        // 右侧
        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("宋体", Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(historyArea);
        sp.setPreferredSize(new Dimension(250, 0));
        sp.setBorder(BorderFactory.createTitledBorder("点名记录"));
        add(sp, BorderLayout.EAST);

        setBtn.addActionListener(e -> {
            service.setCourse((String) courseCombo.getSelectedItem());
            JOptionPane.showMessageDialog(this, "课程已设置");
        });
        resetBtn.addActionListener(e -> {
            service.finishQuestionRound();
            updateStatus();
            JOptionPane.showMessageDialog(this, "本轮已重置");
        });
        callButton.addActionListener(e -> doCall());
        correctButton.addActionListener(e -> recordAnswer(true));
        wrongButton.addActionListener(e -> recordAnswer(false));

        refreshHistory();
    }

    private void doCall() {
        Student s = service.call();
        if (s == null) {
            int total = service.getStudents().size();
            int left = service.getAnsweredThisRound();
            long onLeave = service.getStudents().stream().filter(st -> st.isOnLeave()).count();
            if (total == 0) {
                JOptionPane.showMessageDialog(this, "学生列表为空，请先添加学生！");
            } else if (left + onLeave >= total) {
                JOptionPane.showMessageDialog(this,
                        "所有学生均已答出或请假，无法继续点名！\n请重置本轮或销假后再试。");
            } else {
                JOptionPane.showMessageDialog(this, "暂无可点名学生，请检查请假状态。");
            }
            return;
        }
        currentStudent = s;
        nameLabel.setText(s.getName());
        infoLabel.setText(String.format("学号:%s | 班级:%s | 已点名%d次 答出%d次",
                s.getStudentNo(), s.getClassName(),
                s.getTotalCalled(), s.getTotalAnswered()));
        callButton.setEnabled(false);
        correctButton.setEnabled(true);
        wrongButton.setEnabled(true);
        updateStatus();
    }

    private void recordAnswer(boolean answered) {
        if (currentStudent == null) return;
        service.recordAnswer(currentStudent, answered);
        refreshHistory();

        if (answered) {
            int choice = JOptionPane.showConfirmDialog(this,
                    currentStudent.getName() + " 已答出！\n本轮已答出: "
                            + service.getAnsweredThisRound() + "人\n\n是否继续提问？",
                    "继续提问", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                doCall();
                return;
            } else {
                service.finishQuestionRound();
            }
        } else {
            // 未答出：检查是否"题目太难"
            if (service.isTooHard()) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "救场机制下连续3位高回答率同学也未答出！\n这道题目太难，建议老师讲解一下。\n\n是否继续提问？",
                        "题目太难", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    doCall();
                    return;
                } else {
                    service.finishQuestionRound();
                }
            }
        }

        callButton.setEnabled(true);
        correctButton.setEnabled(false);
        wrongButton.setEnabled(false);
        updateStatus();
    }

    private void updateStatus() {
        int un = service.getUnansweredCount();
        int an = service.getAnsweredThisRound();
        unansweredLabel.setText("本题未答: " + un + "人  |  已答: " + an + "人");
    }

    private void refreshHistory() {
        List<RollCallRecord> records = service.getRecords();
        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, records.size() - 20);
        for (int i = records.size() - 1; i >= start; i--) {
            RollCallRecord r = records.get(i);
            sb.append(String.format("[%s] %s %s\n",
                    r.getCallTime().substring(11, 19), r.getStudentName(),
                    r.isAnswered() ? "答出" : "未答出"));
        }
        historyArea.setText(sb.toString());
    }
}
