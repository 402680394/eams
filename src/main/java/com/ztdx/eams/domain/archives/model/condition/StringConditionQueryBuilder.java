package com.ztdx.eams.domain.archives.model.condition;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class StringConditionQueryBuilder extends AbstractConditionEsQueryBuilder {
    public StringConditionQueryBuilder(String name, Object value) {
        super(name, value);
    }

    @Override
    public QueryBuilder contain() {
        return QueryBuilders.wildcardQuery(name, String.format("*%s*", value));
    }
}
