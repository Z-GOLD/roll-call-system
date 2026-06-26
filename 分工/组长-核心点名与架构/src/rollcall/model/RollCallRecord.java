package rollcall.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RollCallRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String studentNo;
    private String studentName;
    private String courseName;
    private boolean answered;
    private String callTime;

    public RollCallRecord(String studentNo, String studentName,
                          String courseName, boolean answered) {
        this.studentNo = studentNo;
        this.studentName = studentName;
        this.courseName = courseName;
        this.answered = answered;
        this.callTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getStudentNo() { return studentNo; }
    public String getStudentName() { return studentName; }
    public String getCourseName() { return courseName; }
    public boolean isAnswered() { return answered; }
    public String getCallTime() { return callTime; }
}
