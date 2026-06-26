package rollcall.util;

import rollcall.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 加权随机工具类 —— 策略模式核心算法
 * 被点名次数越少的学生权重越高，确保机会均等
 */
public class WeightedRandom {

    private static final Random RANDOM = new Random();

    /**
     * 加权随机选择
     * @param students 学生列表
     * @param excludeIndices 本轮已未答出的学生索引
     * @return 被选中学生的索引
     */
    public static int select(List<Student> students, List<Integer> excludeIndices) {
        if (students.isEmpty()) return -1;

        double[] weights = new double[students.size()];
        double totalWeight = 0.0;

        for (int i = 0; i < students.size(); i++) {
            double w = students.get(i).getCallWeight();
            if (excludeIndices != null && excludeIndices.contains(i)) {
                w *= 0.01;
            }
            weights[i] = Math.max(w, 0.01);
            totalWeight += weights[i];
        }

        double random = RANDOM.nextDouble() * totalWeight;
        double cumulative = 0.0;
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (random <= cumulative) return i;
        }
        return weights.length - 1;
    }

    /**
     * 救场机制：从高回答率学生中随机选取
     */
    public static int selectTop(List<Student> students) {
        double maxRate = -1;
        for (Student s : students) {
            if (s.getAnswerRate() > maxRate) maxRate = s.getAnswerRate();
        }

        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getAnswerRate() >= maxRate * 0.8
                    && students.get(i).getTotalCalled() > 0) {
                candidates.add(i);
            }
        }
        if (candidates.isEmpty()) {
            return RANDOM.nextInt(students.size());
        }
        return candidates.get(RANDOM.nextInt(candidates.size()));
    }
}
