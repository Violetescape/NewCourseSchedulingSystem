package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 手动指定时间与教室排课的请求体。
 * <p>
 * 起止教学周若为空，则由服务端按课程默认开课周次填充。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualScheduleDTO {

    private Integer taskId;
    private Integer classroomId;
    /** 星期：1-7（周一至周日），与课表网格一致 */
    private Integer weekday;
    /** 起始节次 */
    private Integer section;
    /** 教学周起始（可选） */
    private Integer startWeek;
    /** 教学周结束（可选） */
    private Integer endWeek;
}
