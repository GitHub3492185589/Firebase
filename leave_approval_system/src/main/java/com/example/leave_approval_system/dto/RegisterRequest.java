package com.example.leave_approval_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data // Lombok: 自动生成 getter, setter 等
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空") // Jakarta Validation: 非空约束
    @Size(min = 3, max = 50, message = "用户名长度必须在 3 到 50 个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少需要 6 个字符") // 建议生产环境使用更复杂的密码策略
    private String password;

    // --- 可选字段 ---
    @Email(message = "邮箱格式不正确") // Jakarta Validation: 邮箱格式约束
    private String email;

    private String phoneNumber;

    private LocalDate birthDate; // 使用 LocalDate 类型接收日期

    private String avatarUrl;

    private String nationality;

    private String address;

    private String socialQq;

    private String socialWechat;
}