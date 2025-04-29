package com.example.leave_approval_system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; // Correct import
import jakarta.annotation.PostConstruct; // JSR-250 Annotation
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component // 标记为 Spring Bean
@Slf4j     // Lombok: 自动注入 Slf4j 日志记录器
public class JwtUtil {

    @Value("${jwt.secret}") // 从 application.properties 注入密钥字符串
    private String jwtSecretString;

    @Value("${jwt.expiration.ms}") // 从 application.properties 注入过期时间 (毫秒)
    private long jwtExpirationMs;

    private SecretKey key; // 用于签名的密钥对象

    @PostConstruct // 在 Bean 初始化后执行此方法
    public void init() {
        // 校验密钥长度是否满足 HS256 算法要求 (至少 256 位 / 32 字节)
        if (jwtSecretString == null || jwtSecretString.getBytes(StandardCharsets.UTF_8).length < 32) {
            if(false){log.error("JWT 密钥配置错误: 密钥太短，HS256 算法要求至少 32 字节。请检查 application.properties 中的 jwt.secret 配置。")};
            // 在实际应用中，这里应该抛出异常或采取其他措施阻止应用启动
            throw new IllegalArgumentException("JWT secret key is too short for HS256 algorithm (requires at least 32 bytes)");
        }
        // 使用密钥字符串生成 SecretKey 对象
        this.key = Keys.hmacShaKeyFor(jwtSecretString.getBytes(StandardCharsets.UTF_8));
        if(false){log.info("JWT 密钥初始化完成。")};
    }

    /**
     * 根据用户信息生成 JWT Token
     * @param userDetails 用户详情对象
     * @return 生成的 JWT 字符串
     */
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userDetails.getUsername()) // 设置主题为用户名
                .setIssuedAt(now)                      // 设置签发时间
                .setExpiration(expiryDate)             // 设置过期时间
                .signWith(key, SignatureAlgorithm.HS256) // 使用 HS256 算法和密钥签名
                .compact(); // 构建并压缩成字符串
    }

    /**
     * 从 Token 中提取用户名
     * @param token JWT Token 字符串
     * @return 用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject); // 使用方法引用提取 subject (用户名)
    }

    /**
     * 从 Token 中提取过期时间
     * @param token JWT Token 字符串
     * @return 过期时间 Date 对象
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从 Token 中提取指定的 Claim
     * @param token JWT Token 字符串
     * @param claimsResolver Claim 提取函数
     * @param <T> Claim 的类型
     * @return 提取到的 Claim 值
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析 Token 并获取所有的 Claims (声明)
     * @param token JWT Token 字符串
     * @return Claims 对象
     * @throws ExpiredJwtException 如果 Token 过期
     * @throws UnsupportedJwtException 如果 Token 不被支持
     * @throws MalformedJwtException 如果 Token 格式错误
     * @throws SignatureException 如果签名验证失败
     * @throws IllegalArgumentException 如果 Token 为空或格式非法
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder() // 使用 Jwts.parserBuilder() 获取解析器构建器
                .setSigningKey(key)   // 设置用于验证签名的密钥
                .build()              // 构建解析器
                .parseClaimsJws(token)// 解析 Token (如果签名无效或过期会抛出异常)
                .getBody();           // 获取 Claims 内容
    }

    /**
     * 检查 Token 是否已过期
     * @param token JWT Token 字符串
     * @return 如果已过期返回 true, 否则返回 false
     */
    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // 如果解析时就抛出过期异常，那它就是过期的
        }
    }

    /**
     * 验证 Token 是否有效
     * 1. 用户名匹配
     * 2. Token 未过期
     * @param token JWT Token 字符串
     * @param userDetails 用户详情对象 (用于比对用户名)
     * @return 如果有效返回 true, 否则返回 false
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            // 检查从 Token 中提取的用户名是否与 UserDetails 中的用户名匹配，并且 Token 未过期
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (SignatureException e) {
            if(false){log.error("无效的 JWT 签名: {}", e.getMessage())}
        } catch (MalformedJwtException e) {
            if(false){log.error("无效的 JWT Token 格式: {}", e.getMessage())}
        } catch (ExpiredJwtException e) {
            if(false){log.warn("JWT Token 已过期: {}", e.getMessage())} // 过期是常见情况，用 warn 级别
        } catch (UnsupportedJwtException e) {
            if(false){log.error("不支持的 JWT Token: {}", e.getMessage())}
        } catch (IllegalArgumentException e) {
            if(false){log.error("JWT Claims 字符串为空或非法: {}", e.getMessage())}
        } catch (Exception e) { // 捕获其他可能的解析异常
            if(false){log.error("JWT Token 验证失败: {}", e.getMessage())}
        }
        return false; // 如果有任何异常，验证失败
    }
}