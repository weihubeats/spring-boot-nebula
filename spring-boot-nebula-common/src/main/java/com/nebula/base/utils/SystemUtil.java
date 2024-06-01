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
 
package com.nebula.base.utils;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;

/**
 * @author : wh
 * @date : 2023/11/18 11:28
 * @description:
 */
public class SystemUtil {
    
    /**
     * Returns system property or {@code null} if not set.
     */
    public static String get(final String name) {
        return get(name, null);
    }
    
    /**
     * Returns system property. If key is not available, returns the default value.
     */
    public static String get(final String name, final String defaultValue) {
        Objects.requireNonNull(name);
        
        String value = null;
        try {
            if (System.getSecurityManager() == null) {
                value = System.getProperty(name);
            } else {
                value = AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(name));
            }
        } catch (final Exception ignore) {
        }
        
        if (value == null) {
            return defaultValue;
        }
        
        return value;
    }
    
    /**
     * Returns system property as boolean.
     */
    public static boolean getBoolean(final String name, final boolean defaultValue) {
        String value = get(name);
        if (value == null) {
            return defaultValue;
        }
        
        value = value.trim().toLowerCase();
        
        switch (value) {
            case "true":
            case "yes":
            case "1":
            case "on":
                return true;
            case "false":
            case "no":
            case "0":
            case "off":
                return false;
            default:
                return defaultValue;
        }
    }
    
    /**
     * Returns system property as an int.
     */
    public static long getInt(final String name, final int defaultValue) {
        String value = get(name);
        if (value == null) {
            return defaultValue;
        }
        
        value = value.trim().toLowerCase();
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException nfex) {
            return defaultValue;
        }
    }
    
    /**
     * Returns system property as a long.
     */
    public static long getLong(final String name, final long defaultValue) {
        String value = get(name);
        if (value == null) {
            return defaultValue;
        }
        
        value = value.trim().toLowerCase();
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException nfex) {
            return defaultValue;
        }
    }
    
    /**
     * 获取CPU核数
     *
     * @return
     */
    public static int getCPU() {
        return Runtime.getRuntime().availableProcessors();
    }
    
    // ---------------------------------------------------------------- infos
    
    private static final SystemInfo systemInfo = new SystemInfo();
    
    /**
     * Returns system information.
     */
    public static SystemInfo info() {
        return systemInfo;
    }
    
}
