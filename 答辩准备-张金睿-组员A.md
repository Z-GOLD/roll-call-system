# 🎤 答辩准备 — 张金睿（组员 A：数据管理与持久化）

---

## 第一部分：我负责的模块详细讲解（重点）

> **我的角色**：组员 A — 数据管理与持久化  
> **负责文件**：`Student.java`、`RollCallRecord.java`、`StudentDAO.java`、`ExcelUtil.java`、`StudentPanel.java`（共 5 个文件，约占项目总代码量的 60%）

---

### 1.1 数据模型层（Model）

#### Student.java — 学生实体类

```java
public class Student implements Serializable {
    // 核心字段
    private String studentNo;     // 学号
    private String name;          // 姓名
    private String className;     // 班级
    private int totalCalled;      // 累计被点名次数
    private int totalAnswered;    // 累计答出次数
    private boolean onLeave;      // 请假状态
```

**讲解要点**：

1. **实现 `Serializable` 接口的原因**：因为我们采用 Java 对象序列化来持久化数据（把整个 List 对象写入 .dat 文件），所以 Student 必须实现 Serializable。`serialVersionUID = 1L` 是序列化的版本标识符，确保反序列化时类定义匹配。

2. **核心方法 — `getCallWeight()`**：
   ```java
   public double getCallWeight() {
       return 100.0 / (totalCalled + 1);
   }
   ```
   这是加权随机算法的核心公式。返回值随被点名次数增加而递减：
   - 被点名 0 次 → 权重 100
   - 被点名 1 次 → 权重 50
   - 被点名 2 次 → 权重 33.3
   - 被点名 9 次 → 权重 10
   
   分母 `+1` 是为了防止除以零。这个设计体现了设计模式中的「高内聚」原则——权重计算逻辑封装在模型内部，而非散落在业务代码中。

3. **核心方法 — `getAnswerRate()`**：
   ```java
   public double getAnswerRate() {
       if (totalCalled == 0) return 0.0;  // 未被点名过，返回 0
       return (double) totalAnswered / totalCalled * 100;
   }
   ```
   返回百分比形式的答出率，用于统计面板展示和救场机制的候选人筛选。

4. **`incrementCalled()` 和 `incrementAnswered()`**：简单的计数器递增方法，由 RollCallService 在点名和记录回答时调用。

5. **`onLeave` 请假标记**：布尔值，请假的学生在点名时会被自动排除，且在表格中显示"请假"状态。

---

#### RollCallRecord.java — 点名记录实体类

```java
public class RollCallRecord implements Serializable {
    private String studentNo;     // 学号
    private String studentName;   // 姓名
    private String courseName;    // 课程名称
    private boolean answered;     // 是否答出
    private String callTime;      // 点名时间
```

**讲解要点**：

1. **同样实现 `Serializable`**：记录也需要持久化到 `records.dat` 文件中。

2. **时间戳自动生成**：
   ```java
   this.callTime = LocalDateTime.now()
           .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
   ```
   在构造时自动获取当前时间并格式化为 `YYYY-MM-DD HH:mm:ss` 格式，无需调用者手动传入。使用了 Java 8 的 `LocalDateTime`，替代了老的 `java.util.Date`。

3. **每个字段对应课堂点名的一个维度**：谁（studentName）、什么课（courseName）、答出没（answered）、什么时候（callTime），这是一个纯数据载体（POJO），符合 JavaBean 规范。

---

### 1.2 数据访问层（DAO）

#### StudentDAO.java — 核心持久化层

```java
public class StudentDAO {
    private static final String STUDENT_FILE = "students.dat";
    private static final String RECORD_FILE = "records.dat";
```

**讲解要点（这是答辩重点）**：

1. **DAO 设计模式**：Data Access Object 模式，将所有数据读写操作集中在一个类中，上层 Service 不需要知道数据是怎么存的。如果我将来把文件存储换成 MySQL 数据库，只需要改这个类，Service 和 View 代码完全不用动。这就是「面向接口编程」和「开闭原则」的体现。

2. **文件不存在时返回空列表**：
   ```java
   public List<Student> loadStudents() {
       File file = new File(STUDENT_FILE);
       if (!file.exists()) return new ArrayList<>();  // 首次运行不报错
   ```
   这是必要的防御性编程——系统第一次运行时 `students.dat` 不存在，不应该报错，而是返回空列表。

