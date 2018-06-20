package com.ztdx.eams.domain.archives.model.condition;

import org.elasticsearch.index.query.QueryBuilder;

public class IntegerConditionQueryBuilder extends AbstractConditionEsQueryBuilder<Integer> {

    public IntegerConditionQueryBuilder(String name, Integer value) {
        super(name, value);
    }

    @Override
    public QueryBuilder contain() {
        return null;
    }
}
