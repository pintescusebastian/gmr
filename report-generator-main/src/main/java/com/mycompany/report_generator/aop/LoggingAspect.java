package com.mycompany.report_generator.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.mycompany.report_generator.services.ReportGenerationService.generateReport(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();

        // todo
        System.out.println("AOP: Start method: " + joinPoint.getSignature().getName());

        Object result = joinPoint.proceed();

        long duration = System.currentTimeMillis() - startTime;

        // todo
        System.out.println("AOP: End method in " + duration + "ms");

        return result;



    }
}
