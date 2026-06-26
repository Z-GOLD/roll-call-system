# 📋 课堂点名系统 v1.0

基于 Java Swing 的课堂随机点名系统，支持加权随机算法、学生数据管理、统计分析及 Excel 导入导出。

## ✨ 功能特性

### 🎲 课堂点名
- **加权随机算法**：被点名次数越少的学生，被抽中的概率越高，确保机会公平
- **排除机制**：本轮已点名的学生不会被重复抽中
- **救场模式**：连续 3 人未答出时自动切换到救场策略
- **课堂记录**：自动记录每次点名结果（答出/未答出）

### 👥 学生管理
- 学生信息的增、删、改、查
- 支持 CSV 模板导入学生数据
- 支持 Excel 批量导入导出
- 学生列表可视化展示

### 📊 数据统计
- 点名次数统计与排名
- 答出率分析
- 课堂表现数据可视化

### 🏗️ 技术架构

- **架构模式**：MVC（Model-View-Controller）
- **核心算法**：策略模式 — `WeightedRandom` 加权随机选择
- **UI 框架**：Java Swing（JTabbedPane 三面板布局）
- **数据存储**：CSV 文件持久化

## 📁 项目结构

```
课堂点名系统/
├── src/rollcall/
│   ├── dao/               # 数据访问层
│   │   └── StudentDAO.java
│   ├── model/             # 数据模型
│   │   ├── Student.java
│   │   └── RollCallRecord.java
│   ├── service/           # 业务逻辑层
│   │   ├── RollCallService.java
│   │   └── StatisticsService.java
│   ├── util/              # 工具类
│   │   ├── WeightedRandom.java
│   │   └── ExcelUtil.java
│   └── view/              # 视图层（Swing 界面）
│       ├── MainFrame.java
│       ├── StudentPanel.java
│       ├── RollCallPanel.java
│       └── StatisticsPanel.java
├── doc/                   # 文档与数据模板
│   ├── 学生导入模板.csv
│   ├── 学生数据.csv
│   ├── 学生数据实验.xlsx
│   └── 系统使用说明书.docx
└── 分工/                  # 小组分工文档
```

## 🚀 运行环境

| 要求 | 说明 |
|------|------|
| JDK | 1.8 及以上 |
| IDE | IntelliJ IDEA、Eclipse 或任意 Java IDE |
| 依赖 | 纯标准库，无需第三方 JAR 包 |

## ▶️ 使用方法

1. **克隆项目**
   ```bash
   git clone https://github.com/Z-GOLD/roll-call-system.git
   ```

2. **导入 IDE**
   - 将 `src` 目录设为源码根目录
   - 确保 JDK 版本 ≥ 1.8

3. **运行程序**
   - 运行 `src/rollcall/view/MainFrame.java` 的 `main` 方法

4. **导入学生数据**
   - 打开程序 → 学生管理面板 → 导入 `doc/学生导入模板.csv`
   - 也可使用 Excel 导入

## 🎯 核心算法

### 加权随机选择

```java
// 权重计算：基础权重 + 衰减因子
// 被点名次数越少 → callWeight 越高 → 被选中概率越大
double w = student.getCallWeight();
```

### 救场机制

- 累计 3 人未答出 → 自动激活救场模式
- 救场模式下优先选择答出率高、次数少的学生
- 救场持续 3 轮后自动退出

## 👥 小组分工

| 角色 | 负责模块 |
|------|----------|
| 组长 | 核心点名逻辑 (RollCallService)、主界面 (MainFrame)、点名面板 (RollCallPanel) |
| 组员 A | 数据管理与持久化 (StudentDAO)、学生面板 (StudentPanel)、Excel 工具 |
| 组员 B | 统计服务 (StatisticsService)、统计面板 (StatisticsPanel)、数据分析 |

## 📝 许可证

本项目为课程作业项目，仅供学习参考。
