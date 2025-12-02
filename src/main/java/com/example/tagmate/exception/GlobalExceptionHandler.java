package com.example.tagmate.exception;

import com.example.tagmate.common.BaseResponse;
import com.example.tagmate.common.ErrorCode;
import com.example.tagmate.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 
 * @author 86150
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     *
     * @param e 业务异常
     * @return 统一响应
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException: ", e);
        return new BaseResponse<>(e.getCode(), null, e.getMessage(), e.getDescription());
    }

    /**
     * 参数校验异常处理
     * 
     * @param e 参数校验异常
     * @return 统一响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: ", e);
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder errorMessage = new StringBuilder();

        // 获取第一个错误信息
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                errorMessage.append(fieldError.getDefaultMessage());
            }
        }

        return ResultUtils.error(ErrorCode.PARAMS_ERROR, errorMessage.toString());
    }

    /**
     * 运行时异常处理
     *
     * @param e 运行时异常
     * @return 统一响应
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException: ", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }
}