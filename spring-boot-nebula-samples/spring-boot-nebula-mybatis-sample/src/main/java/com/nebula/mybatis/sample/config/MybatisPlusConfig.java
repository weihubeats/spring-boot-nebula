package com.nebula.mybatis.sample.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.nebula.mybatis.entity.NebulaMetaObjectHandler;
import com.nebula.mybatis.handler.ArrayTypeHandler;
import javax.sql.DataSource;
import org.apache.ibatis.type.JdbcType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author : wh
 * @date : 2025/1/8 16:37
 * @description:
 */
@Configuration
public class MybatisPlusConfig {


    @Bean
    public MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean (DataSource dataSource) throws Exception{
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        factoryBean.setMapperLocations(resolver.getResources("classpath:/mybatis/**/*.xml"));
        factoryBean.setTypeHandlers(new ArrayTypeHandler());
        // 实体类别名 别名冲突暂时不使用别名 WarehouseLocationDO
        factoryBean.setTypeAliasesPackage("com.nebula.mybatis.sample.entity");
        MybatisConfiguration configuration = new MybatisConfiguration();
        //开启下划线转驼峰
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setBanner(false);
        globalConfig.setMetaObjectHandler(new NebulaMetaObjectHandler());
        factoryBean.setGlobalConfig(globalConfig);
        factoryBean.setConfiguration(configuration);
        return factoryBean;
    }
    
}
