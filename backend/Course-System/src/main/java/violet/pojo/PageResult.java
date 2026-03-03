package violet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页结果封装
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {

    // 总记录数
    private Long total;

    // 当前页数据列表
    private List<T> rows;
}

