package com.ztdx.eams.basic.task.quartz;

import com.ztdx.eams.basic.BeanManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class QuartzJob extends QuartzJobBean {

    private Log log = LogFactory.getLog(QuartzJob.class);

    private BeanManager beanManager;

    private Class<?> clazz;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] arguments;

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

    private Object getTargetObject(){
        return beanManager.getBean(clazz);
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        // Try to get the exact method first.
        try {
            log.debug("开始执行作业...");
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
            log.error("--- Error in job!");
            JobExecutionException e2 =
                    new JobExecutionException(ex);
            // Quartz will automatically unschedule
            // all triggers associated with this job
            // so that it does not run again
            e2.setUnscheduleAllTriggers(true);

            throw e2;
        }
    }
}
