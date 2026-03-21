package com.sky.aspect;



import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import org.aspectj.lang.reflect.MethodSignature;
//import jdk.internal.org.jline.terminal.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

//这里如果getMethod爆红,看一下导包对没有，正确导包是import org.aspectj.lang.reflect.MethodSignature;
/**
 *  自定义切面，实现公共字段自动填充处理逻辑
 */
@Aspect
@Component
@Slf4j

public class AutoFillAspect {


    /**
     * 切入点
     * 切面=切入点+通知
     */

    //直接annotation会扫描全部，影响效率，加上execution就会只扫描mp里面带AutoFill注解的方法
    //execution直接锁定com.sky.mapper下的所有方法和类
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {}


    /**
     * 前置通知，在通知中进行公共字段的复制
     */

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充...");

        //获取到当前被拦截的方法上的数据库操作类型
        MethodSignature signature =  (MethodSignature)joinPoint.getSignature();//方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获取方法上的注解对象
        OperationType operationType = autoFill.value();//获得数据库操作类型
        //通过上述三行代码，获取EmployeeMapper中UPDATE的操作类型
        //前三行代码的作用仅仅是为了获取mapper上的注解类型（刚刚加入的@AutoFill注解），以便于下一步的分类型讨论

        //获取到当前被拦截的方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }

        Object entity = args[0];//用Object泛指，传EM就接收em，传菜品类就接收菜品类

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();//系统当前时间
        Long currentId = BaseContext.getCurrentId();//获取ID

        //根据当前不同的操作类型，为对应的属性通过反射来赋值
        if(operationType == OperationType.INSERT){
            //为4个公共字段赋值
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                Method setCreateUser= entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setUpdateUser= entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);

                //通过反射为对象属性赋值
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {//不捕获NoSuchMethodException，直接捕获大的Exception,否则上面反射invoke会报错
                e.printStackTrace();
            }
        }else if(operationType == OperationType.UPDATE){
            //为2个公共字段赋值
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setUpdateUser= entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);

                //通过反射为对象属性赋值
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {//不捕获NoSuchMethodException，直接捕获大的Exception,否则上面反射invoke会报错
                e.printStackTrace();
            }
        }
    }
}
