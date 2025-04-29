package com.example.leave_approval_system.controller;

import com.example.leave_approval_system.dto.AuthResponse;
import com.example.leave_approval_system.dto.LoginRequest;
import com.example.leave_approval_system.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * 认证控制器接口文档
 * 用于定义 Swagger 文档注解
 */
@Tag(name = "认证管理", description = "包括用户注册、登录和获取个人资料等操作")
public interface AuthControllerDoc {

    @Operation(summary = "用户注册", description = "注册新用户账号")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "用户注册成功"),
            @ApiResponse(responseCode = "400", description = "请求参数无效或用户名/邮箱已存在"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    ResponseEntity<com.example.leave_approval_system.dto.ApiResponse<?>> registerUser(
            @Parameter(description = "用户注册信息", required = true) RegisterRequest registerRequest);

    @Operation(summary = "用户登录", description = "使用用户名和密码登录系统，获取JWT令牌")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功", 
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "用户名或密码错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    ResponseEntity<com.example.leave_approval_system.dto.ApiResponse<?>> authenticateUser(
            @Parameter(description = "用户登录信息", required = true) LoginRequest loginRequest);

    @Operation(summary = "获取用户资料", description = "获取当前登录用户的基本信息，需要JWT认证")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功获取用户资料"),
            @ApiResponse(responseCode = "401", description = "未认证或认证已过期")
    })
    ResponseEntity<com.example.leave_approval_system.dto.ApiResponse<?>> getUserProfile();
}