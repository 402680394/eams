package com.ztdx.eams.domain.archives.model.condition;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;

public class ArrayConditionQueryBuilder extends AbstractConditionEsQueryBuilder<ArrayList> {
    public ArrayConditionQueryBuilder(String name, ArrayList value) {
        super(name, value);
    }

    @Override
    public QueryBuilder contain() {
        return QueryBuilders.termsQuery(name, value);
    }
}
