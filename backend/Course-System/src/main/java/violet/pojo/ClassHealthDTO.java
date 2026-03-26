package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 班级课表健康度 DTO（20周时序折线图）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassHealthDTO {
    private Integer classId;
    private String className;
    /** 1-20周每周健康度得分(0-100) */
    private List<Double> weeklyScores;
}

