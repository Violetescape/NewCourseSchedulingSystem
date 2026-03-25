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
     * 起始课程
     */
    private Integer section;

    /**
     * 第几周
     */
    private Integer week;

    /**
     * 单次授课课时数（用于课表前端的跨行合并）
     */
    private Integer courseSingleHour;

    /**
     * 课程起始周（来自 course 表）
     */
    private Integer courseStartWeek;

    /**
     * 课程结束周（来自 course 表）
     */
    private Integer courseEndWeek;

    /**
     * 课程 ID（用于前端聚合去重）
     */
    private Integer scheduleCourseId;

    /**
     * 教室 ID（用于前端聚合去重）
     */
    private Integer scheduleClassroomId;
}