3. **try-with-resources 自动关闭流**：
   ```java
   try (ObjectInputStream in = new ObjectInputStream(
           new FileInputStream(file))) {
       return (List<Student>) in.readObject();
   ```
   这是 Java 7 引入的语法糖。`try()` 括号内的资源实现了 `AutoCloseable` 接口，无论 try 块正常执行还是抛异常，流都会自动关闭，不需要写 `finally { in.close(); }`。对应教材第 12 章「输入输出流」的知识点。

4. **对象流实现深度持久化**：
   - `ObjectOutputStream.writeObject()` — 将整个 `List<Student>` 对象图完整写入文件
   - `ObjectInputStream.readObject()` — 读取时自动重建整个对象图，包括所有字段值
   - 对比 CSV：CSV 只能存文本，不能存复杂对象；对象流可以一键存取，代码极简

5. **`@SuppressWarnings("unchecked")` 注解**：`readObject()` 返回 `Object` 类型，我们强制转换为 `List<Student>`，编译器会警告 unchecked cast。加这个注解告诉编译器：我们知道自己在做什么。

6. **CRUD 操作的设计模式**：
   - `loadStudents()` / `saveStudents()` — 学生数据的读取和写入
   - `loadRecords()` / `saveRecords()` — 点名记录的读取和写入
   - 所有保存操作都完整覆盖原文件（`FileOutputStream` 默认覆盖模式），不是追加

7. **数据存储位置**：`.dat` 文件存储在当前工作目录（项目根目录），不需要配置额外路径。

**答辩话术模板**：
> "这套持久化方案虽然简单，但设计上完全遵循了 DAO 模式。将来如果要迁移到数据库，只需要修改 StudentDAO 中的 4 个方法，其他 10 个 Java 文件完全不需要改动。这就是分层架构的价值所在。"

---

### 1.3 Excel 工具类

#### ExcelUtil.java — Apache POI 读写 Excel

```java
public class ExcelUtil {
    // 导入
    public static List<Student> importFromExcel(File file) throws IOException { ... }
    // 导出
    public static void exportToExcel(File file, List<Student> students) throws IOException { ... }
    // 单元格值提取
    private static String getCellValue(Cell cell) { ... }
```

**讲解要点**：

1. **第三方库 Apache POI**：
   - 项目核心功能只用 Java 标准库，Excel 读写是唯一引入第三方库的地方
   - 引用了 `poi-5.3.0.jar`、`poi-ooxml-5.3.0.jar` 等共 8 个 jar 包
   - `XSSFWorkbook` 专门处理 `.xlsx` 格式（Excel 2007+）

2. **`importFromExcel()` 方法逐行讲解**：
   ```java
   try (FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new XSSFWorkbook(fis)) {
   ```
   同样使用 try-with-resources 管理资源。

   ```java
   Sheet sheet = workbook.getSheetAt(0);
   for (int i = 1; i <= sheet.getLastRowNum(); i++) {
   ```
   从第 1 行开始（`i=1`），跳过第 0 行的标题行（标题行：学号、姓名、班级）。

   ```java
   Row row = sheet.getRow(i);
   if (row == null) continue;  // 跳过空行
   ```
   防御性编程，防止空行导致 NullPointerException。

   ```java
   String no = getCellValue(row.getCell(0));   // 第1列：学号
   String name = getCellValue(row.getCell(1)); // 第2列：姓名
   String cls = getCellValue(row.getCell(2));  // 第3列：班级
   if (no.isEmpty() && name.isEmpty()) continue;  // 空行跳过
   students.add(new Student(no, name, cls.isEmpty() ? "未分班" : cls));
   ```
   三元运算符给未填班级的学生赋默认值"未分班"。

3. **`getCellValue()` — 多类型单元格处理**：
   ```java
   switch (cell.getCellType()) {
       case STRING:  return cell.getStringCellValue().trim();
       case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
       // 学号可能以数字形式存，转为 long 再转字符串，防止科学计数法
       case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
       default:      return "";
   }
   ```
   为什么需要这个 switch？Excel 单元格有 5 种类型（STRING / NUMERIC / BOOLEAN / FORMULA / BLANK）。如果学号"2021001"被 Excel 存成了数字，直接 `getStringCellValue()` 会抛异常。这里做了类型适配。

