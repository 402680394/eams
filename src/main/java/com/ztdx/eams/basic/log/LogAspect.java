package com.ztdx.eams.basic.log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LogAspect {

    @Before("execution(* *.*(..)) && @annotation(checkEntity)")
    public void checkEntity(JoinPoint joinPoint, OperationLog checkEntity) {
        String result = getValue(joinPoint, checkEntity.key());
        System.out.println(result);
    }

    private String getValue(JoinPoint joinPoint, String condition) {
        Object[] args = joinPoint.getArgs();
        if (args == null) {
            return null;
        }

        ExpressionParser expressionParser = new SpelExpressionParser();
        EvaluationContext evaluationContext = new StandardEvaluationContext(args);
        return expressionParser.parseExpression(condition).getValue(evaluationContext).toString();
    }
}
