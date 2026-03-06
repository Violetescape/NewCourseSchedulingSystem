package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教学任务视图对象。
 * <p>
 * 在 TeachingTask 基础上，增加教师姓名、课程名称、班级名称三个字段，
 * 用于前端表格联合展示。
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeachingTaskVO {

    // ========== 原表字段 ==========

    /**
     * 教学任务编号
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
     * 上课班级 ID
     */
    private Integer classId;

    /**
     * 任务状态：未排课 / 已排课
     */
    private String taskState;

    // ========== 关联展示字段 ==========

    /**
     * 教师姓名（来自表 teacher）
     */
    private String teacherName;

    /**
     * 课程名称（来自表 course）
     */
    private String courseName;

    /**
     * 班级名称（来自表 class）
     */
    private String className;
}

