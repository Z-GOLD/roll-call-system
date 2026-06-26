package rollcall.view;

import rollcall.dao.StudentDAO;
import rollcall.model.Student;
import rollcall.service.RollCallService;
import rollcall.util.ExcelUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.List;

/**
 * 学生管理面板 —— CSV表格导入导出
 */
public class StudentPanel extends JPanel {

    private final RollCallService rollCallService;
    private final StudentDAO dao = new StudentDAO();
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField searchField;

    public StudentPanel(RollCallService service) {
        this.rollCallService = service;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部面板（两行）
        JPanel topArea = new JPanel(new BorderLayout(5, 5));

        // 按钮栏
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton btnAdd = new JButton("添加学生");
        JButton btnBatch = new JButton("批量添加");
        JButton btnImport = new JButton("导入表格");
        JButton btnExport = new JButton("导出表格");
        JButton btnDelete = new JButton("删除选中");
        JButton btnClear = new JButton("清空全部");
        JButton btnRefresh = new JButton("刷新");

        btnPanel.add(btnAdd); btnPanel.add(btnBatch);
        btnPanel.add(btnImport); btnPanel.add(btnExport);
        btnPanel.add(btnDelete); btnPanel.add(btnClear);
        btnPanel.add(btnRefresh);
        topArea.add(btnPanel, BorderLayout.NORTH);

        // 搜索+请假栏
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        searchPanel.add(new JLabel("搜索:"));
        searchField = new JTextField(12);
        searchPanel.add(searchField);
        JButton btnSearch = new JButton("搜索");
        searchPanel.add(btnSearch);
        JButton btnShowAll = new JButton("显示全部");
        searchPanel.add(btnShowAll);
        searchPanel.add(new JLabel("  |  "));
        JButton btnLeave = new JButton("设为请假");
        searchPanel.add(btnLeave);
        JButton btnUnleave = new JButton("销假");
        searchPanel.add(btnUnleave);
        topArea.add(searchPanel, BorderLayout.SOUTH);

        add(topArea, BorderLayout.NORTH);

        // 表格
        String[] cols = {"序号", "学号", "姓名", "班级", "状态", "被点名", "答出", "成功率"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("宋体", Font.BOLD, 13));
        table.setFont(new Font("宋体", Font.PLAIN, 13));
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnAdd.addActionListener(e -> showAddDialog());
        btnBatch.addActionListener(e -> showBatchDialog());
        btnImport.addActionListener(e -> importCSV());
        btnExport.addActionListener(e -> exportCSV());
        btnDelete.addActionListener(e -> deleteSelected());
        btnClear.addActionListener(e -> clearAll());
        btnRefresh.addActionListener(e -> refreshTable());
        btnSearch.addActionListener(e -> searchStudents());
        btnShowAll.addActionListener(e -> refreshTable());
        btnLeave.addActionListener(e -> setLeave(true));
        btnUnleave.addActionListener(e -> setLeave(false));

        refreshTable();
    }

    private void refreshTable() { refreshTable(null); }

