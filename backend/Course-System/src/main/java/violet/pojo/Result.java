package violet.pojo;

import lombok.Data;

/**
 * 统一结果返回类
 */
@Data
public class Result {

    private Integer code;    // 状态码：1 成功, 0 失败
    private String msg;      // 提示信息
    private Object data;     // 返回的数据

    // 成功静态方法(无数据返回)
    public static Result success() {
        Result result = new Result();
        result.code = 1;
        result.msg = "success";
        return result;
    }

    // 成功静态方法 (返回数据)
    public static Result success(Object object) {
        Result result = new Result();
        result.data = object;
        result.code = 1;
        result.msg = "success";
        return result;
    }

    // 失败静态方法
    public static Result error(String msg) {
        Result result = new Result();
        result.code = 0;
        result.msg = msg;
        return result;
    }
}