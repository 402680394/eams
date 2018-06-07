package com.ztdx.eams.controller.operationLog;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.basic.WorkContext;
import com.ztdx.eams.domain.system.application.OperationLogService;
import com.ztdx.eams.domain.system.model.OperationLog;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.OptionalLong;

@Component
@Aspect
public class LogAspect {

    private final OperationLogService operationLogService;

    @Autowired
    public LogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    /**
     * 环绕拦截处理
     */
    @Around("execution(public * *(..)) && @annotation(logInfo)")
    public Object AroundAspect(ProceedingJoinPoint joinPoint, LogInfo logInfo) {
        UserCredential userCredential = (UserCredential) WorkContext.getSession().getAttribute(UserCredential.KEY);
        LogContext logContext = new LogContext();
        logContext.setArgs(joinPoint.getArgs());
        Throwable exception =null;
        Boolean isSuccess =true;

        try {
            Object result = joinPoint.proceed();
            logContext.setResult(result);
            return result;
        } catch (Throwable e) {
            exception=e;
            isSuccess =false;
            throw new RuntimeException(e);
        } finally {
            String message = null;
            ExpressionParser expressionParser = new SpelExpressionParser();
            EvaluationContext evaluationContext = new StandardEvaluationContext(logContext);
            message = expressionParser.parseExpression(logInfo.message()).getValue(evaluationContext).toString();

            OperationLog operationLog = new OperationLog(message, 1, "test");
            operationLog.setException(exception);
            operationLog.setIsSuccess(isSuccess);
            if(userCredential !=null){
                operationLog.setOperatorId(userCredential.getUserId());
                operationLog.setOperatorName(userCredential.getName());
            }
            operationLogService.add(operationLog);
        }
    }

}
