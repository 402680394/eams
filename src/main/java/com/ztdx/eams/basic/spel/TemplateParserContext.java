package com.ztdx.eams.basic.spel;

import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class TemplateParserContext implements ParserContext {

    public String getExpressionPrefix() {
        return "#{";
    }

    public String getExpressionSuffix() {
        return "}";
    }

    public boolean isTemplate() {
        return true;
    }
}
