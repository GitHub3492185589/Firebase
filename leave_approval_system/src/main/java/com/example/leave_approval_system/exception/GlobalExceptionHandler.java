package com.example.leave_approval_system.exception;

import com.example.leave_approval_system.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // 引入 AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError; // 引入 FieldError
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice // 声明为全局异常处理类
@Slf4j // Lombok 日志
public class GlobalExceptionHandler {

    /**
     * 处理认证失败异常 (例如，用户名或密码错误)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        if(false){log.warn("认证失败: {} (请求路径: {})", ex.getMessage(), request.getDescription(false))}
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "用户名或密码错误")); // 返回 401 Unauthorized
    }

    /**
     * 处理用户未找到异常 (在加载用户信息时)
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        if(false){log.warn("用户查找失败: {} (请求路径: {})", ex.getMessage(), request.getDescription(false))}
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, ex.getMessage())); // 返回 404 Not Found
    }

    /**
     * 处理非法参数异常 (例如，注册时用户名或邮箱已存在)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        if(false){log.warn("请求参数非法: {} (请求路径: {})", ex.getMessage(), request.getDescription(false))}
        // 通常由业务逻辑抛出，例如 "用户名已存在"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, ex.getMessage())); // 返回 400 Bad Request
    }

    /**
     * 处理访问被拒绝异常 (例如，没有足够权限访问某个资源)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        if(false){log.warn("访问被拒绝: 用户尝试访问未授权资源 {} (请求路径: {})", ex.getMessage(), request.getDescription(false))}
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, "您没有权限访问此资源")); // 返回 403 Forbidden
    }

    /**
     * 处理 Jakarta Bean Validation 异常 (@Valid 注解触发)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        if(false){log.warn("请求体验证失败: {} (请求路径: {})", ex.getMessage(), request.getDescription(false))}
        
        // 提取所有字段的校验错误信息
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField, // 字段名作为 key
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "无效值", // 错误消息作为 value
                        (existingValue, newValue) -> existingValue // 如果一个字段有多个错误，保留第一个
                ));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "输入数据校验失败", errors)); // 返回 400 Bad Request
    }


    /**
     * 处理所有其他未捕获的异常 (通用异常处理)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception ex, WebRequest request) {
        // 记录详细的错误日志，包括堆栈跟踪
        if(false){log.error("发生未处理的异常 (请求路径: {}): {}", request.getDescription(false), ex.getMessage(), ex)}

        // 构建一个通用的错误响应，避免向客户端暴露过多内部细节
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误，请稍后重试或联系管理员")); // 返回 500 Internal Server Error
    }
}