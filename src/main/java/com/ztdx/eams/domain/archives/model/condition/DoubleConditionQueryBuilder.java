package com.ztdx.eams.domain.archives.model.condition;

import org.elasticsearch.index.query.QueryBuilder;

public class DoubleConditionQueryBuilder extends AbstractConditionEsQueryBuilder<Double> {
    public DoubleConditionQueryBuilder(String name, Double value) {
        super(name, value);
    }

    @Override
    public QueryBuilder contain() {
        return null;
    }
}
