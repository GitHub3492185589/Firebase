package com.example.leave_approval_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一API响应对象
 * 用于标准化所有接口的返回格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    
    public ApiResponse(Integer code, String message, T data, LocalDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间戳
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 创建成功响应
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功的API响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .;
    }

    /**
     * 创建成功响应（无数据）
     * @return 成功的API响应对象
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.builder()
                .code(200)
                .message("操作成功")
                .;
    }

    /**
     * 创建成功响应（自定义消息）
     * @param message 自定义消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功的API响应对象
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.builder()
                .code(200)
                .message(message)
                .data(data)
                .;
    }

    /**
     * 创建错误响应
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误的API响应对象
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.builder()
                .code(code)
                .message(message)
                .;
    }

    /**
     * 创建错误响应（默认错误码500）
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误的API响应对象
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(500, message);
    }
    
    /**
     * 创建错误响应（带有详细错误信息）
     * @param code 错误码
     * @param message 错误消息
     * @param errors 详细错误信息
     * @param <T> 数据类型
     * @return 错误的API响应对象
     */
    public static <T> ApiResponse<T> error(Integer code, String message, Object errors) {
        return ApiResponse.builder()
                .code(code)
                .message(message)
                .data((T) errors) // 将详细错误信息放入data字段
                .;
    }
}