package com.lxd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;

import java.util.Arrays;
import java.util.List;

/**

 * 权限决策配置类

 * 独立出来以避免与 SecurityConfig 形成循环依赖

 */

@Configuration

public class AccessDecisionConfig {

    /**

     * 权限决策管理器：决定用户是否有权限访问资源

     */

    @Bean

    public AccessDecisionManager accessDecisionManager() {

        List<AccessDecisionVoter<?>> voters = Arrays.asList(

                new RoleVoter(),           // 基于角色的投票器

                new AuthenticatedVoter()   // 基于认证状态的投票器

        );

        return new AffirmativeBased(voters);

    }

}
