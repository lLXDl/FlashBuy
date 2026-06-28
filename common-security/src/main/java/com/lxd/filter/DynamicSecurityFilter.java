package com.lxd.filter;

import com.lxd.config.IgnoreUrlsConfig;
import com.lxd.security.DynamicSecurityMetadataSource;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class DynamicSecurityFilter extends AbstractSecurityInterceptor implements Filter {
    private final DynamicSecurityMetadataSource dynamicSecurityMetadataSource;
    private final IgnoreUrlsConfig ignoreUrlsConfig;
    private final AccessDecisionManager accessDecisionManager;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @PostConstruct
    public void init() {
        super.setAccessDecisionManager(accessDecisionManager);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestUri = httpRequest.getRequestURI();

        // 白名单URL直接放行
        if (isIgnoreUrl(requestUri)) {
            chain.doFilter(request, response);
            return;
        }

        FilterInvocation fi = new FilterInvocation(request, response, chain);
        InterceptorStatusToken token = super.beforeInvocation(fi);
        try {
            fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
        } finally {
            super.afterInvocation(token, null);
        }
    }

    private boolean isIgnoreUrl(String requestUri) {
        for (String ignoreUrl : ignoreUrlsConfig.getIgnoreUrls()) {
            if (pathMatcher.match(ignoreUrl, requestUri)) {
                return true;
            }
        }
        return false;
    }

    // ✅ 修复2: 返回正确的受保护对象类型
    @Override
    public Class<?> getSecureObjectClass() {
        return FilterInvocation.class;
    }

    // ✅ 修复3: 返回注入的动态元数据源（这是整个过滤器生效的关键）
    @Override
    public SecurityMetadataSource obtainSecurityMetadataSource() {
        return this.dynamicSecurityMetadataSource;
    }
}
