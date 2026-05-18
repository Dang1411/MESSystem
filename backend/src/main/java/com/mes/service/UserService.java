package com.mes.service;

import com.mes.dto.request.UserRequest;
import com.mes.dto.response.UserResponse;
import com.mes.entity.Role;
import com.mes.entity.User;
import com.mes.exception.MesException;
import com.mes.repository.RoleRepository;
import com.mes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers(String keyword) {
        List<User> users = (keyword != null && !keyword.trim().isEmpty())
                ? userRepository.searchUsers(keyword.trim())
                : userRepository.findAll();
        return users.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UserResponse getUserById(Integer id) {
        User user = findById(id);
        return toResponse(user);
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        // Kiểm tra username trùng
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new MesException("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại");
        }
        // Kiểm tra mã nhân viên trùng
        if (userRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new MesException("Mã nhân viên '" + request.getEmployeeCode() + "' đã tồn tại");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new MesException("Mật khẩu không được để trống khi tạo mới");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new MesException("Không tìm thấy vai trò"));

        User user = new User();
        user.setEmployeeCode(request.getEmployeeCode());
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Integer id, UserRequest request) {
        User user = findById(id);

        // Kiểm tra username trùng (trừ chính mình)
        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new MesException("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new MesException("Không tìm thấy vai trò"));

        user.setEmployeeCode(request.getEmployeeCode());
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setRole(role);
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : user.getIsActive());

        // Chỉ đổi mật khẩu nếu có truyền vào
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Integer id) {
        User user = findById(id);
        userRepository.delete(user);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // --- Helper ---
    private User findById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new MesException("Không tìm thấy người dùng với ID: " + id, HttpStatus.NOT_FOUND));
    }

    private UserResponse toResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setEmployeeCode(user.getEmployeeCode());
        res.setFullName(user.getFullName());
        res.setUsername(user.getUsername());
        res.setRoleName(user.getRole().getName());
        res.setRoleDescription(user.getRole().getDescription());
        res.setIsActive(user.getIsActive());
        res.setCreatedAt(user.getCreatedAt());
        return res;
    }
}
