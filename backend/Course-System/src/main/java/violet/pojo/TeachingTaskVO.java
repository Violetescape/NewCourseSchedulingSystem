package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教学任务视图对象。
 * <p>
 * 在 TeachingTask 基础上，增加教师姓名、课程名称、班级名称等字段，
 * 用于前端表格联合展示。合班模式下 classIds 为逗号分隔 ID，className / classNum 为聚合展示。
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
     * 上课班级 ID 列表（逗号分隔，如 "1,2,3"）
     */
    private String classIds;

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
     * 合班班级名称（多个班级名用顿号拼接）
     */
    private String className;

    /**
     * 合班总人数（各班级人数之和，用于教室容量匹配）
     */
    private Integer classNum;

    /**
     * 开课起始周（来自课程）
     */
    private Integer courseStartWeek;

    /**
     * 结课结束周（来自课程）
     */
    private Integer courseEndWeek;

    /**
     * 单次授课课时（连续节次数，来自课程）
     */
    private Integer courseSingleHour;

    /**
     * 课程所需教室类型（来自课程）
     */
    private String requiredClassroomType;
}
