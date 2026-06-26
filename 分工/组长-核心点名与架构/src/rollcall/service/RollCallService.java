package rollcall.service;

import rollcall.dao.StudentDAO;
import rollcall.model.RollCallRecord;
import rollcall.model.Student;
import rollcall.util.WeightedRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * 点名服务 —— 核心业务逻辑
 * 采用策略模式：WeightedRandom提供算法策略
 * 包含救场机制：连续N人未答出自动切换策略
 */
public class RollCallService {

    private final StudentDAO dao;
    private List<Student> students;
    private List<RollCallRecord> records;
    private String currentCourse = "默认课程";
    private final List<Integer> unansweredList = new ArrayList<>();
    private final List<Integer> answeredThisRound = new ArrayList<>();
    /** 救场模式：已用次数 / 失败次数 */
    private int rescueCallCount = 0;
    private int rescueFailCount = 0;
    private boolean inRescue = false;
    private static final int RESCUE_THRESHOLD = 3;
    private static final int RESCUE_ROUNDS = 3;

    public RollCallService() {
        dao = new StudentDAO();
        reload();
    }

    public void setCourse(String course) { currentCourse = course; }
    public String getCourse() { return currentCourse; }
    public List<Student> getStudents() { return students; }
    public List<RollCallRecord> getRecords() { return records; }
    public int getUnansweredCount() { return unansweredList.size(); }
    public int getAnsweredThisRound() { return answeredThisRound.size(); }
    public boolean isTooHard() { return rescueFailCount >= RESCUE_ROUNDS; }

    public void finishQuestionRound() {
        unansweredList.clear();
        answeredThisRound.clear();
        rescueCallCount = 0;
        rescueFailCount = 0;
        inRescue = false;
    }

    public void resetRound() {
        unansweredList.clear();
        answeredThisRound.clear();
        rescueCallCount = 0;
        rescueFailCount = 0;
        inRescue = false;
    }

    public void reload() {
        students = dao.loadStudents();
        records = dao.loadRecords();
    }

    /** 执行点名 —— 排除本轮已答出的学生 */
    public Student call() {
        reload();
        if (students.isEmpty()) return null;

        List<Integer> allExclude = new ArrayList<>(unansweredList);
        allExclude.addAll(answeredThisRound);
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).isOnLeave()) {
                allExclude.add(i);
            }
        }

        int index;
        // 前3人连续失败 → 进入救场模式，之后3人从高正确率同学中选
        if (inRescue || unansweredList.size() >= RESCUE_THRESHOLD) {
            inRescue = true;
            rescueCallCount++;
            index = WeightedRandom.selectTop(students);
        } else {
            index = WeightedRandom.select(students, allExclude);
        }

        if (index < 0) return null;
        Student selected = students.get(index);
        selected.incrementCalled();
        dao.saveStudents(students);
        return selected;
    }

    /** 记录回答结果 */
    public void recordAnswer(Student student, boolean answered) {
        reload();
        for (Student s : students) {
            if (s.getStudentNo().equals(student.getStudentNo())) {
                if (answered) s.incrementAnswered();
                break;
            }
        }

        RollCallRecord record = new RollCallRecord(
                student.getStudentNo(), student.getName(),
                currentCourse, answered);
        records.add(record);
        dao.saveRecords(records);

        int idx = findIndex(student.getStudentNo());
        if (answered) {
            unansweredList.remove((Integer) idx);
            if (idx >= 0 && !answeredThisRound.contains(idx)) {
                answeredThisRound.add(idx);
            }
            // 答出了 → 退出救场，重置计数
            inRescue = false;
            rescueCallCount = 0;
            rescueFailCount = 0;
        } else {
            if (idx >= 0 && !unansweredList.contains(idx)) {
                unansweredList.add(idx);
            }
            // 救场模式下失败 → 累计
            if (inRescue) {
                rescueFailCount++;
            }
            // 救场3轮结束 → 退出救场模式（等待下次触发）
            if (rescueCallCount >= RESCUE_ROUNDS) {
                inRescue = false;
                rescueCallCount = 0;
                // rescueFailCount 保留给 isTooHard() 判断
            }
        }
        dao.saveStudents(students);
    }

    private int findIndex(String studentNo) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getStudentNo().equals(studentNo)) return i;
        }
        return -1;
    }
}
