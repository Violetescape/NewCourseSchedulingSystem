package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教师不可排课时间视图对象。
 * <p>
 * 在 TeacherUnavailable 原有字段基础上，增加教师姓名 teacherName，
 * 用于前端表格展示。
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherUnavailableVO {

    // ====== 原表字段 ======

    /**
     * 记录编号
     */
    private Integer unId;

    /**
     * 教师编号
     */
    private Integer teacherId;

    /**
     * 第几周（为空表示每周）
     */
    private Integer unWeek;

    /**
     * 周几（1-7）
     */
    private Integer unWeekday;

    /**
     * 第几节课（如 1 代表 1-2 节）
     */
    private Integer unSection;

    /**
     * 不可排课原因
     */
    private String unRemark;

    // ====== 关联展示字段 ======

    /**
     * 教师姓名（来自 teacher.Teacher_Name）
     */
    private String teacherName;
}