4. **`exportToExcel()` 方法**：
   ```java
   CellStyle headerStyle = workbook.createCellStyle();
   Font headerFont = workbook.createFont();
   headerFont.setBold(true);
   headerStyle.setFont(headerFont);
   ```
   使用 POI 的样式 API 给标题行加粗。数据行包含学号、姓名、班级、被点名次数、答出次数、成功率。

   ```java
   sheet.autoSizeColumn(i);  // 自动调整列宽
   ```
   导出后列宽自适应内容，提升用户体验。

5. **全部是 static 方法**：ExcelUtil 不需要实例化，因为它不持有状态，纯粹是工具方法。这是工具类的标准做法。

---

### 1.4 学生管理面板（View 层）

#### StudentPanel.java — GUI 交互界面

```java
public class StudentPanel extends JPanel {
    // 7 个操作按钮 + 搜索栏 + 请假管理 + JTable 表格
```

**讲解要点**：

1. **组件结构**：
   - **顶部按钮栏**：添加学生 / 批量添加 / 导入表格 / 导出表格 / 删除选中 / 清空全部 / 刷新（7 个 JButton）
   - **搜索栏**：搜索输入框 + 搜索按钮 + 显示全部按钮 + 设为请假 / 销假按钮
   - **中央表格**：JTable + DefaultTableModel，列：序号/学号/姓名/班级/状态/被点名/答出/成功率

2. **`DefaultTableModel` 不可编辑**：
   ```java
   tableModel = new DefaultTableModel(cols, 0) {
       public boolean isCellEditable(int r, int c) { return false; }
   };
   ```
   通过匿名内部类重写 `isCellEditable()` 返回 `false`，禁止用户直接在表格中修改数据。所有修改通过按钮+对话框进行，保证数据一致性。

3. **搜索功能实现**：
   ```java
   if (keyword != null && !keyword.trim().isEmpty()) {
       String kw = keyword.trim().toLowerCase();
       if (!s.getStudentNo().toLowerCase().contains(kw)
               && !s.getName().toLowerCase().contains(kw)
               && !s.getClassName().toLowerCase().contains(kw)) {
           continue;  // 不匹配则跳过，不加入表格
       }
   }
   ```
   模糊搜索：用户输入的关键词只要匹配学号、姓名或班级中的任意一个字段即可。`toLowerCase()` 实现不区分大小写。

4. **批量添加 — 字符串分割**：
   ```java
   String[] parts = line.split("[\\t ,，]+");
   ```
   正则表达式 `[\\t ,，]+` 同时支持 Tab 键、英文逗号、中文逗号、空格作为分隔符，一个或多个连续分隔符视为一个。这大大提升了使用的容错性。

5. **导入表格 — FileDialog 文件选择**：
   ```java
   FileDialog dialog = new FileDialog((Frame) SwingUtilities.getWindowAncestor(this),
           "选择要导入的Excel表格文件", FileDialog.LOAD);
   dialog.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".xlsx") || ...);
   ```
   使用 AWT 的 `FileDialog`（而非 `JFileChooser`），因为它会调用系统原生文件对话框，用户更熟悉。文件名过滤器只显示 Excel 文件。

   ```java
   catch (Exception ex) {
       StringWriter sw = new StringWriter();
       ex.printStackTrace(new PrintWriter(sw));
       JOptionPane.showMessageDialog(this, "导入失败:\n" + ex.toString() + "\n\n" + sw.toString());
   }
   ```
   完整的异常信息输出，方便排查问题（比如 Excel 格式不对、文件被占用等）。

6. **请假管理**：
   ```java
   private void setLeave(boolean leave) {
       int row = table.getSelectedRow();
       if (row < 0) {
           JOptionPane.showMessageDialog(this, "请先在表格中选中一名学生");
           return;
       }
       // 找到学生并修改状态
       s.setOnLeave(leave);
       dao.saveStudents(students);
   ```
   请假学生在点名时自动排除（`RollCallService.call()` 中检测 `isOnLeave()`）。

7. **表格刷新机制**：每次增删改操作后都调用 `refreshTable()`，从 DAO 重新加载最新数据并重建表格行。这保证了界面始终和持久化数据保持同步。

8. **依赖关系**：StudentPanel 依赖 `RollCallService`（传入构造函数）和 `StudentDAO`（内部创建）。面板只负责界面交互，数据操作委托给 DAO 和 ExcelUtil。

