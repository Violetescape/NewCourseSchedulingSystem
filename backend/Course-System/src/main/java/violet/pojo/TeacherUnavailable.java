package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教师不可排课时间实体类，对应表：teacher_unavailable。
 * <p>
 * 字段映射：
 * Un_ID      -> unId
 * Teacher_ID -> teacherId
 * Un_Week    -> unWeek
 * Un_Weekday -> unWeekday
 * Un_Section -> unSection
 * Un_Remark  -> unRemark
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherUnavailable {

    /**
     * 记录编号（Primary Key，自增）
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
     * 不可排课原因（如：教研会议、出差）
     */
    private String unRemark;
}

