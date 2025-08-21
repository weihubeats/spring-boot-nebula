package com.nebula.dynamic.datasource.core;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {
    
    
    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContextHolder.getDataSource();
    }
}
