package com.example.leave_approval_system.exception;

import lombok.Getter; // Lombok Getter
import java.util.Date;

@Getter // 只需 Getter，因为通常是只读的
public class ErrorDetails {
    private final Date timestamp;     // 错误发生的时间戳
    private final String message;     // 错误消息 (给用户看)
    private final String details;     // 错误的详细描述 (例如请求路径)
    private final String path;        // 请求的路径 (冗余，但有时方便)

    public ErrorDetails(Date timestamp, String message, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
        this.path = details; // 简化，将 details 视为 path
    }

    public ErrorDetails(Date timestamp, String message, String details, String path) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
        this.path = path;
    }
}