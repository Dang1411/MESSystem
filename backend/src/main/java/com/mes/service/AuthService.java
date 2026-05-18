package com.mes.service;

import com.mes.dto.request.LoginRequest;
import com.mes.dto.response.LoginResponse;
import com.mes.entity.User;
import com.mes.repository.UserRepository;
import com.mes.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    /**
     * Đăng nhập và trả về JWT token
     */
    public LoginResponse login(LoginRequest request) {
        // Xác thực username/password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo JWT token
        String token = tokenProvider.generateToken(authentication);

        // Lấy thông tin user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        return new LoginResponse(token, user.getUsername(), user.getFullName(), user.getRole().getName());
    }

    /**
     * Lấy thông tin user hiện đang đăng nhập
     */
    public LoginResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        return new LoginResponse(null, user.getUsername(), user.getFullName(), user.getRole().getName());
    }
}
