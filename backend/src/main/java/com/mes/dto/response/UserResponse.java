package com.mes.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Integer id;
    private String employeeCode;
    private String fullName;
    private String username;
    private String roleName;
    private String roleDescription;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
