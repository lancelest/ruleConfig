package com.risk.exception;

import com.risk.dto.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * 统一拦截所有异常，返回标准 ResultVO 格式的错误信息。
 * Controller 层不需要写任何 try-catch，所有异常都会在这里被兜底。
 *
 * 优先级从高到低：
 * 1. BusinessException — 可预期的业务错误（规则不存在、重复创建等）
 * 2. MethodArgumentNotValidException — @Validated 校验失败（请求体字段校验）
 * 3. BindException — 表单绑定错误
 * 4. MissingServletRequestParameterException — 缺少必传参数
 * 5. Exception — 兜底，处理所有未预期的异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     *
     * 由 Service 层主动抛出，如：规则不存在、状态不允许操作等
     */
    @ExceptionHandler(BusinessException.class)
    public ResultVO<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return ResultVO.fail(e.getCode(), e.getMessage());
    }

    /**
     * 请求体参数校验异常
     *
     * @Validated 触发的字段校验失败，返回具体哪个字段不通过
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultVO<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败：{}", message);
        return ResultVO.fail(400, message);
    }

    /**
     * 表单绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResultVO<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.warn("参数绑定失败：{}", message);
        return ResultVO.fail(400, message);
    }

    /**
     * 缺少必传请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResultVO<Void> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少必传参数：{}", e.getParameterName());
        return ResultVO.fail(400, "缺少必传参数：" + e.getParameterName());
    }

    /**
     * 兜底异常处理
     *
     * 捕获所有未预期的异常（如 NullPointerException、数据库异常等），
     * 前端只看到通用错误信息，详细信息打印在日志中方便排错
     */
    @ExceptionHandler(Exception.class)
    public ResultVO<Void> handleException(Exception e) {
        log.error("系统异常：{}", e.getMessage(), e);
        return ResultVO.fail("系统内部错误，请联系管理员");
    }
}
