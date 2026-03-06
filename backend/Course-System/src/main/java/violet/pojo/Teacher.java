package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教师实体类，对应表：teacher。
 * <p>
 * 数据库下划线/大写字段映射为 Java 驼峰字段，例如 Teacher_ID -> teacherId。
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Teacher {

    /**
     * 教师编号（Primary Key，手动传入）
     */
    private Integer teacherId;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 所属院系/部门
     */
    private String teacherDepartment;

    // 当前数据库中未维护职称和状态字段，如后续需要再扩展。
}

