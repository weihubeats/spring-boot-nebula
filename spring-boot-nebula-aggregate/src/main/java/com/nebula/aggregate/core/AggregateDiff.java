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
 
package com.nebula.aggregate.core;

import com.google.common.collect.Lists;
import com.nebula.base.utils.DataUtils;
import com.nebula.base.utils.PropertyFunc;
import com.nebula.base.utils.ReflectionUtils;
import com.nebula.base.utils.StringUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.diff.ListCompareAlgorithm;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.PropertyChange;
import org.javers.core.diff.changetype.container.ContainerElementChange;
import org.javers.core.diff.changetype.container.ElementValueChange;
import org.javers.core.diff.changetype.container.ListChange;
import org.javers.core.diff.changetype.container.ValueAdded;
import org.javers.core.diff.changetype.container.ValueRemoved;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.Id;

/**
 * @author : wh
 * @date : 2023/12/8 11:04
 * @description:
 */
@Slf4j
public class AggregateDiff {
    
    private static final ConcurrentHashMap<String, List<String>> PROPERTIES_CACHE = new ConcurrentHashMap<>();
    
    private static final Javers javers = JaversBuilder.javers().withListCompareAlgorithm(ListCompareAlgorithm.LEVENSHTEIN_DISTANCE).build();
    
    private final Diff diff;
    
    public AggregateDiff(Object oldVersion, Object currentVersion) {
        this.diff = javers.compare(oldVersion, currentVersion);
    }
    
    /**
     * 构建diff
     *
     * @param currentVersion
     * @param <T>
     */
    public <T extends AbstractAggregate<T>> AggregateDiff(T currentVersion) {
        this.diff = javers.compare(currentVersion.getOld(), currentVersion);
    }
    
    public boolean hasChanges() {
        return diff.hasChanges();
    }
    
    public boolean propertyHasChange(String propertyName) {
        return propertyHasChange(Lists.newArrayList(propertyName));
    }
    
    /**
     * 判断两个对象是否有变化
     * <p>
     * true 发生了变化
     */
    public boolean objectHasChange(Object oldObject, Object newObject) {
        Diff objectDiff = javers.compare(oldObject, newObject);
        return !objectDiff.getChanges().isEmpty();
    }
    
    /**
     * 判断对象里面的属性值是否发生了变化,list需要单独判断,对象也需要单独判断
     *
     * @param propertyNameList 属性值
     * @return true 表示发生了变化
     */
    public boolean propertyHasChange(List<String> propertyNameList) {
        return propertyNameList.stream().anyMatch(s -> !diff.getPropertyChanges(s).isEmpty());
    }
    
    /**
     * 判断对象里面的属性值是否发生了变化
     *
     * @param function
     * @param <T>
     * @param <R>
     * @return
     */
    @SafeVarargs
    public final <T, R> boolean propertyHasChange(PropertyFunc<T, R>... function) {
        return Arrays.stream(function).anyMatch(s -> !diff.getPropertyChanges(ReflectionUtils.getFieldName(s)).isEmpty());
    }
    
    /**
     * 对象变化
     *
     * @param addConsume
     * @param updateConsume
     * @param deleteConsume
     * @param <T>
     */
    public <T> void objectChangeFunction(Consumer<T> addConsume,
                                         Consumer<T> updateConsume,
                                         Consumer<T> deleteConsume,
                                         Class<T> clazz) {
        Changes changes = this.diff.getChanges();
        for (Change change : changes) {
            if ((change instanceof NewObject && Objects.nonNull(addConsume))) {
                change.getAffectedObject().ifPresent(item -> addConsume.accept((T) item));
                return;
            }
            if ((change instanceof ObjectRemoved && Objects.nonNull(deleteConsume))) {
                change.getAffectedObject().ifPresent(item -> deleteConsume.accept((T) item));
                return;
            }
            if ((change instanceof PropertyChange && Objects.nonNull(updateConsume))) {
                change.getAffectedObject().ifPresent(item -> updateConsume.accept((T) item));
                return;
            }
        }
    }
    
    /**
     * @param oldList       旧list
     * @param newList       新list
     * @param clazz         class
     * @param addConsume    新增
     * @param updateConsume 修改
     * @param removeConsume 删除
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public static <T> void listChangeFunction(List<T> oldList,
                                              List<T> newList,
                                              Class<T> clazz,
                                              Consumer<List<T>> addConsume,
                                              Consumer<List<T>> updateConsume,
                                              Consumer<List<T>> removeConsume) {
        
        initNegativeId(clazz, newList);
        Diff listDiff = javers.compareCollections(oldList, newList, clazz);
        Map<String, T> addMap = new HashMap<>();
        Map<String, T> removeMap = new HashMap<>();
        Map<String, T> updateMap = new HashMap<>();
        
        for (Change change : listDiff.getChanges()) {
            if ((change instanceof NewObject)) {
                addMap.put(change.getAffectedLocalId().toString(), (T) change.getAffectedObject().get());
            }
            
            if ((change instanceof ObjectRemoved)) {
                removeMap.put(change.getAffectedLocalId().toString(), (T) change.getAffectedObject().get());
            }
            
            if ((change instanceof PropertyChange)) {
                if (change.getAffectedLocalId() != null) {
                    updateMap.put(change.getAffectedLocalId().toString(), (T) change.getAffectedObject().get());
                }
            }
        }
        if (DataUtils.isAllNotNull(addMap.values(), addConsume)) {
            addConsume.accept((new ArrayList<>(addMap.values())));
        }
        if (DataUtils.isAllNotNull(updateMap.values(), updateConsume)) {
            updateConsume.accept(new ArrayList<>(updateMap.values()));
        }
        if (DataUtils.isAllNotNull(removeMap.values(), removeConsume)) {
            removeConsume.accept((new ArrayList<>(removeMap.values())));
        }
    }
    
    /**
     * id初始化负数
     *
     * @param clazz
     * @param list
     * @param <T>
     */
    public static <T> void initNegativeId(Class<T> clazz, Collection<T> list, String fileName) {
        // 检查是否有id 属性
        if (ReflectionUtils.isExistFieldName(fileName, clazz, Id.class)) {
            for (T t : list) {
                Object id = ReflectionUtils.getPropertyValue(t, fileName);
                if (DataUtils.isEmpty(id)) {
                    ReflectionUtils.setPropertyValue(t, fileName, DataUtils.randomLongId());
                }
            }
        }
    }
    
