package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自动排课结果摘要。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutoSchedulingResult {

    /**
     * 成功排课的任务数
     */
    private int successCount;

    /**
     * 排课失败的任务数
     */
    private int failCount;

    /**
     * 总任务数
     */
    private int totalCount;
}
