package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教室效能双指标 DTO（散点图）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomEfficiencyDTO {
    private Integer classroomId;
    private String classroomName;
    /** 时间利用率(%) */
    private Double timeUtilization;
    /** 空间满载率(%) */
    private Double spaceLoadRate;
}