    public static <T> void initNegativeId(Class<T> clazz, Collection<T> list) {
        initNegativeId(clazz, list, "id");
    }
    
    /**
     * map 里面的key 是@id 的值,需要用到long类型 需要自己强转
     *
     * @param oldList
     * @param newList
     * @param clazz
     * @param addConsume
     * @param updateConsume
     * @param removeConsume
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public static <T> void collectionChangeFunction(Collection<T> oldList,
                                                    Collection<T> newList,
                                                    Class<T> clazz,
                                                    Consumer<Map<String, T>> addConsume,
                                                    Consumer<Map<String, T>> updateConsume,
                                                    Consumer<Map<String, T>> removeConsume) {
        
        Diff listDiff = javers.compareCollections(oldList, newList, clazz);
        
        Map<String, T> addMap = new HashMap<>();
        Map<String, T> removeMap = new HashMap<>();
        Map<String, T> updateMap = new HashMap<>();
        
        for (Change change : listDiff.getChanges()) {
            if ((change instanceof NewObject)) {
                addMap.put(change.getAffectedLocalId().toString(), (T) change.getAffectedObject().get());
            }
            
            if ((change instanceof ObjectRemoved)) {
                removeMap.put(change.getAffectedLocalId().toString(), (T) change.getAffectedObject().get());
            }
            
            if ((change instanceof PropertyChange)) {
                if (change.getAffectedLocalId() != null) {
                    updateMap.put(change.getAffectedLocalId().toString(), (T) change.getAffectedObject().get());
                }
            }
        }
        if (!addMap.isEmpty()) {
            addConsume.accept(addMap);
        }
        if (!updateMap.isEmpty()) {
            updateConsume.accept(updateMap);
        }
        if (!removeMap.isEmpty()) {
            removeConsume.accept(removeMap);
        }
    }
    
    /**
     * 获取需要比较的简单属性
     *
     * @param clazz clazz
     * @return 需要进行比较的简单属性名称集合
     */
    public static List<String> simpleProperties(Class<?> clazz) {
        return PROPERTIES_CACHE.computeIfAbsent(clazz.getName() + "-simpleProperties", k -> {
            Field[] fields = clazz.getDeclaredFields();
            ArrayList<String> properties = Lists.newArrayList();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(DiffIgnore.class)) {
                    continue;
                }
                if (Collection.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                try {
                    Method getM = clazz.getMethod("get" + StringUtils.firstUpperCase(field.getName()));
                    Method setM = clazz.getMethod("set" + StringUtils.firstUpperCase(field.getName()), field.getType());
                    properties.add(field.getName());
                } catch (NoSuchMethodException e) {
                    log.info("{} No Such Method {} Or {}", clazz, "get" + StringUtils.firstUpperCase(field.getName()), "set" + StringUtils.firstUpperCase(field.getName()));
                }
            }
            return properties;
        });
    }
    
    /**
     * 用于Long和Integer类型的比较
     *
     * @param clazz Long or Integer
     */
    @SuppressWarnings("unchecked")
    public static <T> void simpleListChangeFunction(Collection<T> oldList,
                                                    Collection<T> newList,
                                                    Class<T> clazz,
                                                    Consumer<List<T>> addConsume,
                                                    Consumer<List<T>> removeConsume) {
        
        Diff listDiff = javers.compareCollections(oldList, newList, clazz);
        
        List<T> addList = new ArrayList<>();
        List<T> removeList = new ArrayList<>();
        
        for (Change change : listDiff.getChanges()) {
            if (change instanceof ListChange && change.getAffectedLocalId() == null) {
                for (ContainerElementChange containerElementChange : ((ListChange) change).getChanges()) {
                    if (containerElementChange instanceof ValueAdded) {
                        Object addedValue = ((ValueAdded) containerElementChange).getAddedValue();
                        addList.add((T) addedValue);
                    } else if (containerElementChange instanceof ValueRemoved) {
                        Object removedValue = ((ValueRemoved) containerElementChange).getRemovedValue();
                        removeList.add((T) removedValue);
                    } else if (containerElementChange instanceof ElementValueChange) {
                        Object leftValue = ((ElementValueChange) containerElementChange).getLeftValue();
                        Object rightValue = ((ElementValueChange) containerElementChange).getRightValue();
                        addList.add((T) rightValue);
                        removeList.add((T) leftValue);
                    }
                }
            }
        }
        if (DataUtils.isNotEmpty(addList)) {
            addConsume.accept(addList);
        }
        if (DataUtils.isNotEmpty(removeList)) {
            removeConsume.accept(removeList);
        }
    }
}
