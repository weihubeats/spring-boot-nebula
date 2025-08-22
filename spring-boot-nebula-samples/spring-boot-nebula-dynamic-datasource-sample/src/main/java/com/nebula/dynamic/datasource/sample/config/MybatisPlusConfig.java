/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.nebula.dynamic.datasource.sample.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.nebula.dynamic.datasource.DynamicConstant;
import com.nebula.dynamic.datasource.core.DynamicRoutingDataSource;
import com.nebula.mybatis.entity.NebulaMetaObjectHandler;
import com.nebula.mybatis.handler.ArrayTypeHandler;
import com.nebula.mybatis.handler.ListTypeHandler;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/**
 * @author : wh
 * @date : 2025/8/21
 * @description:
 */
@Configuration
@MapperScan("com.nebula.dynamic.datasource.sample.dao.mapper")
@Slf4j
public class MybatisPlusConfig {
    
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "db.nebula.pg.write")
    public DataSourceProperties writeDataSourceProperties() {
        return new DataSourceProperties();
    }
    
    @Bean(name = DynamicConstant.WRITE)
    @Primary
    public DataSource write(DataSourceProperties writeDataSourceProperties) {
        DataSource ds = writeDataSourceProperties.initializeDataSourceBuilder().build();
        
        // schema init
        DatabasePopulator databasePopulator =
                new ResourceDatabasePopulator(
                        new ClassPathResource("write-schema.sql"), new ClassPathResource("write-data.sql"));
        DatabasePopulatorUtils.execute(databasePopulator, ds);
        
        log.info("write datasource: {}", writeDataSourceProperties.getUrl());
        return ds;
    }
    
    @Bean
    @ConfigurationProperties(prefix = "db.nebula.pg.read")
    public DataSourceProperties readDataSourceProperties() {
        return new DataSourceProperties();
    }
    
    @Bean(name = DynamicConstant.READ)
    public DataSource read(DataSourceProperties readDataSourceProperties) {
        DataSource ds = readDataSourceProperties.initializeDataSourceBuilder().build();
        
        // schema init
        DatabasePopulator databasePopulator =
                new ResourceDatabasePopulator(
                        new ClassPathResource("read-schema.sql"), new ClassPathResource("read-data.sql"));
        DatabasePopulatorUtils.execute(databasePopulator, ds);
        
        log.info("read datasource: {}", readDataSourceProperties.getUrl());
        return ds;
    }
    
    /**
     * 配置读数据源
     * @return
     */
    /*
     * @Bean(name = DynamicConstant.READ)
     * 
     * @ConfigurationProperties(prefix = "db.nebula.pg.read") public DataSource read() { return DataSourceBuilder.create().build(); }
     */
    
    /**
     * 动态数据源配置
     * @param write 写数据源
     * @param read 读数据源
     * @return
     */
    @Bean
    public DataSource multipleDataSource(@Qualifier(DynamicConstant.WRITE) DataSource write,
                                         @Qualifier(DynamicConstant.READ) DataSource read) {
        DynamicRoutingDataSource dynamicDataSource = new DynamicRoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DynamicConstant.WRITE, write);
        targetDataSources.put(DynamicConstant.READ, read);
        dynamicDataSource.setTargetDataSources(targetDataSources);
        // 设置默认数据源
        dynamicDataSource.setDefaultTargetDataSource(write);
        return dynamicDataSource;
    }
    
    @Bean
    @Primary
    public MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        factoryBean.setMapperLocations(resolver.getResources("classpath:/mybatis/*.xml"));
        factoryBean.setTypeHandlers(new ArrayTypeHandler(), new ListTypeHandler());
        factoryBean.setTypeAliasesPackage("com.nebula.mybatis.sample.dao.entity");
        MybatisConfiguration configuration = new MybatisConfiguration();
        // 开启下划线转驼峰
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setBanner(false);
        globalConfig.setMetaObjectHandler(new NebulaMetaObjectHandler());
        factoryBean.setGlobalConfig(globalConfig);
        factoryBean.setConfiguration(configuration);
        return factoryBean;
    }
    @Bean
    public DataSourceTransactionManager transactionManager(@Qualifier("multipleDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
