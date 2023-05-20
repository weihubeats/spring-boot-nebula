package com.nebula.base.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * @author : wh
 * @date : 2023/4/13 10:11
 * @description:
 */
public class ExceptionUtil {

    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        }
    }

    /**
     * 将CheckedException转换为UncheckedException.
     *
     * @param e Throwable
     * @return {RuntimeException}
     */
    public static RuntimeException unchecked(Throwable e) {
        if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException
            || e instanceof NoSuchMethodException) {
            return new IllegalArgumentException(e);
        } else if (e instanceof InvocationTargetException) {
            return new RuntimeException(((InvocationTargetException) e).getTargetException());
        } else if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else {
            return new RuntimeException(e);
        }
    }

    /**
     * 代理异常解包
     *
     * @param wrapped 包装过得异常
     * @return 解包后的异常
     */
    public static Throwable unwrap(Throwable wrapped) {
        Throwable unwrapped = wrapped;
        while (true) {
            if (unwrapped instanceof InvocationTargetException) {
                unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
            } else if (unwrapped instanceof UndeclaredThrowableException) {
                unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
            } else {
                return unwrapped;
            }
        }
    }

    /**
     * 获取根异常
     *
     * @param throwable
     * @return
     */
    public static Throwable getRootCause(final Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause == null) {
            return throwable;
        }

        Throwable t = throwable;

        // defend against (malicious?) circularity
        for (int i = 0; i < 1000; i++) {
            cause = t.getCause();
            if (cause == null) {
                return t;
            }
            t = cause;
        }
        return throwable;
    }

    /**
     * 构建异常
     *
     * @param message
     * @param cause
     * @return
     */
    public static String buildMessage(final String message, Throwable cause) {
        if (cause != null) {
            cause = getRootCause(cause);
            StringBuilder buf = new StringBuilder();
            if (message != null) {
                buf.append(message).append("; ");
            }
            buf.append("<--- ").append(cause);
            return buf.toString();
        } else {
            return message;
        }
    }

}
