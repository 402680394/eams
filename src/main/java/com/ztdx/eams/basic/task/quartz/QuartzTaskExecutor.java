package com.ztdx.eams.basic.task.quartz;

import org.quartz.*;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import java.util.UUID;

import static org.quartz.JobBuilder.newJob;

public class QuartzTaskExecutor implements TaskExecutor {

    private Scheduler scheduler;

    public QuartzTaskExecutor(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void execute(Runnable task) {

        /*String groupName = joinPoint.getTarget().getClass().getName();

        JobDetail jobDetail = newJob(QuartzJob.class)
                .withIdentity(groupName + UUID.randomUUID().toString(), groupName)
                .requestRecovery()
                .build();

        jobDetail.getJobDataMap().put("clazz", joinPoint.getTarget().getClass());
        jobDetail.getJobDataMap().put("methodName", methodSignature.getName());
        jobDetail.getJobDataMap().put("parameterTypes", methodSignature.getMethod().getParameterTypes());
        jobDetail.getJobDataMap().put("arguments", joinPoint.getArgs());

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(groupName + UUID.randomUUID().toString(), groupName)
                .startNow()
                .build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);

            if (!scheduler.isStarted()) {
                scheduler.start();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }*/

    }
}
