package rollcall.service;

import rollcall.dao.StudentDAO;
import rollcall.model.RollCallRecord;
import rollcall.model.Student;

import java.util.*;

/**
 * 统计服务 —— 汇总计算、频次分布
 */
public class StatisticsService {

    private final StudentDAO dao = new StudentDAO();

    public List<Student> getStudents() { return dao.loadStudents(); }

    public Map<String, Object> getSummary() {
        List<Student> students = dao.loadStudents();
        List<RollCallRecord> records = dao.loadRecords();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalStudents", students.size());
        map.put("totalCalls", records.size());
        long answered = records.stream().filter(RollCallRecord::isAnswered).count();
        map.put("totalAnswered", answered);
        double rate = records.isEmpty() ? 0 : (double) answered / records.size() * 100;
        map.put("overallRate", String.format("%.1f", rate));
        return map;
    }

    /** 点名频次分布 */
    public List<String[]> getFrequency() {
        List<Student> students = dao.loadStudents();
        Map<String, Integer> dist = new LinkedHashMap<>();
        dist.put("0次", 0); dist.put("1~3次", 0);
        dist.put("4~6次", 0); dist.put("7~10次", 0);
        dist.put("10次以上", 0);

        for (Student s : students) {
            int n = s.getTotalCalled();
            if (n == 0) dist.merge("0次", 1, Integer::sum);
            else if (n <= 3) dist.merge("1~3次", 1, Integer::sum);
            else if (n <= 6) dist.merge("4~6次", 1, Integer::sum);
            else if (n <= 10) dist.merge("7~10次", 1, Integer::sum);
            else dist.merge("10次以上", 1, Integer::sum);
        }
        List<String[]> result = new ArrayList<>();
        for (Map.Entry<String, Integer> e : dist.entrySet()) {
            result.add(new String[]{e.getKey(), String.valueOf(e.getValue())});
        }
        return result;
    }
}
