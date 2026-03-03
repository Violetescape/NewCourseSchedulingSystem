package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 班级实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Clazz {
    // 班级编号 (Primary Key)
    private Integer classId;

    // 班级名称
    private String className;

    // 专业
    private String classMajor;

    // 班级年级
    private Integer classGrade;

    // 班级人数
    private Integer classNum;

    // 班级院系
    private String classDepartment;
}