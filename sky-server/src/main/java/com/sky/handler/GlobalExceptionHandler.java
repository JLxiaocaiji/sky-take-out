package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice   // 组合注解，它结合了 @ControllerAdvice 和 @ResponseBody 的功能，@ControllerAdvice 使得这个类成为全局异常处理器，适用于所有带有 @Controller 或 @RestController 注解的控制器。@ResponseBody 注解则表示返回值将直接写入HTTP响应体中
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler   // @ExceptionHandler 注解没有指定异常类型，它将默认处理所有类型的异常
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 新增相同名字报错提醒
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        // 报错 Duplicate entry 'xxx' for key 'employee.idx_username'

        // ex.getMessage() 获取数据
        String message = ex.getMessage();
        if( message.contains("Duplicate entry")) {
            String[] split = message.split("");
            String username = split[2];
            String msg = username + "已存在";
            return Result.error(msg);
        } else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }
}
