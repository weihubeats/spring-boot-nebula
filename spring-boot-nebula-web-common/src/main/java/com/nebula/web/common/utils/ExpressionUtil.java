package com.nebula.web.common.utils;

import com.nebula.base.utils.DataUtils;
import java.lang.reflect.Method;
import java.util.Objects;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * @author : wh
 * @date : 2024/3/16 10:20
 * @description:
 */
public class ExpressionUtil {

    /**
     * el表达式解析
     *
     * @param expressionString 解析值
     * @param method           方法
     * @param args             参数
     * @return
     */
    public static Object parse(String expressionString, Method method, Object[] args) {
        if (DataUtils.isEmpty(expressionString)) {
            return null;
        }
        //获取被拦截方法参数名列表
        LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNameArr = discoverer.getParameterNames(method);
        //SPEL解析
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < Objects.requireNonNull(paramNameArr).length; i++) {
            context.setVariable(paramNameArr[i], args[i]);
        }
        return parser.parseExpression(expressionString).getValue(context);
    }

    public static boolean isEl(String param) {
        if (DataUtils.isEmpty(param)) {
            return false;
        }
        return Objects.equals(param.substring(0, 1), "#");
    }
}
