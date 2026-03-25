package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排课原始记录 DTO，用于冲突检测模型初始化。
 * <p>
 * 从 schedule 表 LEFT JOIN course 表查询，获取每条排课记录及其课程跨度（courseSingleHour）。
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleRawDTO {

    /**
     * 教师 ID
     */
    private Integer teacherId;

    /**
     * 班级 ID
     */
    private Integer classId;

    /**
     * 教室 ID
     */
    private Integer classroomId;

    /**
     * 周几（1-5）
     */
    private Integer weekday;

    /**
     * 起始节次
     */
    private Integer section;

    /**
     * 第几周
     */
    private Integer week;

    /**
     * 单次授课课时数（连续占用节次数）
     */
    private Integer courseSingleHour;
}
