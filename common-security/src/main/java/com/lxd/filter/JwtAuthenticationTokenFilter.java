package com.lxd.filter;

import com.lxd.util.JwtTokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // 🚀 新增：检查 Redis 黑名单
            String blacklistKey = "blacklist:token:" + token;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                // Token 已被拉黑，直接放行但不设置认证（交给后续权限过滤器处理）
                chain.doFilter(request, response);
                return;
            }
            // 1. 校验Token合法性
            if (jwtTokenUtil.validateToken(token)) {
                // 3. 构建Authentication对象
                Long userId = jwtTokenUtil.getUserIdFromToken(token);
                List<String> roles = jwtTokenUtil.getRolesFromToken(token);

                // 将 JWT 中的字符串角色列表转换为 Spring Security 标准的权限对象列表。
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new).collect(Collectors.toList());

                // 构建认证令牌
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);

                // 绑定请求
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 4. 设置到上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
        }
        chain.doFilter(request, response);
    }
}
