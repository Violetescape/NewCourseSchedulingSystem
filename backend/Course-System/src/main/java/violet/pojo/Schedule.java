package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排课记录实体类，对应表：schedule。
 * <p>
 * 用于自动排课时的 INSERT 操作。
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Schedule {

    /**
     * 排课记录编号（Primary Key，自增，插入时可省略）
     */
    private Integer scheduleId;

    /**
     * 班级 ID
     */
    private Integer scheduleClassId;

    /**
     * 教师 ID
     */
    private Integer scheduleTeacherId;

    /**
     * 课程 ID
     */
    private Integer scheduleCourseId;

    /**
     * 教室 ID
     */
    private Integer scheduleClassroomId;

    /**
     * 周几（1-5 周一至周五）
     */
    private Integer scheduleWeekday;

    /**
     * 起始节次（如 1 表示第 1-2 节）
     */
    private Integer scheduleSection;

    /**
     * 第几周
     */
    private Integer scheduleWeek;
}
