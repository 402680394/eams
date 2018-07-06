package com.ztdx.eams.basic.task.quartz;

import com.ztdx.eams.basic.BeanManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.NumberUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.sql.Date;
import java.time.Instant;
import java.util.UUID;

import static org.quartz.JobBuilder.newJob;

public class QuartzJob extends QuartzJobBean {

    private Integer numExecutions;

    private Log log = LogFactory.getLog(QuartzJob.class);

    private BeanManager beanManager;

    private Class<?> clazz;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] arguments;

    private Scheduler scheduler;

    @Autowired
    public void setBeanManager(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public void setNumExecutions(Integer numExecutions) {
        this.numExecutions = numExecutions;
    }

    @Autowired
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    private Object getTargetObject(){
        return beanManager.getBean(clazz);
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();

        // Try to get the exact method first.
        try {
            log.debug("开始执行作业..." + jobExecutionContext.getJobDetail().getKey().toString());
            Method methodObject = clazz.getMethod(methodName + "Job", parameterTypes);
            Object targetObject = getTargetObject();
            ReflectionUtils.makeAccessible(methodObject);
            log.debug("类:"+targetObject.getClass().getSimpleName()+" 方法:"+methodName);
            Object result = methodObject.invoke(targetObject, arguments);
            if(result != null) {
                log.debug("结果:" + result.toString());
            }
        }
        catch (Exception ex) {
            // Just rethrow exception if we can't get any match.
            ex.printStackTrace();
            JobExecutionException e2 =
                    new JobExecutionException(ex);
            if (numExecutions <= 3) {
                numExecutions++;
                executeJob(scheduler, clazz, methodName, parameterTypes, arguments, numExecutions);
            }
            log.error("作业执行失败" + jobExecutionContext.getJobDetail().getKey().toString());
            throw e2;
        }
    }

    static void executeJob(Scheduler scheduler
            , Class<?> targetClass
            , String methodName
            , Class<?>[] parameterTypes
            , Object[] arguments
            , int numExecutions
    ){
        String groupName = targetClass.getName();

        JobDetail jobDetail = newJob(QuartzJob.class)
                .withIdentity(groupName + UUID.randomUUID().toString(), groupName)
                .requestRecovery()
                .build();

        jobDetail.getJobDataMap().put("clazz", targetClass);
        jobDetail.getJobDataMap().put("methodName", methodName);
        jobDetail.getJobDataMap().put("parameterTypes", parameterTypes);
        jobDetail.getJobDataMap().put("arguments", arguments);
        jobDetail.getJobDataMap().put("NumExecutions", numExecutions);

        Trigger trigger;
        if (numExecutions > 1) {
            long sec = NumberUtils.convertNumberToTargetClass(10 * Math.pow(2D, numExecutions * 1D), Long.class);
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(groupName + UUID.randomUUID().toString(), groupName)
                    .startAt(Date.from(Instant.now().plusSeconds(sec)))
                    .build();
        }else{
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(groupName + UUID.randomUUID().toString(), groupName)
                    .startNow()
                    .build();
        }

        try {
            scheduler.scheduleJob(jobDetail, trigger);

            if (!scheduler.isStarted()) {
                scheduler.start();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
