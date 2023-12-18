package com.nebula.base.utils;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author : wh
 * @date : 2023/12/18 13:56
 * @description:
 */
public interface PropertyFunc<T, R> extends Function<T, R>, Serializable {

}