---

## 第二部分：整体项目简要讲解

### 2.1 项目架构（MVC）

```
┌─── View 层 ─────────────────────┐
│  MainFrame (JTabbedPane 主窗口)  │
│  ├─ StudentPanel  (我负责任)     │
│  ├─ RollCallPanel  (组长负责)    │
│  └─ StatisticsPanel (组员B负责)  │
├─── Service 层 ───────────────────┤
│  ├─ RollCallService  (组长)      │
│  └─ StatisticsService (组员B)    │
├─── DAO 层 ───────────────────────┤
│  └─ StudentDAO  (我负责)         │
├─── Model 层 ─────────────────────┤
│  ├─ Student  (我负责)            │
│  └─ RollCallRecord  (我负责)     │
└─── Util 层 ──────────────────────┤
   ├─ WeightedRandom  (组长)       │
   └─ ExcelUtil  (我负责)          │
```

### 2.2 组长负责模块简述

| 文件 | 功能 | 一句话 |
|------|------|--------|
| `MainFrame.java` | 主窗口入口 | JTabbedPane 组织三个面板，设置系统外观 |
| `RollCallService.java` | 核心点名服务 | 调度加权随机 + 救场机制状态机 |
| `RollCallPanel.java` | 点名交互界面 | 48 号大字显示姓名，答出/未答出按钮，历史记录面板 |
| `WeightedRandom.java` | 加权随机算法 | 累积权重法 + 救场 selectTop |

**关键逻辑**：`RollCallService.call()` 方法：
```java
if (inRescue || unansweredList.size() >= RESCUE_THRESHOLD) {
    index = WeightedRandom.selectTop(students);  // 救场模式
} else {
    index = WeightedRandom.select(students, allExclude);  // 正常模式
}
```
连续 3 人未答出 → 切换救场模式 → 从最高答出率学生中选取 → 救场 3 连败 → 提示"题目太难"。

### 2.3 组员 B 负责模块简述

| 文件 | 功能 | 一句话 |
|------|------|--------|
| `StatisticsService.java` | 统计服务 | 汇总指标计算 + 频次分布统计 |
| `StatisticsPanel.java` | 统计界面 | 4 张汇总卡片 + 排名表 |

---

## 第三部分：模拟答辩提问（基于组员 A）

以下按提问概率从高到低排列，请逐题准备。

---

### Q1：为什么选择 Java 对象序列化而不是数据库？

**参考答案**：
> 课程设计要求使用教材第 12 章「输入输出流」的知识点。Java 对象序列化是一个很好的应用场景——它可以将整个对象图（List\<Student\> + 每个 Student 的所有字段）一键写入文件。相比数据库需要安装 MySQL、建表、写 SQL、配置 JDBC，对象流的方案做到了零配置、开箱即用。而且我们的 DAO 层已经做了抽象——将来如果要迁移到数据库，只需修改 StudentDAO 的 4 个方法，其他层完全不受影响。

---

### Q2：`students.dat` 文件损坏了怎么办？如何备份和恢复？

**参考答案**：
> 一方面，Excel 导出功能就是一个天然的备份手段——用户可以随时将数据导出为 .xlsx 文件保存。另一方面，`students.dat` 是二进制文件，不能直接编辑，但可以直接复制备份。如果要重置数据，删除这两个 .dat 文件即可（首次运行会自动创建空列表）。在 `loadStudents()` 方法中也有异常处理——如果反序列化失败，会返回空列表而不会崩溃。

---

### Q3：你的 `getCellValue()` 方法为什么要用 switch？

**参考答案**：
> Excel 单元格有 STRING、NUMERIC、BOOLEAN、FORMULA、BLANK 五种类型。如果学号"2021001"在 Excel 中被存成了数字类型，直接调 `getStringCellValue()` 会抛出 `IllegalStateException`。我的 switch 做了类型适配——数字类型先转 long 再转 String，避免了科学计数法问题。FORMULA 和 BLANK 类型返回空字符串，保证程序不会因为单个单元格的格式问题而崩溃。

---

### Q4：批量添加时如果用户输入格式不对怎么办？

**参考答案**：
> 我用了正则表达式 `split("[\\t ,，]+")` 做分割，支持 Tab、英文逗号、中文逗号、空格等多种分隔符，容错性比较高。分割后检查 `parts.length >= 2`，不足 2 个字段的行直接跳过。但如果用户输入的学号和姓名顺序反了，目前代码无法自动纠正——这确实是一个改进方向，可以考虑增加列映射配置。

