package rollcall.util;

import rollcall.model.Student;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel工具类 —— 使用Apache POI读写.xlsx表格
 */
public class ExcelUtil {

    /** 从.xlsx文件导入学生列表 */
    public static List<Student> importFromExcel(File file) throws IOException {
        List<Student> students = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 跳过标题行
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String no = getCellValue(row.getCell(0));
                String name = getCellValue(row.getCell(1));
                String cls = getCellValue(row.getCell(2));

                if (no.isEmpty() && name.isEmpty()) continue;
                students.add(new Student(no, name, cls.isEmpty() ? "未分班" : cls));
            }
        }
        return students;
    }

    /** 导出统计数据到.xlsx文件 */
    public static void exportToExcel(File file, List<Student> students) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("点名统计");

            // 标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 标题行
            String[] titles = {"学号", "姓名", "班级", "被点名次数", "回答出次数", "成功率"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < titles.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(titles[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            int rowIdx = 1;
            for (Student s : students) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.getStudentNo());
                row.createCell(1).setCellValue(s.getName());
                row.createCell(2).setCellValue(s.getClassName());
                row.createCell(3).setCellValue(s.getTotalCalled());
                row.createCell(4).setCellValue(s.getTotalAnswered());
                row.createCell(5).setCellValue(String.format("%.1f%%", s.getAnswerRate()));
            }

            // 自动调整列宽
            for (int i = 0; i < titles.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default:      return "";
        }
    }
}
