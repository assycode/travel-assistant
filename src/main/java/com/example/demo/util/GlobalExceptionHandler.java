package com.example.demo.util;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 Assert 断言异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.fail(ErrorCode.PARAM_ERROR, e.getMessage());
    }

    /**
     * 处理请求参数绑定/校验异常（@Valid/@Validated）
     */
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidException(Exception e) {
        String message = "参数校验失败";
        if (e instanceof BindException) {
            message = ((BindException) e).getFieldError().getDefaultMessage();
        } else if (e instanceof MethodArgumentNotValidException) {
            message = ((MethodArgumentNotValidException) e).getBindingResult().getFieldError().getDefaultMessage();
        } else if (e instanceof ConstraintViolationException) {
            message = ((ConstraintViolationException) e).getMessage();
        }
        return Result.fail(ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = String.format("参数%s类型不匹配，期望类型：%s", e.getName(), e.getRequiredType().getSimpleName());
        return Result.fail(ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 处理系统未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleSystemException(Exception e) {
        // 生产环境可打印日志，此处简化
        return Result.fail(ErrorCode.SYSTEM_ERROR, "系统内部错误：" + e.getMessage());
    }
}