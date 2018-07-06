package com.ztdx.eams.basic.task.quartz;

import com.ztdx.eams.basic.task.Job;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.quartz.JobBuilder.newJob;

@Component
@Aspect
public class QuartzJobAspect {
    private Scheduler scheduler;

    @Autowired
    public QuartzJobAspect(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Around("execution(public void *(..)) && @annotation(job)")
    public Object AroundAspect(ProceedingJoinPoint joinPoint, Job job) {
        try {
            Signature signature = joinPoint.getSignature();
            MethodSignature methodSignature;
            if (!(signature instanceof MethodSignature)) {
                throw new IllegalArgumentException("该注解只能用于方法");
            }
            methodSignature = (MethodSignature) signature;

            QuartzJob.executeJob(scheduler
                    , joinPoint.getTarget().getClass()
                    , methodSignature.getName()
                    , methodSignature.getMethod().getParameterTypes()
                    , joinPoint.getArgs()
                    , 1);

            return null;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
