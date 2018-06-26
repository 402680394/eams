package com.ztdx.eams.domain.archives.model.condition;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;

public class ArrayConditionQueryBuilder extends AbstractConditionEsQueryBuilder {
    public ArrayConditionQueryBuilder(String name, Object value) {
        super(name, value);
    }

    @Override
    public QueryBuilder contain() {
        return QueryBuilders.termsQuery(name, value);
    }
}
