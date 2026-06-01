package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用「名称 + 数值」切片，用于饼图 / 折线图类目 / 柱状图等。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisSliceDTO {

    private String name;
    private Double value;
}
