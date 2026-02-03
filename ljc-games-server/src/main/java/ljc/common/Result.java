package ljc.common;

import lombok.Data;

/**
 * 统一API响应结果封装
 * @param <T> 这里 T 代表里面的数据类型（可能是 User，也可能是 String，由你决定）
 */
@Data
public class Result<T> {
    private Integer code; // 业务状态码 (200=成功)
    private String message; // 提示信息
    private T data;       // 数据本体

    // 1. 快速创建一个成功的盒子（不带数据）
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    // 2. 快速创建一个成功的盒子（带数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    // 3. 快速创建一个失败的盒子
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500); // 暂时统一定义错误码为 500
        result.setMessage(msg);
        return result;
    }
}