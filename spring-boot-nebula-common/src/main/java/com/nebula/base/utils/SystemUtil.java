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
