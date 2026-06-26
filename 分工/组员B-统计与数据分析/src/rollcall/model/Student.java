package rollcall.model;

import java.io.Serializable;

public class Student implements Serializable {
    private static final long serialVersionUID = 1L;

    private String studentNo;
    private String name;
    private String className;
    private int totalCalled;
    private int totalAnswered;
    private boolean onLeave = false;

    public Student(String studentNo, String name, String className) {
        this.studentNo = studentNo;
        this.name = name;
        this.className = className;
        this.totalCalled = 0;
        this.totalAnswered = 0;
        this.onLeave = false;
    }

    public double getAnswerRate() {
        if (totalCalled == 0) return 0.0;
        return (double) totalAnswered / totalCalled * 100;
    }

    public double getCallWeight() {
        return 100.0 / (totalCalled + 1);
    }

    public String getStudentNo() { return studentNo; }
    public String getName() { return name; }
    public String getClassName() { return className; }
    public int getTotalCalled() { return totalCalled; }
    public void incrementCalled() { totalCalled++; }
    public int getTotalAnswered() { return totalAnswered; }
    public void incrementAnswered() { totalAnswered++; }
    public boolean isOnLeave() { return onLeave; }
    public void setOnLeave(boolean onLeave) { this.onLeave = onLeave; }
}
