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
 
package com.nebula.base.utils.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileUtilReadBytesTest {
    
    @TempDir
    File tempDir;
    
    @Test
    void readBytesFullContent() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        File f = new File(tempDir, "a.bin");
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(data);
        }
        assertArrayEquals(data, FileUtil.readBytes(f));
        // 再读一次验证句柄已正常关闭（try-with-resources 后文件可重复读取）
        assertArrayEquals(data, FileUtil.readBytes(f));
    }
    
    @Test
    void readBytesEmptyFile() throws IOException {
        File f = new File(tempDir, "empty.bin");
        Files.createFile(f.toPath());
        assertArrayEquals(new byte[0], FileUtil.readBytes(f));
    }
    
    @Test
    void readBytesWithCountLimit() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        File f = new File(tempDir, "b.bin");
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(data);
        }
        assertArrayEquals(new byte[]{1, 2, 3}, FileUtil.readBytes(f, 3));
    }
    
    @Test
    void readBytesMissingFileThrows() {
        File missing = new File(tempDir, "missing.bin");
        assertThrows(IOException.class, () -> FileUtil.readBytes(missing));
    }
}
