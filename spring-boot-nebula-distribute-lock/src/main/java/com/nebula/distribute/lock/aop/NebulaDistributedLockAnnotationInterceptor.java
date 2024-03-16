package com.nebula.distribute.lock.aop;

import com.nebula.base.utils.DataUtils;
import com.nebula.distribute.lock.annotation.NebulaDistributedLock;
import com.nebula.distribute.lock.core.DistributedLock;
import com.nebula.distribute.lock.core.NebulaDistributedLockTemplate;
import com.nebula.web.common.utils.ExpressionUtil;
import java.lang.reflect.Method;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author : wh
 * @date : 2024/3/15 13:34
 * @description:
 */
@Slf4j
public class NebulaDistributedLockAnnotationInterceptor implements MethodInterceptor {

    private final NebulaDistributedLockTemplate lock;

    public NebulaDistributedLockAnnotationInterceptor(NebulaDistributedLockTemplate lock) {
        if (DataUtils.isEmpty(lock)) {
            throw new RuntimeException("DistributedLockTemplate is null");
        }
        this.lock = lock;
    }

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation methodInvocation) {
        Method method = methodInvocation.getMethod();
        NebulaDistributedLock annotation = method.getAnnotation(NebulaDistributedLock.class);
        Object[] args = methodInvocation.getArguments();
        String lockName = getLockName(annotation, args, method);
        if (log.isDebugEnabled()) {
            log.debug("lockName: {}", lockName);
        }
        boolean fairLock = annotation.fairLock();
        if (annotation.tryLock()) {
            return lock.tryLock(new DistributedLock<>() {
                @Override
                public Object process() {
                    return proceed(methodInvocation);
                }

                @Override
                public String lockName() {
                    return lockName;
                }
            }, annotation.tryWaitTime(), annotation.outTime(), annotation.timeUnit(), fairLock);
        } else {
            return lock.lock(new DistributedLock<>() {
                @Override
                public Object process() {
                    return proceed(methodInvocation);
                }

                @Override
                public String lockName() {
                    return lockName;
                }
            }, annotation.outTime(), annotation.timeUnit(), fairLock);
        }
    }

    public Object proceed(MethodInvocation methodInvocation) {
        try {
            return methodInvocation.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 获取锁名字，优先获取注解中锁名
     *
     * @param nebulaDistributedLock
     * @param args
     * @param method
     * @return
     */
    private String getLockName(NebulaDistributedLock nebulaDistributedLock, Object[] args, Method method) {
        if (DataUtils.isNotEmpty(nebulaDistributedLock.lockName())) {
            return nebulaDistributedLock.lockName();
        }
        String lockNamePre = nebulaDistributedLock.lockNamePre();
        String lockNamePost = nebulaDistributedLock.lockNamePost();
        String separator = nebulaDistributedLock.separator();

        if (ExpressionUtil.isEl(lockNamePre)) {
            lockNamePre = (String) ExpressionUtil.parse(lockNamePre, method, args);
        }
        if (ExpressionUtil.isEl(lockNamePost)) {
            lockNamePost = Objects.requireNonNull(ExpressionUtil.parse(lockNamePost, method, args)).toString();
        }

        StringBuilder sb = new StringBuilder();
        if (DataUtils.isNotEmpty(lockNamePre)) {
            sb.append(lockNamePre);
        }
        sb.append(separator);
        if (DataUtils.isNotEmpty(lockNamePost)) {
            sb.append(lockNamePost);
        }
        return sb.toString();
    }
}