---

### Q5：为什么要让 `DefaultTableModel` 不可编辑？

**参考答案**：
> 如果允许用户直接在表格单元格中修改数据，可能会出现数据格式错误（比如学号被改成了非法值），而且修改后没有经过 DAO 的 save 流程，数据和文件不一致。我的设计是：所有修改操作必须通过按钮 → 弹出对话框 → 校验输入 → 调用 DAO.save，保证数据完整性和一致性。

---

### Q6：你的 `importCSV()` 方法命名为什么叫 CSV 但实际导入的是 Excel？

**参考答案**：
> 这个确实是命名上的小问题。最初设计时考虑支持 CSV 文件导入，后来因为 Apache POI 能更好地处理中文编码和格式，最终改成了 Excel 导入。但方法名没来得及同步修改。这是我代码中的一个改进点。（面试官会觉得你诚实且有条理）

---

### Q7：`ExcelUtil` 为什么全部用 static 方法？

**参考答案**：
> `ExcelUtil` 是一个纯粹的工具类，它不保存任何状态（没有实例变量），所有操作都通过参数传入、通过返回值传出。这种类适合做成 static 工具方法，使用时不需要 `new ExcelUtil()`，直接 `ExcelUtil.importFromExcel(file)` 即可。这也是 `java.lang.Math`、`java.util.Collections` 等 JDK 工具类的标准做法。

---

### Q8：你的代码有哪些可以改进的地方？

**参考答案**：
> 1. **文件名硬编码**：`STUDENT_FILE = "students.dat"` 写死了，可以改为通过配置文件或用户选择路径。
> 2. **大数据量性能**：目前把所有学生一次性加载到内存，如果上万学生可能会有内存压力，可以考虑分页加载。
> 3. **并发安全**：目前没有考虑多线程场景，DAO 的读写操作不是线程安全的。
> 4. **方法命名**：`importCSV()` 实际导入 Excel，名称不匹配。
> 5. **异常处理**：部分 catch 只调了 `printStackTrace()`，生产环境应该用日志框架（如 log4j）。

---

### Q9：你从这门课学到了什么？（大概率会问）

**参考答案**：
> 通过这次课程设计，我将教材中学到的多个知识点串了起来：
> - **第 12 章 I/O 流**：对象流的实际应用
> - **第 10 章 Swing**：完整桌面 GUI 的开发
> - **设计模式**：DAO 模式、策略模式、MVC 架构
> - **第三方库集成**：Apache POI 的引入和异常处理
> - **Git 版本控制**：团队协作的必备技能
> 
> 更重要的是，我第一次以"模块负责人"的角色参与了一个完整的软件项目，体会到了接口约定和分层架构在团队协作中的重要性。

---

### Q10：如果让你给学弟学妹讲这节课设，你会强调什么？

**参考答案**：
> 1. **先约定接口，再各自开发**：我们三个模块之所以能无缝拼接，靠的就是一开始约定好了 Service 和 DAO 的方法签名。
> 2. **分层的好处**：我写 StudentDAO 的时候完全不知道 View 长什么样，但数据流一跑通就全通了。
> 3. **Git 是救命稻草**：每次改代码前先 commit，出问题了能回滚。
> 4. **别追求完美**：先跑通，再优化。加功能比调细节更重要。

---

## 答辩要点速记卡

| 序号 | 关键概念 | 一句话 |
|:----:|----------|--------|
| 1 | Serializable | 让对象能写入文件的接口 |
| 2 | DAO 模式 | 数据访问集中管理，换数据库不改上层 |
| 3 | try-with-resources | 自动关闭流，防止资源泄漏 |
| 4 | 对象流 | 一键存取整个对象图 |
| 5 | 加权权重公式 | 100/(被点名次数+1) |
| 6 | Apache POI | Java 读写 Excel 的第三方库 |
| 7 | 单元格类型适配 | STRING/NUMERIC/BOOLEAN 分别处理 |
| 8 | 防御性编程 | 文件不存在？返回空列表。空行？跳过。 |
| 9 | 正则分割 | `split("[\\t ,，]+")` 容错多种分隔符 |
| 10 | MVC 分层 | Model-View-Controller 各司其职 |
