package com.lxd.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 免鉴权URL白名单配置
 * 对应 application.yml 中的 security.ignore-urls
 */
@Data
@Component
@ConfigurationProperties(prefix = "security")
public class IgnoreUrlsConfig {
    private List<String> ignoreUrls = new ArrayList<>();
}
