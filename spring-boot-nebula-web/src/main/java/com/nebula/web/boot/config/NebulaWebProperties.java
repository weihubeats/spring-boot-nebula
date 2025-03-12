package com.nebula.web.boot.config;

import com.nebula.web.boot.enums.ResultCode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author : wh
 * @date : 2025/3/12 16:58
 * @description:
 */
@ConfigurationProperties(prefix = "nebula.web")
@Data
public class NebulaWebProperties {

    /**
     * 返回状态码
     */
    private Integer responseCode = ResultCode.SUCCESS.getCode();
    
    
}
