package com.tonghua.risk.interface.dto;

import lombok.Data;

/**
 * 统一响应结果
 *
 * @param <T> 数据类型
 */
@Data
public class ResultVO<T> {

    /** 状态码，200-成功，其他-失败 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 构造成功结果 */
    public static <T> ResultVO<T> success(T data) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /** 构造成功结果（无数据） */
    public static <T> ResultVO<T> success() {
        return success(null);
    }

    /** 构造成功结果（带提示信息） */
    public static <T> ResultVO<T> success(String message, T data) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /** 构造失败结果 */
    public static <T> ResultVO<T> fail(String message) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /** 构造失败结果（自定义状态码） */
    public static <T> ResultVO<T> fail(int code, String message) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
