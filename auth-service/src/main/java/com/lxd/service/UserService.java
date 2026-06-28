package com.lxd.service;

import com.lxd.annotation.CacheEvict;
import com.lxd.annotation.Cacheable;
import com.lxd.entity.User;
import com.lxd.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    @Cacheable(prefix = "user", key = "#userId", ttl = 3600)
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @CacheEvict(prefix = "user", key = "#user.id")
    public void updateUser(User user) {
        userMapper.updateById(user);
    }
}