package com.example.leave_approval_system.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应的数据传输对象 (DTO)
 * 包含登录成功后返回给前端的信息
 */
@Data // Lombok: 自动生成 getter, setter, toString, equals, hashCode
@NoArgsConstructor // Lombok: 自动生成无参构造函数 (可选, 但有时有用)
@AllArgsConstructor // Lombok: 自动生成包含所有字段的构造函数 (关键! 会生成 AuthResponse(String, String, String) 构造器)
public class AuthResponse {

    /**
     * 生成的 JWT 访问令牌
     */
    private String token;

    /**
     * 登录成功的用户名
     */
    private String username;

    /**
     * Token 类型, 固定为 "Bearer".
     * 虽然这里有默认值, 但 @AllArgsConstructor 生成的构造函数仍然需要这个参数。
     */
    private String tokenType = "Bearer";

    // 注意: 因为使用了 @AllArgsConstructor, Lombok 会自动生成如下构造函数:
    // public AuthResponse(String token, String username, String tokenType) {
    //     this.token = token;
    //     this.username = username;
    //     this.tokenType = tokenType; // 即使有默认值，构造函数也需要它
    // }
    // 同时 @NoArgsConstructor 生成了 public AuthResponse() {}
    // 而 @Data 生成了 getter/setter 等方法。
}