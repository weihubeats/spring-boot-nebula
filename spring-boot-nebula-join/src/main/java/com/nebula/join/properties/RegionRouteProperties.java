package com.nebula.join.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "region-route")
@Data
public class RegionRouteProperties {

    private boolean enabled = true;

    private String joinTable = "csa_user_route";

    private String regionColumnName = "csa_region_id";

    private String joinColumn = "uid";

    private String mainColumn = "uid";

    private String headerName = "X-REGION";
}
