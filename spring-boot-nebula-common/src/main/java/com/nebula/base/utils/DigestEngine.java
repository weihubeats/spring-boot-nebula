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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * @author : wh
 * @date : 2023/11/18 14:05
 * @description:
 */
public interface DigestEngine {
    
    class JavaDigestEngine implements DigestEngine {
        
        private final MessageDigest messageDigest;
        
        JavaDigestEngine(final String algorithm) {
            try {
                this.messageDigest = MessageDigest.getInstance(algorithm);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public byte[] digest(final byte[] byteArray) {
            messageDigest.update(byteArray);
            return messageDigest.digest();
        }
        
        @Override
        public byte[] digest(final File file) throws IOException {
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            DigestInputStream dis = null;
            
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                dis = new DigestInputStream(bis, messageDigest);
                
                while (dis.read() != -1) {
                }
            } finally {
                IOUtil.close(dis);
                IOUtil.close(bis);
                IOUtil.close(fis);
            }
            
            return messageDigest.digest();
        }
    }
    
    /**
     * Creates new MD2 digest.
     */
    public static DigestEngine md2() {
        return new JavaDigestEngine("MD2");
    }
    /**
     * Creates new MD5 digest.
     */
    public static DigestEngine md5() {
        return new JavaDigestEngine("MD5");
    }
    /**
     * Creates new SHA-1 digest.
     */
    public static DigestEngine sha1() {
        return new JavaDigestEngine("SHA-1");
    }
    /**
     * Creates new SHA-256 digest.
     */
    public static DigestEngine sha256() {
        return new JavaDigestEngine("SHA-256");
    }
    /**
     * Creates new SHA-384 digest.
     */
    public static DigestEngine sha384() {
        return new JavaDigestEngine("SHA-384");
    }
    /**
     * Creates new SHA-512 digest.
     */
    public static DigestEngine sha512() {
        return new JavaDigestEngine("SHA-512");
    }
    
    /**
     * Returns byte-hash of input byte array.
     */
    public byte[] digest(byte[] input);
    
    /**
     * Returns byte-hash of input string.
     */
    public default byte[] digest(final String input) {
        return digest(input.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Returns digest of a file. Implementations may not read the whole
     * file into the memory.
     */
    public byte[] digest(final File file) throws IOException;
    
    /**
     * Returns string hash of input byte array.
     */
    public default String digestString(final byte[] byteArray) {
        return StringUtils.toHexString(digest(byteArray));
    }
    
    /**
     * Returns string hash of input string.
     */
    public default String digestString(final String input) {
        return StringUtils.toHexString(digest(input));
    }
    
    public default String digestString(final File file) throws IOException {
        return StringUtils.toHexString(digest(file));
    }
    
}
