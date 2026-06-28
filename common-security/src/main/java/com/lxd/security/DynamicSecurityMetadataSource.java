package com.lxd.security;

import com.lxd.service.ResourceService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import com.lxd.entity.Resource;
import org.springframework.util.AntPathMatcher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DynamicSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
    private final ResourceService resourceService; // 从数据库或缓存读取资源-角色映射

    // ✅ 使用 volatile 保证可见性，整体替换保证原子性
    private volatile Map<String, List<ConfigAttribute>> resourceMap = new LinkedHashMap<>();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @PostConstruct
    public void loadDataSource() {
        List<Resource> resources = resourceService.listAll();

        // ✅ 构建新Map再整体替换，避免并发读写问题
        Map<String, List<ConfigAttribute>> newMap = new LinkedHashMap<>();
        resources.forEach(res -> {
            List<ConfigAttribute> attrs = res.getRoles().stream()
                    .map(role -> new SecurityConfig(role.getRoleCode()))
                    .collect(Collectors.toList());
            newMap.put(res.getUrl(), attrs);
        });

        this.resourceMap = newMap; // 原子引用替换
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        String requestUrl = ((FilterInvocation) object).getRequestUrl();
        for (Map.Entry<String, List<ConfigAttribute>> entry : resourceMap.entrySet()) {
            if (pathMatcher.match(entry.getKey(), requestUrl)) {
                return entry.getValue();
            }
        }
        // ✅ 明确返回空集合表示"该URL无需权限校验"
        // 若希望未配置URL默认拒绝，改为: throw new AccessDeniedException("...")
        return Collections.emptyList();
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return List.of();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    public void refreshResourceMap() {
        loadDataSource();
    }
}
