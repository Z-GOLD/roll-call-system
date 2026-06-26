# 组员B —— 统计与数据分析

## 我负责的文件（4个）

| 文件 | 类型 | 说明 |
|------|:--:|------|
| `model/Student.java` | 共享-使用 | 学生实体（需理解getAnswerRate()、getTotalCalled()） |
| `model/RollCallRecord.java` | 共享-使用 | 点名记录（需理解isAnswered()） |
| `service/StatisticsService.java` | **独立负责** | 汇总统计、频次分布、Stream API过滤 |
| `view/StatisticsPanel.java` | **独立负责** | 统计界面：四卡片+排名表+频次展示 |

## 答辩重点

1. **汇总计算**：totalStudents/calls/answered/overallRate
2. **频次分布**：LinkedHashMap保持顺序，按区间分组（0次/1~3/4~6/7~10/10次以上）
3. **排名表**：students.sort((a,b)->b-a)降序排列
4. **Stream API**：records.stream().filter().count() 统计答出次数
5. **界面设计**：GridLayout(1,4)卡片+JTable排名表

## 老师可能问

- Q: 为什么用LinkedHashMap而不是HashMap？A: 需要保持"0次→1~3次→..."的分组顺序
- Q: Lambda表达式的作用？A: 简化Comparator和Stream过滤的写法
- Q: 成功率怎么算？A: totalAnswered/totalCalled×100，分母为0时返回0
