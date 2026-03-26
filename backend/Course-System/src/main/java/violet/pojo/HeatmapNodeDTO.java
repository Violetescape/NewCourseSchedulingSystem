package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热力图节点 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapNodeDTO {
    /** 0-4 代表周一到周五 */
    private Integer weekday;
    /** 0-11 代表第1-12节 */
    private Integer section;
    /** 占用数量（占用教室数量） */
    private Integer occupiedCount;
}
