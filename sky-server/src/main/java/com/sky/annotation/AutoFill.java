package com.sky.annotation;

import com.sky.enumeration.OperationType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//annotation 注解

/**
 * 自定义注解，用于标识某个方法需要进行功能字段自动填充处理
 */
@Target(ElementType.METHOD)//元注解：可以写在注解上面的注解  @Target ：指定注解能在哪里使用
@Retention(RetentionPolicy.RUNTIME)// @Retention ：可以理解为保留时间(生命周期)
public @interface AutoFill {
    //数据库操作类型：UPDATA INSERT   //下面value的属性只在up和insert操作中用到
    OperationType value();
}//
