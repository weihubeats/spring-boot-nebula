package com.nebula.aggregate.core;

import org.javers.core.metamodel.annotation.DiffIgnore;

/**
 * @author : wh
 * @date : 2023/12/8 11:11
 * @description:
 */
public abstract class AbstractOldObj<T> {

    @DiffIgnore
    private T oldObject;


    public void setOldObject(T oldObject) {
        this.oldObject = oldObject;
    }

    public T getOld() {
        return oldObject;
    }

}
