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
 
package com.nebula.web.boot.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nebula.web.boot.api.NebulaResponse;
import com.nebula.web.boot.enums.ResultCode;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author : wh
 * @date : 2026/7/15
 * @description: 协议层 code 映射（内部 int → 对外 Integer/String）
 */
class ResponseCodeWireTest {
    
    @Test
    void defaultSuccessWireCodeIsInteger200() {
        NebulaWebProperties properties = new NebulaWebProperties();
        Object wire = properties.toWireCode(ResultCode.SUCCESS.getCode());
        assertInstanceOf(Integer.class, wire);
        assertEquals(200, wire);
    }
    
    @Test
    void successResponseCodeAsString() {
        NebulaWebProperties properties = new NebulaWebProperties();
        properties.setResponseCode("Success");
        assertEquals("Success", properties.toWireCode(ResultCode.SUCCESS.getCode()));
    }
    
    @Test
    void codeMappingOverridesSuccessAndFailure() {
        NebulaWebProperties properties = new NebulaWebProperties();
        properties.setCodeMapping(Map.of(
                ResultCode.SUCCESS.getCode(), "Success",
                ResultCode.FAILURE.getCode(), "Failure",
                ResultCode.INTERNAL_SERVER_ERROR.getCode(), "Error"));
        assertEquals("Success", properties.toWireCode(ResultCode.SUCCESS.getCode()));
        assertEquals("Failure", properties.toWireCode(ResultCode.FAILURE.getCode()));
        assertEquals("Error", properties.toWireCode(ResultCode.INTERNAL_SERVER_ERROR.getCode()));
    }
    
    @Test
    void unmappedFailureKeepsInt() {
        NebulaWebProperties properties = new NebulaWebProperties();
        Object wire = properties.toWireCode(ResultCode.FAILURE.getCode());
        assertEquals(ResultCode.FAILURE.getCode(), wire);
    }
    
    @Test
    void parseWireValueNumericAndText() {
        assertEquals(200, NebulaWebProperties.parseWireValue("200"));
        assertEquals("Success", NebulaWebProperties.parseWireValue("Success"));
        assertEquals(200, NebulaWebProperties.parseWireValue(""));
    }
    
    @Test
    void isSuccessRecognizesDefaultIntCode() {
        NebulaResponse<String> response = NebulaResponse.data("ok", "success");
        assertTrue(NebulaResponse.isSuccess(response));
        assertEquals(200, response.getCode());
    }
    
    @Test
    void resultCodeImplementsIErrorCodeWithInt() {
        assertEquals(200, ResultCode.SUCCESS.getCode());
        assertEquals("success", ResultCode.SUCCESS.getMessage());
    }
}
