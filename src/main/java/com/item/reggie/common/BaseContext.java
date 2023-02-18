package com.item.reggie.common;

/**
 * 基于threadlocal封装的工具类，用于保存和获取当前登录的用户ID
 */
public class BaseContext {
    public static ThreadLocal<Long> threadLocal=new ThreadLocal<>();

    /**
     * 设置ID
     * @param Id
     */
    public static void setCurrentId(Long Id){
        threadLocal.set(Id);
    }

    /**
     * 获取id
     * @return
     */
    public static Long getCurrentId(){
        return threadLocal.get();
    }

}
