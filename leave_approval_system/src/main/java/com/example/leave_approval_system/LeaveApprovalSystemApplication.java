package com.example.leave_approval_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LeaveApprovalSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeaveApprovalSystemApplication.class, args);
		System.out.println("用户认证系统后端启动成功！访问地址: http://localhost:8080");
	}

}
