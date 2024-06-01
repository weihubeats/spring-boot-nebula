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

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * @author : wh
 * @date : 2023/11/18 14:06
 * @description:
 */
@FunctionalInterface
public interface ClassLoaderStrategy {
    
    /**
     * Loads class with given name and optionally provided class loader.
     */
    Class loadClass(String className, ClassLoader classLoader) throws ClassNotFoundException;
    
    /**
     * Default Jodd class loader strategy.
     * Loads a class with a given name dynamically, more reliable then <code>Class.forName</code>.
     * <p>
     * Class will be loaded using class loaders in the following order:
     * <ul>
     * <li>provided class loader (if any)</li>
     * <li><code>Thread.currentThread().getContextClassLoader()}</code></li>
     * <li>caller classloader</li>
     * </ul>
     */
    class DefaultClassLoaderStrategy implements ClassLoaderStrategy {
        
        /**
         * List of primitive type names.
         */
        public static final String[] PRIMITIVE_TYPE_NAMES = new String[]{
                "boolean", "byte", "char", "double", "float", "int", "long", "short",
        };
        /**
         * List of primitive types that matches names list.
         */
        public static final Class[] PRIMITIVE_TYPES = new Class[]{
                boolean.class, byte.class, char.class, double.class, float.class, int.class, long.class, short.class,
        };
        /**
         * List of primitive bytecode characters that matches names list.
         */
        public static final char[] PRIMITIVE_BYTECODE_NAME = new char[]{
                'Z', 'B', 'C', 'D', 'F', 'I', 'J', 'S'
        };
        
        // ---------------------------------------------------------------- flags
        
        protected boolean loadArrayClassByComponentTypes = false;
        
        /**
         * Returns arrays class loading strategy.
         */
        public boolean isLoadArrayClassByComponentTypes() {
            return loadArrayClassByComponentTypes;
        }
        
        /**
         * Defines arrays class loading strategy.
         * If <code>false</code> (default), classes will be loaded by <code>Class.forName</code>.
         * If <code>true</code>, classes will be loaded by reflection and component types.
         */
        public void setLoadArrayClassByComponentTypes(final boolean loadArrayClassByComponentTypes) {
            this.loadArrayClassByComponentTypes = loadArrayClassByComponentTypes;
        }
        
        // ---------------------------------------------------------------- names
        
        /**
         * Prepares classname for loading, respecting the arrays.
         * Returns <code>null</code> if class name is not an array.
         */
        public static String prepareArrayClassnameForLoading(String className) {
            final int bracketCount = StringUtils.count(className, '[');
            
            if (bracketCount == 0) {
                // not an array
                return null;
            }
            
            final String brackets = StringUtils.repeat('[', bracketCount);
            
            final int bracketIndex = className.indexOf('[');
            className = className.substring(0, bracketIndex);
            
            final int primitiveNdx = getPrimitiveClassNameIndex(className);
            if (primitiveNdx >= 0) {
                className = String.valueOf(PRIMITIVE_BYTECODE_NAME[primitiveNdx]);
                
                return brackets + className;
            } else {
                return brackets + 'L' + className + ';';
            }
        }
        
        /**
         * Detects if provided class name is a primitive type.
         * Returns >= 0 number if so.
         */
        private static int getPrimitiveClassNameIndex(final String className) {
            final int dotIndex = className.indexOf('.');
            if (dotIndex != -1) {
                return -1;
            }
            return Arrays.binarySearch(PRIMITIVE_TYPE_NAMES, className);
        }
        
        // ---------------------------------------------------------------- load
        
        /**
         * Loads class by name.
         */
        @Override
        public Class loadClass(final String className, final ClassLoader classLoader) throws ClassNotFoundException {
            final String arrayClassName = prepareArrayClassnameForLoading(className);
            
            if ((className.indexOf('.') == -1) && (arrayClassName == null)) {
                // maybe a primitive
                final int primitiveNdx = getPrimitiveClassNameIndex(className);
                if (primitiveNdx >= 0) {
                    return PRIMITIVE_TYPES[primitiveNdx];
                }
            }
            
            // try #1 - using provided class loader
            if (classLoader != null) {
                final Class klass = loadClass(className, arrayClassName, classLoader);
                
                if (klass != null) {
                    return klass;
                }
            }
            
            // try #2 - using thread class loader
            final ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
            
            if ((currentThreadClassLoader != null) && (currentThreadClassLoader != classLoader)) {
                final Class klass = loadClass(className, arrayClassName, currentThreadClassLoader);
                
                if (klass != null) {
                    return klass;
                }
            }
            
            // try #3 - using caller classloader, similar as Class.forName()
            // Class callerClass = ReflectUtil.getCallerClass(2);
            final Class callerClass = ClassUtil.getCallerClass();
            final ClassLoader callerClassLoader = callerClass.getClassLoader();
            
            if ((callerClassLoader != classLoader) && (callerClassLoader != currentThreadClassLoader)) {
                final Class klass = loadClass(className, arrayClassName, callerClassLoader);
                
                if (klass != null) {
                    return klass;
                }
            }
            
            // try #4 - everything failed, try alternative array loader
            if (arrayClassName != null) {
                try {
                    return loadArrayClassByComponentType(className, classLoader);
                } catch (final ClassNotFoundException ignore) {
                }
            }
            
            throw new ClassNotFoundException("Class not found: " + className);
        }
        
        /**
         * Loads a class using provided class loader.
         * If class is an array, it will be first loaded using the <code>Class.forName</code>!
         * We must use this since for JDK {@literal >=} 6 arrays will be not loaded using classloader,
         * but only with <code>forName</code> method. However, array loading strategy can be
         * {@link #setLoadArrayClassByComponentTypes(boolean) changed}.
         */
        protected Class loadClass(final String className, final String arrayClassName, final ClassLoader classLoader) {
            if (arrayClassName != null) {
                try {
                    if (loadArrayClassByComponentTypes) {
                        return loadArrayClassByComponentType(className, classLoader);
                    } else {
                        return Class.forName(arrayClassName, true, classLoader);
                    }
                } catch (final ClassNotFoundException ignore) {
                }
            }
            
            try {
                return classLoader.loadClass(className);
            } catch (final ClassNotFoundException ignore) {
            }
            
            return null;
        }
        
        /**
         * Loads array class using component type.
         */
        protected Class loadArrayClassByComponentType(final String className,
                                                      final ClassLoader classLoader) throws ClassNotFoundException {
            final int ndx = className.indexOf('[');
            final int multi = StringUtils.count(className, '[');
            
            final String componentTypeName = className.substring(0, ndx);
            
            final Class componentType = loadClass(componentTypeName, classLoader);
            
            if (multi == 1) {
                return Array.newInstance(componentType, 0).getClass();
            }
            
            final int[] multiSizes;
            
            if (multi == 2) {
                multiSizes = new int[]{0, 0};
            } else if (multi == 3) {
                multiSizes = new int[]{0, 0, 0};
            } else {
                multiSizes = (int[]) Array.newInstance(int.class, multi);
            }
            
            return Array.newInstance(componentType, multiSizes).getClass();
        }
        
    }
}
