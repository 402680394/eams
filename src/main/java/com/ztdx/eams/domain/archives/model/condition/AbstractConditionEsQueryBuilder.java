package com.ztdx.eams.domain.archives.model.condition;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public abstract class AbstractConditionEsQueryBuilder {
    protected String name;
    protected Object value;
    public AbstractConditionEsQueryBuilder(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public QueryBuilder equal() {
        return QueryBuilders.termQuery(name, value);
    }

    public QueryBuilder notEqual() {
        return QueryBuilders.boolQuery().mustNot(equal());
    }

    public QueryBuilder greaterThan() {
        return QueryBuilders.rangeQuery(name).gt(value);
    }

    public QueryBuilder greaterThanOrEqual() {
        return QueryBuilders.rangeQuery(name).gte(value);
    }

    public QueryBuilder lessThan() {
        return QueryBuilders.rangeQuery(name).lt(value);
    }

    public QueryBuilder lessThanOrEqual() {
        return QueryBuilders.rangeQuery(name).lte(value);
    }

    public abstract QueryBuilder contain();

    public QueryBuilder notContain() {
        QueryBuilder contain = contain();
        if (contain != null) {
            return QueryBuilders.boolQuery().mustNot(contain);
        }else{
            return null;
        }
    }
}
