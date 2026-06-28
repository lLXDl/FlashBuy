package com.lxd.config;

import com.lxd.util.JwtTokenUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SecurityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public JwtTokenUtil jwtTokenUtil() {
        return new JwtTokenUtil();
    }
}
