package com.ztdx.eams.controller.operationLog;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.basic.WorkContext;
import com.ztdx.eams.domain.system.application.OperationRecordService;
import com.ztdx.eams.domain.system.model.OperationRecord;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * 操作记录 拦截
 */
@Component
@Aspect
public class OperationRecordAspect {

    private final OperationRecordService operationRecordService;

    @Autowired
    public OperationRecordAspect(OperationRecordService operationRecordService) {
        this.operationRecordService = operationRecordService;
    }

    /**
     * 环绕拦截处理
     */
    @Around("execution(public * *(..)) && @annotation(operationInfo)")
    public Object AroundAspect(ProceedingJoinPoint joinPoint, OperationInfo operationInfo) {
        UserCredential userCredential = (UserCredential) WorkContext.getSession().getAttribute(UserCredential.KEY);
        OperationContext operationContext = new OperationContext();
        operationContext.setArgs(joinPoint.getArgs());
        Throwable exception =null;
        Boolean isSuccess =true;

        try {
            Object result = joinPoint.proceed();
            operationContext.setResult(result);
            return result;
        } catch (Throwable e) {
            exception=e;
            isSuccess =false;
            throw new RuntimeException(e);
        } finally {
            ExpressionParser expressionParser = new SpelExpressionParser();
            EvaluationContext evaluationContext = new StandardEvaluationContext(operationContext);
            String message = expressionParser.parseExpression(operationInfo.message()).getValue(evaluationContext).toString();
            OperationRecord operationRecord = new OperationRecord(message, 1, "test");
            operationRecord.setException(exception);
            operationRecord.setIsSuccess(isSuccess);
            if(userCredential !=null){
                operationRecord.setOperatorId(userCredential.getUserId());
                operationRecord.setOperatorName(userCredential.getName());
            }
            operationRecordService.add(operationRecord);
        }
    }

}
