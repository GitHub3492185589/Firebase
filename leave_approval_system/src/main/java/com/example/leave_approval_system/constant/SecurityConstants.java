package com.example.leave_approval_system.constant;

/**
 * 安全相关常量类
 * 集中管理系统中使用的安全相关常量，避免硬编码，提高可维护性
 */
public class SecurityConstants {
    
    /**
     * JWT 相关常量
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    
    /**
     * 接口路径常量
     */
    public static final String AUTH_PATH = "/api/auth/**";
    public static final String LOGIN_PATH = "/api/auth/login";
    public static final String REGISTER_PATH = "/api/auth/register";
    
    /**
     * 角色常量
     */
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    
    /**
     * 私有构造函数，防止实例化
     */
    private SecurityConstants() {
        throw new IllegalStateException("常量类不应被实例化");
    }
}