package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/*
* 自定义切面，实现公共字段自动填充处理逻辑
* */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /*
    * 切入点
    * */
    @Pointcut("execution(* com.sky.mapper.*.*(..))&&@annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /*
    * 前置通知，在通知中给公共字段赋值
    * */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段自动填充...");

        //获取当前被拦截的数据库操作类型
        MethodSignature signature =(MethodSignature) joinPoint.getSignature();//获取方法签名
        AutoFill autoFill= signature.getMethod().getAnnotation(AutoFill.class);//获取方法上的数据库操作类型
        OperationType operationType = autoFill.value();//获取数据库操作类型
        //获取当前被拦截的方法参数---实体对象
        Object[] args = joinPoint.getArgs();
        //准备赋值的数据

        //根据当前不同的操作类型，为对应的属性通过反射赋值
    }
}
