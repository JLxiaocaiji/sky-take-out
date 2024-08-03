package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 自定义注解，标识某个方法需要进行功能字段自动填充处理
 */

@Target(ElementType.METHOD) // 定义一个可以用于方法的注解
public @interface AutoFill {
    // 需要一个属性来指定当前数据库操作类型
    // 数据库操作类型， UPDATE, INSERT
    OperationType value();  // 返回枚举常量（enum constant）的值
}
