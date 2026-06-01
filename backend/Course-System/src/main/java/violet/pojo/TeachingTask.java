package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教学任务实体类，对应表：teaching_task。
 * <p>
 * 数据库字段映射：
 * Task_ID    -> taskId
 * Teacher_ID -> teacherId
 * Course_ID  -> courseId
 * Class_Ids  -> classIds（逗号分隔的班级 ID，如 "1,2,3"）
 * Task_State -> taskState
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeachingTask {

    /**
     * 教学任务编号（Primary Key，自增）
     */
    private Integer taskId;

    /**
     * 授课教师 ID
     */
    private Integer teacherId;

    /**
     * 授课课程 ID
     */
    private Integer courseId;

    /**
     * 上课班级 ID 列表（逗号分隔，如 "1,2,3"）
     */
    private String classIds;

    /**
     * 任务状态：未排课 / 已排课
     */
    private String taskState;
}
