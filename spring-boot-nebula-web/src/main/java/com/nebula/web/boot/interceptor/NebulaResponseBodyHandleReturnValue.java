package com.nebula.web.boot.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nebula.base.utils.DataUtils;
import com.nebula.base.utils.JsonUtil;
import com.nebula.web.boot.annotation.NebulaResponseBody;
import com.nebula.web.boot.api.NebulaResponse;
import com.nebula.web.boot.enums.ResultCode;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.AsyncHandlerMethodReturnValueHandler;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * handler response value of all http api
 */
public class NebulaResponseBodyHandleReturnValue implements HandlerMethodReturnValueHandler, AsyncHandlerMethodReturnValueHandler {
    /**
     * 处理所有非异常的错误
     *
     * @param returnType
     * @return
     */
    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        //如果已经是基础的返回值
        return returnType.getParameterType() != NebulaResponseBody.class
                && DataUtils.isNotEmpty(returnType.getAnnotatedElement().getAnnotation(NebulaResponseBody.class));
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        mavContainer.setRequestHandled(true);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        assert response != null;
        response.setContentType("application/json;charset=utf-8");
        NebulaResponse<Object> baseResponse = new NebulaResponse<>();
        baseResponse.setCode(ResultCode.SUCCESS.getCode());
        baseResponse.setMsg(ResultCode.SUCCESS.getMessage());
        baseResponse.setData(returnValue);

        NebulaResponseBody responseBody = returnType.getAnnotatedElement().getAnnotation(NebulaResponseBody.class);
        Class<? extends ObjectMapper> objectMapperClass = responseBody.objectMapper();
        if (Objects.equals(objectMapperClass, JsonUtil.JacksonObjectMapper.class)) {
            response.getWriter().write(Objects.requireNonNull(JsonUtil.toJSONString(baseResponse)));
        } else {
            ObjectMapper objectMapper = objectMapperClass.getDeclaredConstructor().newInstance();
            response.getWriter().write(Objects.requireNonNull(JsonUtil.toJSONString(objectMapper, baseResponse)));
        }

    }

    @Override
    public boolean isAsyncReturnValue(Object returnValue, MethodParameter returnType) {
        return supportsReturnType(returnType);
    }
}
