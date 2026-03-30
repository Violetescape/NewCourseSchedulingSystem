package violet.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class TeachingTaskExcelDTO {

    @ExcelProperty("教师姓名")
    private String teacherName;

    @ExcelProperty("课程名称")
    private String courseName;

    @ExcelProperty("班级名称")
    private String className;
}
