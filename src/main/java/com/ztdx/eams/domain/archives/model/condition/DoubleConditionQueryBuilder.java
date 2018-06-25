package com.ztdx.eams.domain.archives.model.condition;

import org.elasticsearch.index.query.QueryBuilder;

public class DoubleConditionQueryBuilder extends AbstractConditionEsQueryBuilder {
    public DoubleConditionQueryBuilder(String name, Object value) {
        super(name, value);
    }

    @Override
    public QueryBuilder contain() {
        return null;
    }
}
