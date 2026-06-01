package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 手动排课冲突校验结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualScheduleValidateResult {

    private boolean ok;
    private String message;
}
