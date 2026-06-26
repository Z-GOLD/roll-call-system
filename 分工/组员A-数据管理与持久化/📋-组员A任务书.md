# 组员A —— 数据管理与持久化

## 我负责的文件（5个）

| 文件 | 类型 | 说明 |
|------|:--:|------|
| `model/Student.java` | 共享-使用 | 学生实体（需理解序列化、字段含义） |
| `model/RollCallRecord.java` | 共享-使用 | 点名记录实体 |
| `dao/StudentDAO.java` | **独立负责** | DAO模式数据持久层：ObjectStream读写、异常处理 |
| `util/ExcelUtil.java` | **独立负责** | Apache POI读写.xlsx：导入跳过标题行、导出含样式 |
| `view/StudentPanel.java` | **独立负责** | 学生管理界面：增删改查、搜索、请假、Excel导入导出 |

## 答辩重点

1. **DAO模式**：接口分离→切换存储方式不改变上层代码
2. **对象序列化**：ObjectOutputStream/ObjectInputStream、implements Serializable
3. **Apache POI**：XSSFWorkbook读写.xlsx、Cell类型判断（STRING/NUMERIC/BOOLEAN）
4. **请假管理**：Student.onLeave字段标记、按钮切换状态、dao.saveStudents()持久化
5. **搜索过滤**：关键字匹配学号/姓名/班级（contains忽略大小写）

## 老师可能问

- Q: 数据文件损坏怎么办？A: load方法catch异常返回空列表，不崩溃
- Q: 为什么用序列化而不是CSV？A: 序列化保留对象结构，代码简洁
- Q: Excel导入跳过标题行怎么实现？A: for循环从i=1开始
