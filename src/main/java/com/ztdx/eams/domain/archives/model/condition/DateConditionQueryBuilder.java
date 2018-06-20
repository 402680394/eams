package com.ztdx.eams.domain.archives.model.condition;

import org.elasticsearch.index.query.QueryBuilder;

import java.util.Date;

public class DateConditionQueryBuilder extends AbstractConditionEsQueryBuilder<Date>{
    public DateConditionQueryBuilder(String name, Date value) {
        super(name, value);
    }

    @Override
    public QueryBuilder contain() {
        return null;
    }
}
