package com.ztdx.eams.domain.archives.model.condition;

import org.elasticsearch.index.query.QueryBuilder;

import java.util.Date;

public class DateConditionQueryBuilder extends AbstractConditionEsQueryBuilder{
    public DateConditionQueryBuilder(String name, Object value) {
        super(name, value);
    }

    @Override
    public QueryBuilder contain() {
        return null;
    }
}