    private void refreshTable(String keyword) {
        tableModel.setRowCount(0);
        List<Student> list = dao.loadStudents();
        int i = 1;
        for (Student s : list) {
            // 搜索过滤
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.trim().toLowerCase();
                if (!s.getStudentNo().toLowerCase().contains(kw)
                        && !s.getName().toLowerCase().contains(kw)
                        && !s.getClassName().toLowerCase().contains(kw)) {
                    continue;
                }
            }
            String status = s.isOnLeave() ? "请假" : "正常";
            tableModel.addRow(new Object[]{
                    i++, s.getStudentNo(), s.getName(), s.getClassName(), status,
                    s.getTotalCalled(), s.getTotalAnswered(),
                    String.format("%.1f%%", s.getAnswerRate())
            });
        }
    }

    /** 搜索学生 */
    private void searchStudents() {
        String keyword = searchField.getText().trim();
        refreshTable(keyword);
    }

    /** 设置/取消请假 */
    private void setLeave(boolean leave) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先在表格中选中一名学生");
            return;
        }
        String no = (String) tableModel.getValueAt(row, 1);
        List<Student> students = dao.loadStudents();
        for (Student s : students) {
            if (s.getStudentNo().equals(no)) {
                s.setOnLeave(leave);
                break;
            }
        }
        dao.saveStudents(students);
        refreshTable();
        String msg = leave ? "已设为请假，该同学将不会被点到" : "已销假，该同学恢复正常点名";
        JOptionPane.showMessageDialog(this, msg);
    }

    private void showAddDialog() {
        JTextField tfNo = new JTextField(10);
        JTextField tfName = new JTextField(10);
        JTextField tfClass = new JTextField(10);
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("学号:")); panel.add(tfNo);
        panel.add(new JLabel("姓名:")); panel.add(tfName);
        panel.add(new JLabel("班级:")); panel.add(tfClass);

        if (JOptionPane.showConfirmDialog(this, panel,
                "添加学生", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String no = tfNo.getText().trim();
            String name = tfName.getText().trim();
            String cls = tfClass.getText().trim();
            if (no.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "学号和姓名不能为空"); return;
            }
            List<Student> students = dao.loadStudents();
            students.add(new Student(no, name, cls.isEmpty() ? "未分班" : cls));
            dao.saveStudents(students);
            refreshTable();
        }
    }

    private void showBatchDialog() {
        JTextArea area = new JTextArea(8, 30);
        area.setFont(new Font("宋体", Font.PLAIN, 13));
        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createTitledBorder("每行：学号,姓名,班级"));

        if (JOptionPane.showConfirmDialog(this, sp,
                "批量添加", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            List<Student> students = dao.loadStudents();
            int count = 0;
            for (String line : area.getText().split("\n")) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("[\\t ,，]+");
                if (parts.length >= 2) {
                    students.add(new Student(parts[0], parts[1],
                            parts.length >= 3 ? parts[2] : "未分班"));
                    count++;
                }
            }
            dao.saveStudents(students);
            JOptionPane.showMessageDialog(this, "成功添加 " + count + " 名学生");
            refreshTable();
        }
    }

    /** 导入表格文件(.xlsx) */
    private void importCSV() {
        FileDialog dialog = new FileDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "选择要导入的Excel表格文件",
                FileDialog.LOAD);
        dialog.setFilenameFilter((dir, name) ->
                name.toLowerCase().endsWith(".xlsx") ||
                name.toLowerCase().endsWith(".xls"));
        dialog.setVisible(true);

        String dir = dialog.getDirectory();
        String fileName = dialog.getFile();
        if (dir != null && fileName != null) {
            File file = new File(dir, fileName);
            try {
                List<Student> imported = ExcelUtil.importFromExcel(file);
                List<Student> students = dao.loadStudents();
                students.addAll(imported);
                dao.saveStudents(students);
                JOptionPane.showMessageDialog(this, "成功导入 " + imported.size() + " 名学生");
                refreshTable();
            } catch (Exception ex) {
                // 详细错误信息
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                JOptionPane.showMessageDialog(this,
                        "导入失败:\n" + ex.toString() + "\n\n" + sw.toString());
            }
        }
    }

    /** 导出表格文件(.xlsx) */
    private void exportCSV() {
        FileDialog dialog = new FileDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "选择导出位置",
                FileDialog.SAVE);
        dialog.setFile("学生点名统计.xlsx");
        dialog.setVisible(true);

        String dir = dialog.getDirectory();
        String fileName = dialog.getFile();
        if (dir != null && fileName != null) {
            File file = new File(dir, fileName);
            if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }
            List<Student> students = dao.loadStudents();
            try {
                ExcelUtil.exportToExcel(file, students);
                JOptionPane.showMessageDialog(this,
                        "成功导出 " + students.size() + " 条记录到:\n" + file.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "导出失败：" + ex.getMessage());
            }
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选中要删除的行"); return;
        }
        String no = (String) tableModel.getValueAt(row, 1);
        List<Student> students = dao.loadStudents();
        students.removeIf(s -> s.getStudentNo().equals(no));
        dao.saveStudents(students);
        refreshTable();
    }

    private void clearAll() {
        if (JOptionPane.showConfirmDialog(this, "确定清空所有数据？",
                "确认", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dao.saveStudents(new java.util.ArrayList<>());
            dao.saveRecords(new java.util.ArrayList<>());
            refreshTable();
        }
    }
}
