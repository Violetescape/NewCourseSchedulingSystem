package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课表查询视图对象（按周查询）。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleVO {

    /**
     * 排课记录编号
     */
    private Integer scheduleId;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 教室名称
     */
    private String classroomName;

    /**
     * 周几（1-7）
     */
    private Integer weekday;

    /**
     * 第几节课（如 1 代表 1-2 节）
     */
    private Integer section;

    /**
     * 第几周
     */
    private Integer week;
}

