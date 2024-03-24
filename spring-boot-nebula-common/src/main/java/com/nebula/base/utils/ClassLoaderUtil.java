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

import com.nebula.base.utils.io.IOUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author : wh
 * @date : 2023/11/18 13:57
 * @description:
 */
public class ClassLoaderUtil {
    
    public static ClassLoaderStrategy classLoaderStrategy = new ClassLoaderStrategy.DefaultClassLoaderStrategy();
    
    // ---------------------------------------------------------------- default class loader
    
    /**
     * Returns default class loader. By default, it is {@link #getContextClassLoader() threads context class loader}.
     * If this one is <code>null</code>, then class loader of the <b>caller class</b> is returned.
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = getContextClassLoader();
        if (cl == null) {
            final Class callerClass = ClassUtil.getCallerClass(2);
            cl = callerClass.getClassLoader();
        }
        return cl;
    }
    
    /**
     * Returns thread context class loader.
     */
    public static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            return AccessController.doPrivileged(
                    (PrivilegedAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader());
        }
    }
    
    /**
     * Returns system class loader.
     */
    public static ClassLoader getSystemClassLoader() {
        if (System.getSecurityManager() == null) {
            return ClassLoader.getSystemClassLoader();
        } else {
            return AccessController.doPrivileged(
                    (PrivilegedAction<ClassLoader>) ClassLoader::getSystemClassLoader);
        }
    }
    
    // ---------------------------------------------------------------- classpath
    
    private static final String[] MANIFESTS = {"Manifest.mf", "manifest.mf", "MANIFEST.MF"};
    
    /**
     * Returns classpath item manifest or <code>null</code> if not found.
     */
    public static Manifest getClasspathItemManifest(final File classpathItem) {
        Manifest manifest = null;
        
        if (classpathItem.isFile()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(classpathItem);
                final JarFile jar = new JarFile(classpathItem);
                manifest = jar.getManifest();
            } catch (final IOException ignore) {
            } finally {
                IOUtil.close(fis);
            }
        } else {
            final File metaDir = new File(classpathItem, "META-INF");
            File manifestFile = null;
            if (metaDir.isDirectory()) {
                for (final String m : MANIFESTS) {
                    final File mFile = new File(metaDir, m);
                    if (mFile.isFile()) {
                        manifestFile = mFile;
                        break;
                    }
                }
            }
            if (manifestFile != null) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(manifestFile);
                    manifest = new Manifest(fis);
                } catch (final IOException ignore) {
                } finally {
                    IOUtil.close(fis);
                }
            }
        }
        
        return manifest;
    }
    
    /**
     * Returns base folder for classpath item. If item is a (jar) file,
     * its parent is returned. If item is a directory, its name is returned.
     */
    public static String getClasspathItemBaseDir(final File classpathItem) {
        final String base;
        if (classpathItem.isFile()) {
            base = classpathItem.getParent();
        } else {
            base = classpathItem.toString();
        }
        return base;
    }
    
    // ---------------------------------------------------------------- class stream
    
    /**
     * Opens a class of the specified name for reading using class classloader.
     */
    public static InputStream getClassAsStream(final Class clazz) throws IOException {
        return ResourcesUtil.getResourceAsStream(ClassUtil.convertClassNameToFileName(clazz), clazz.getClassLoader());
    }
    
    /**
     * Opens a class of the specified name for reading. No specific classloader is used
     * for loading class.
     */
    public static InputStream getClassAsStream(final String className) throws IOException {
        return ResourcesUtil.getResourceAsStream(ClassUtil.convertClassNameToFileName(className));
    }
    
    /**
     * Opens a class of the specified name for reading using provided class loader.
     */
    public static InputStream getClassAsStream(final String className,
                                               final ClassLoader classLoader) throws IOException {
        return ResourcesUtil.getResourceAsStream(ClassUtil.convertClassNameToFileName(className), classLoader);
    }
    
    // ---------------------------------------------------------------- load class
    
    /**
     * Loads a class using default class loader strategy.
     *
     * @see ClassLoaderStrategy.DefaultClassLoaderStrategy
     */
    public static Class loadClass(final String className) throws ClassNotFoundException {
        return classLoaderStrategy.loadClass(className, null);
    }
    
    /**
     * Loads a class using default class loader strategy.
     *
     * @see ClassLoaderStrategy.DefaultClassLoaderStrategy
     */
    public static Class loadClass(final String className, final ClassLoader classLoader) throws ClassNotFoundException {
        return classLoaderStrategy.loadClass(className, classLoader);
    }
    
    // ---------------------------------------------------------------- class location
    
    /**
     * Returns location of the class. If class is not in a jar, it's classpath
     * is returned; otherwise the jar location.
     */
    public static String classLocation(final Class clazz) {
        return clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
    }
    
}
