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
 
package com.nebula.join.interceptor;

import com.nebula.join.annotation.AutoJoin;
import com.nebula.join.context.RegionRouteHelper;
import com.nebula.join.context.RouteContextConfig;
import com.nebula.join.exception.NoRegionException;
import com.nebula.join.properties.RegionRouteProperties;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RegionSqlInterceptor implements Interceptor {
    
    private final RegionRouteProperties properties;
    
    private final Map<String, RouteContextConfig> annotationCache = new ConcurrentHashMap<>();
    
    public RegionSqlInterceptor(RegionRouteProperties properties) {
        this.properties = properties;
    }
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler resultHandler = (ResultHandler) args[3];
        Executor executor = (Executor) invocation.getTarget();
        
        BoundSql boundSql;
        CacheKey cacheKey;
        if (args.length == 4) {
            // 4 参数方法：必须手动获取 BoundSql，后续我们要把这个改过的对象传给 6 参数方法
            boundSql = ms.getBoundSql(parameter);
            cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
        } else {
            // 6 参数方法：直接使用参数中的
            cacheKey = (CacheKey) args[4];
            boundSql = (BoundSql) args[5];
        }
        
        RouteContextConfig routeConfig = resolveFinalConfig(ms);
        // 手动开启的 scope 优先；其次看 @AutoJoin 注解配置
        if (!RegionRouteHelper.isRewriteEnabled() && (routeConfig == null || !routeConfig.isEnabled())) {
            return invocation.proceed();
        }
        
        List<Long> regions = RegionRouteHelper.getRegions();
        if (regions == null || regions.isEmpty()) {
            throw new NoRegionException("No regions permissions");
        }
        
        String originSql = boundSql.getSql();
        try {
            String newSql = rewriteSql(originSql, regions, routeConfig);
            BoundSql newBoundSql = copyBoundSql(ms, boundSql, newSql);
            return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, newBoundSql);
        } catch (JSQLParserException e) {
            throw new SQLException("Region Route SQL rewrite failed", e);
        }
        // 注意：scope 的计数由开启方（RegionRouteTemplate / RegionRouteHelper.clear）负责释放，
        // 拦截器不能按查询次数递减，否则多条 SQL 时第二条起改写会被静默关闭。
    }
    
    /**
     * BoundSql.additionalParameters 是私有属性，必须复制否则动态 SQL 参数会丢失
     */
    private static final Field ADDITIONAL_PARAMETERS_FIELD;
    
    static {
        try {
            ADDITIONAL_PARAMETERS_FIELD = BoundSql.class.getDeclaredField("additionalParameters");
            ADDITIONAL_PARAMETERS_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    private BoundSql copyBoundSql(MappedStatement ms, BoundSql boundSql, String newSql) {
        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), newSql, boundSql.getParameterMappings(), boundSql.getParameterObject());
        try {
            Map<String, Object> additionalParameters = (Map<String, Object>) ADDITIONAL_PARAMETERS_FIELD.get(boundSql);
            for (Map.Entry<String, Object> entry : additionalParameters.entrySet()) {
                newBoundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            log.warn("Failed to copy additionalParameters for BoundSql", e);
        }
        return newBoundSql;
    }
    
    private RouteContextConfig resolveFinalConfig(MappedStatement ms) {
        
        RouteContextConfig config = RegionRouteHelper.getContextConfig();
        
        // 应用上下文配置，未填写的字段回退到全局默认值。
        // 返回副本，避免修改调用方放入 ThreadLocal 的原始对象
        if (Objects.nonNull(config)) {
            return new RouteContextConfig(config.isEnabled(),
                    Objects.nonNull(config.getMainColumn()) ? config.getMainColumn() : properties.getMainColumn(),
                    Objects.nonNull(config.getJoinTable()) ? config.getJoinTable() : properties.getJoinTable(),
                    Objects.nonNull(config.getJoinColumn()) ? config.getJoinColumn() : properties.getJoinColumn());
        }
        return getRouteConfig(ms);
    }
    
    /**
     * 解析并缓存注解配置
     */
    private RouteContextConfig getRouteConfig(MappedStatement ms) {
        String msId = ms.getId();
        return annotationCache.computeIfAbsent(msId, id -> {
            RouteContextConfig config = new RouteContextConfig(false, properties.getMainColumn(), properties.getJoinTable(), properties.getJoinColumn());
            try {
                // 处理 Mapper 代理类的全限定名
                String className = id.substring(0, id.lastIndexOf("."));
                String methodName = id.substring(id.lastIndexOf(".") + 1);
                Class<?> mapperClass = Class.forName(className);
                
                // 简单的遍历匹配，如果存在方法重载可能需要根据参数类型精确匹配
                for (Method method : mapperClass.getMethods()) {
                    if (method.getName().equals(methodName)) {
                        AutoJoin annotation = method.getAnnotation(AutoJoin.class);
                        if (annotation != null) {
                            config.setEnabled(true);
                            config.setMainColumn(annotation.mainColumn());
                            config.setJoinTable(annotation.joinTable());
                            config.setJoinColumn(annotation.joinColumn());
                            return config;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse AutoRoute annotation for " + id, e);
            }
            return config;
        });
    }
    
    private String rewriteSql(String sql, List<Long> regions, RouteContextConfig config) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        if (!(statement instanceof Select select)) {
            // 本拦截器只做读隔离；非 SELECT 语句（insert/update/delete）不经过 Executor.query，正常不会到这里
            return sql;
        }
        
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        
        FromItem fromItem = plainSelect.getFromItem();
        if (!(fromItem instanceof Table mainTable)) {
            // FromItem 不是简单 Table（如子查询）时无法安全注入权限 Join。
            // 默认快速失败，防止未加区域过滤的 SQL 越权执行；
            // 可通过 region-route.fail-on-unrewritable=false 显式关闭（有越权风险）。
            if (properties.isFailOnUnrewritable()) {
                throw new NoRegionException("Unable to apply region route: unsupported FROM clause in SQL: " + sql);
            }
            log.warn("Region route skipped for unsupported FROM clause, SQL executes without region filter: {}", sql);
            return sql;
        }
        
        // 默认使用 'uda' 或主表名称的首字母缩写
        String mainAlias = mainTable.getAlias() != null ? mainTable.getAlias().getName() : "t1"; // 使用 t1 作为通用别名
        if (mainTable.getAlias() == null) {
            mainTable.setAlias(new Alias(mainAlias, false));
        }
        
        // 2. 解决 SELECT 列表中的字段歧义问题 (id, uid -> uda.id, uda.uid)
        List<SelectItem> modifiedSelectItems = new ArrayList<>();
        for (SelectItem item : plainSelect.getSelectItems()) {
            if (item instanceof SelectExpressionItem sei) {
                if (sei.getExpression() instanceof Column col && sei.getAlias() == null) {
                    // 仅对非限定（没有表名/别名）的简单字段添加主表别名
                    if (col.getTable() == null || col.getTable().getName() == null) {
                        col.setColumnName(mainAlias + "." + col.getColumnName());
                    }
                }
                modifiedSelectItems.add(sei);
            } else if (item instanceof AllColumns) {
                // 如果是 SELECT *，则替换为 uda.*
                Table aliasTable = new Table(mainAlias);
                modifiedSelectItems.add(new AllTableColumns(aliasTable));
            } else {
                modifiedSelectItems.add(item);
            }
        }
        
        plainSelect.setSelectItems(modifiedSelectItems);
        
        ColumnQualifier qualifier = new ColumnQualifier(mainAlias);
        
        // 3. 应用 Column Qualifier 到原始 WHERE 子句
        Expression originalWhere = plainSelect.getWhere();
        if (originalWhere != null) {
            // 创建并应用 ColumnQualifier，确保 WHERE 中的 uid, id, is_delete 等字段都被加上别名
            // 遍历并修改原始 WHERE 表达式树
            originalWhere.accept(qualifier);
        }
        
        List<net.sf.jsqlparser.statement.select.OrderByElement> orderByElements = plainSelect.getOrderByElements();
        if (orderByElements != null) {
            for (net.sf.jsqlparser.statement.select.OrderByElement orderByElement : orderByElements) {
                // 对排序字段的表达式进行访问，自动添加别名
                orderByElement.getExpression().accept(qualifier);
            }
        }
        
        // 3.2. (建议) 应用 Column Qualifier 到 GROUP BY 子句
        net.sf.jsqlparser.statement.select.GroupByElement groupBy = plainSelect.getGroupBy();
        if (groupBy != null) {
            net.sf.jsqlparser.expression.operators.relational.ExpressionList groupByExpressions = groupBy.getGroupByExpressionList();
            if (groupByExpressions != null) {
                for (Expression expr : groupByExpressions.getExpressions()) {
                    expr.accept(qualifier);
                }
            }
        }
        
        // 3.3. (建议) 应用 Column Qualifier 到 HAVING 子句
        Expression having = plainSelect.getHaving();
        if (having != null) {
            having.accept(qualifier);
        }
        
        // 4. 构建 INNER JOIN csa_user_route cur ON uda.uid = cur.uid
        // Join Table: csa_user_route cur
        Table joinTable = new Table(config.getJoinTable());
        String joinAlias = "cur";
        joinTable.setAlias(new Alias(joinAlias));
        Join join = new Join();
        join.setInner(true);
        join.setRightItem(joinTable);
        
        // ON condition: t1.uid = cur.uid
        EqualsTo onCondition = new EqualsTo();
        onCondition.setLeftExpression(new Column(mainAlias + "." + config.getMainColumn()));
        onCondition.setRightExpression(new Column(joinAlias + "." + config.getJoinColumn()));
        join.setOnExpression(onCondition);
        
        // 将权限 Join 插入到 Join 列表的最前面，确保它先于其他业务 Join 执行（如果有的话）
        List<Join> joins = plainSelect.getJoins();
        if (joins == null) {
            plainSelect.setJoins(Collections.singletonList(join));
        } else {
            // 如果有其他 Join，将权限 Join 放在最前面
            joins.add(0, join);
            plainSelect.setJoins(joins);
        }
        
        // 5. 追加 WHERE 条件: AND cur.csa_user_region IN (1,2)
        InExpression regionIn = new InExpression();
        regionIn.setLeftExpression(new Column(joinAlias + "." + properties.getRegionColumnName()));
        
        ExpressionList itemsList = new ExpressionList();
        itemsList.setExpressions(regions.stream().map(LongValue::new).collect(Collectors.toList()));
        regionIn.setRightItemsList(itemsList);
        
        if (originalWhere == null) {
            plainSelect.setWhere(regionIn);
        } else {
            // (Original WHERE) AND (new condition)
            plainSelect.setWhere(new AndExpression(originalWhere, regionIn));
        }
        
        return select.toString();
    }
    
    @Override
    public Object plugin(Object target) {
        // 生成代理对象
        return Plugin.wrap(target, this);
    }
    
    @Override
    public void setProperties(Properties properties) {
        // 如果需要在 xml 中配置属性，可以在这里接收
    }
}
