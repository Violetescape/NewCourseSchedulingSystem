package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程实体类，对应表：course。
 * <p>
 * 数据库下划线/大写字段映射为 Java 驼峰字段，例如 Course_ID -> courseId。
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Course {

    /**
     * 课程编号（Primary Key，手动传入）
     */
    private Integer courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程类型：1-必修, 2-专业选修, 3-公选
     */
    private Integer courseType;

    /**
     * 开课起始周
     */
    private Integer courseStartWeek;

    /**
     * 结课结束周
     */
    private Integer courseEndWeek;

    /**
     * 单次授课课时数
     */
    private Integer courseSingleHour;
}

