package com.lxd.controller;

import com.lxd.dto.LoginReq;
import com.lxd.dto.RegisterReq;
import com.lxd.dto.TokenResp;
import com.lxd.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 4.1 登录
    @PostMapping("/login")
    public TokenResp login(@RequestBody LoginReq req) {
        return authService.login(req);
    }

    // 4.1 注册
    @PostMapping("/register")
    public String register(@RequestBody RegisterReq req) {
        authService.register(req);
        return "注册成功";
    }

    // 4.1 刷新 Token
    @PostMapping("/refresh")
    public TokenResp refresh(HttpServletRequest request) {
        String token = extractToken(request);
        return authService.refreshToken(token);
    }

    // 4.1 登出
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        String token = extractToken(request);
        authService.logout(token);
        return "登出成功";
    }

    // 提取 Header 中的 Bearer Token
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new RuntimeException("未检测到有效的 Token");
    }
}