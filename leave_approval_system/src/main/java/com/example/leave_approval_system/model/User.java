package com.example.leave_approval_system.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections; // 引入 Collections

@Entity
@Table(name = "users", // 明确指定表名
        uniqueConstraints = { // 添加唯一约束定义
                @UniqueConstraint(columnNames = "username", name = "uk_username"),
                @UniqueConstraint(columnNames = "email", name = "uk_email")
        })
@Data // Lombok: 自动生成 getter, setter, toString, equals, hashCode
@NoArgsConstructor // Lombok: 自动生成无参构造函数
public class User implements UserDetails { // 实现 UserDetails 接口以集成 Spring Security

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自增
    private Long id;

    @Column(nullable = false, length = 50) // 用户名非空，长度限制50
    private String username;

    @Column(name = "password_hash", nullable = false) // 映射到 password_hash 列，非空
    private String password; // 字段名使用 password 以符合 UserDetails 接口，存储的是哈希值

    @Column(length = 100) // 邮箱长度限制100
    private String email;

    @Column(name = "phone_number", length = 20) // 映射到 phone_number 列
    private String phoneNumber;

    @Column(name = "birth_date")
    @Temporal(TemporalType.DATE) // 指定日期类型映射
    private LocalDate birthDate;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(length = 100)
    private String nationality;

    @Column(columnDefinition = "TEXT") // 地址可能较长，使用 TEXT 类型
    private String address;

    @Column(name = "social_qq", length = 50)
    private String socialQq;

    @Column(name = "social_wechat", length = 50)
    private String socialWechat;

    @Column(name = "created_at", nullable = false, updatable = false) // 创建时间不可更新
    @Temporal(TemporalType.TIMESTAMP) // 指定时间戳类型映射
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false) // 更新时间
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    // --- JPA 生命周期回调 ---

    @PrePersist // 在实体持久化之前调用
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate // 在实体更新之前调用
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- UserDetails 接口实现 ---
    // 权限集合，为简单起见，这里返回空集合。实际应用中可能需要基于角色管理权限。
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // return List.of(new SimpleGrantedAuthority("ROLE_USER")); // 如果需要，可以添加默认角色
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        // 返回存储的哈希密码
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    // 账户状态方法，为简单起见，默认都返回 true。可以根据业务需求添加相应字段控制。
    @Override
    public boolean isAccountNonExpired() {
        // 账户是否未过期
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 账户是否未锁定
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 凭证 (密码) 是否未过期
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 账户是否启用
        return true;
    }
}