package com.nebula.web.common.utils;


import lombok.Getter;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

public class NebulaSysWebUtils {

    public static final String ENV_DEV = "dev";

    public static final String ENV_TEST = "test";

    public static final String ENV_STAGE = "stage";

    public static final String ENV_PRD = "prd";

    private final Environment environment;

    @Getter
    private final String applicationName;

    public NebulaSysWebUtils(Environment environment) {
        this.environment = environment;
        this.applicationName = environment.getProperty("spring.application.name", "unknown");
    }

    // ================= 环境判断 API =================

    public boolean isDev() {
        return isEnv(ENV_DEV);
    }

    public boolean isTest() {
        return isEnv(ENV_TEST);
    }

    public boolean isStage() {
        return isEnv(ENV_STAGE);
    }

    public boolean isPrd() {
        return isEnv(ENV_PRD);
    }

    /**
     * 2. 自定义环境判断：检查当前是否激活了指定的 Profile
     *
     * @param profile 自定义环境名称
     * @return true 如果激活了该环境
     */
    public boolean isEnv(String profile) {
        if (profile == null || profile.isBlank()) {
            return false;
        }
        return environment.acceptsProfiles(Profiles.of(profile));
    }

    /**
     * 3. 额外增强：支持判断多个自定义环境（只要满足其中一个就返回 true）
     * * @param profiles 多个环境名称
     * @return true 如果激活了其中任意一个环境
     */
    public boolean isAnyEnv(String... profiles) {
        if (profiles == null || profiles.length == 0) {
            return false;
        }
        return environment.acceptsProfiles(Profiles.of(profiles));
    }

    public String[] getActiveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            return activeProfiles;
        }
        return environment.getDefaultProfiles();
    }

    public String getActive() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            return activeProfiles[0];
        }
        return environment.getDefaultProfiles()[0];
    }
}