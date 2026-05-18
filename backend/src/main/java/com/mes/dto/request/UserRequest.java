package com.mes.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserRequest {

    @NotBlank(message = "Mã nhân viên không được để trống")
    private String employeeCode;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    // Null khi update (giữ nguyên mật khẩu)
    private String password;

    @NotNull(message = "Vai trò không được để trống")
    private Integer roleId;

    private Boolean isActive = true;
}
