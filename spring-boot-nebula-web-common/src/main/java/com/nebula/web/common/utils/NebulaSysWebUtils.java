package com.nebula.web.common.utils;

import java.util.Objects;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author : wh
 * @date : 2024/3/21 10:21
 * @description:
 */
@ConfigurationProperties(prefix = "nebula")
@Data
public class NebulaSysWebUtils {

    private static final String DEV = "dev";

    private static final String TEST = "test";

    private static final String PRD = "prd";

    /**
     * 开发环境
     */
    @Value("${spring.profiles.active:dev}")
    private String active;

    /**
     * 服务名
     */
    @Value("${spring.application.name:unknown}")
    private String applicationName;

    public boolean isPrd() {
        return Objects.equals(active, PRD);
    }

    public boolean isDev() {
        return Objects.equals(active, DEV);
    }

    public boolean isTest() {
        return Objects.equals(active, TEST);
    }

}
