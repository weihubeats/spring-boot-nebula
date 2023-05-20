package com.nebula.web.boot.resolver;

import com.nebula.base.utils.DataUtils;
import com.nebula.base.utils.TimeUtil;
import com.nebula.web.boot.annotation.GetTimestamp;
import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.servlet.ServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.ServletRequestParameterPropertyValues;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 *@author : wh
 *@date : 2022/9/17 16:13
 *@description:
 */
public class TimestampArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter methodParameter) {
		return methodParameter.hasParameterAnnotation(GetTimestamp.class);
	}

	@Override
	public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
		GetTimestamp getTimestamp = methodParameter.getParameterAnnotation(GetTimestamp.class);
		Object instance = methodParameter.getParameterType().getDeclaredConstructor().newInstance();
		String name = getTimestamp.name();
		if (DataUtils.isEmpty(name)) {
			name = methodParameter.getParameterName();
		}
		if (instance instanceof LocalDateTime) {
			assert name != null;
			String parameter = nativeWebRequest.getParameter(name);
			return parse(parameter);
		}

		WebDataBinder binder = webDataBinderFactory.createBinder(nativeWebRequest, instance, name);
		binder.registerCustomEditor(LocalDateTime.class, new LocalDateTimeEditor());

		ServletRequestParameterPropertyValues propertyValues = new ServletRequestParameterPropertyValues(Objects.requireNonNull(nativeWebRequest.getNativeRequest(ServletRequest.class)));

		binder.bind(propertyValues);
		return instance;
	}

	class LocalDateTimeEditor extends PropertyEditorSupport {
		@Override
		public String getAsText() {
			final LocalDateTime localDateTime = (LocalDateTime) getValue();
			if (localDateTime == null) {
				return "";
			}
			return localDateTime.toString();
		}

		@Override
		public void setAsText(String text) throws IllegalArgumentException {
			setValue(parse(text));
		}
	}

	private LocalDateTime parse(String timestampStr) {
		if (DataUtils.isEmpty(timestampStr)) {
			return null;
		}
		timestampStr = timestampStr.replaceAll("\"", "");
		long timestamp = Long.parseLong(timestampStr);
		if (timestamp == 0) {
			return null;
		}
		return TimeUtil.toLocalDateTime(timestamp);
	}
	
	
	
}
