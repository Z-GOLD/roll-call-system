package rollcall.dao;

import rollcall.model.Student;
import rollcall.model.RollCallRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据访问层 —— DAO模式（面向接口设计）
 * 使用对象流实现文件持久化（第12章 输入输出流）
 */
public class StudentDAO {

    private static final String STUDENT_FILE = "students.dat";
    private static final String RECORD_FILE = "records.dat";

    @SuppressWarnings("unchecked")
    public List<Student> loadStudents() {
        File file = new File(STUDENT_FILE);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(file))) {
            return (List<Student>) in.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void saveStudents(List<Student> students) {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(STUDENT_FILE))) {
            out.writeObject(students);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public List<RollCallRecord> loadRecords() {
        File file = new File(RECORD_FILE);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(file))) {
            return (List<RollCallRecord>) in.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void saveRecords(List<RollCallRecord> records) {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(RECORD_FILE))) {
            out.writeObject(records);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
