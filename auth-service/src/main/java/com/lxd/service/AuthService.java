package com.lxd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lxd.annotation.Cacheable;
import com.lxd.dto.LoginReq;
import com.lxd.dto.RegisterReq;
import com.lxd.dto.TokenResp;
import com.lxd.entity.User;
import com.lxd.mapper.RoleMapper;
import com.lxd.mapper.UserMapper;
import com.lxd.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.expiration}")
    private Long expiration;  // 用于黑名单过期时间

    // 修改后（添加缓存）
    @Cacheable(prefix = "user", key = "#username")
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    // ============ 4.2 登录核心逻辑 ============
    public TokenResp login(LoginReq req) {
        // 1. 根据用户名查用户
        User user = getUserByUsername(req.getUsername());  // 优先查缓存

        // 2. 校验用户是否存在 及 密码是否匹配（BCrypt）
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 检查账号状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 4. 查询该用户的角色列表（例如 ["ROLE_ADMIN", "ROLE_USER"]）
        List<String> roles = roleMapper.selectRoleCodesByUserId(user.getId());

        // 5. 调用 common-security 的 JWT 工具生成 Token
        String token = jwtTokenUtil.generateToken(user.getId(), roles);

        // 6. 返回 Token 和用户 ID
        return new TokenResp(token, user.getId());
    }

    // ============ 注册逻辑 ============
    @Transactional
    public void register(RegisterReq req) {
        // 检查用户名是否已存在
        String existUsername = userMapper.checkUsernameExists(req.getUsername());
        if (existUsername != null) {
            throw new RuntimeException("用户名已被占用");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        // 密码加密存入数据库
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setStatus(1); // 默认启用
        userMapper.insert(user);

        // 注册时默认分配普通用户角色（roleId=2 对应 ROLE_USER）
        roleMapper.insertUserRole(user.getId(), 2L);
    }

    // ============ 刷新 Token（重置有效期） ============
    public TokenResp refreshToken(String oldToken) {
        // 1. 校验旧 Token 是否有效（若无效直接抛异常）
        if (!jwtTokenUtil.validateToken(oldToken)) {
            throw new RuntimeException("Token 无效或已过期");
        }

        // 2. 从旧 Token 中解析用户 ID 和角色
        Long userId = jwtTokenUtil.getUserIdFromToken(oldToken);
        List<String> roles = jwtTokenUtil.getRolesFromToken(oldToken);

        // 3. 将旧 Token 加入黑名单（防止并发刷新导致旧 Token 仍可用）
        addToBlacklist(oldToken);

        // 4. 生成新 Token
        String newToken = jwtTokenUtil.generateToken(userId, roles);
        return new TokenResp(newToken, userId);
    }

    // ============ 登出（加入黑名单） ============
    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token 不能为空");
        }
        // 将当前 Token 加入 Redis 黑名单
        addToBlacklist(token);
    }

    // ---------- 私有方法：黑名单处理 ----------
    private void addToBlacklist(String token) {
        // 解析 Token 剩余有效期（秒）
        long ttl = jwtTokenUtil.getRemainingExpiration(token); // 需要你在 JwtTokenUtil 中补一个方法
        if (ttl <= 0) {
            throw new RuntimeException("Token 已过期，无需登出");
        }
        // 存入 Redis，Key: blacklist:token:{token}，Value: "logout"
        String key = "blacklist:token:" + token;
        redisTemplate.opsForValue().set(key, "logout", ttl, TimeUnit.SECONDS);
    }
}