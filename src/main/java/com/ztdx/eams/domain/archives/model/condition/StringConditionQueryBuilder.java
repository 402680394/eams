package com.ztdx.eams.domain.archives.model.condition;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class StringConditionQueryBuilder extends AbstractConditionEsQueryBuilder<String> {
    public StringConditionQueryBuilder(String name, String value) {
        super(name, value);
    }

    @Override
    public QueryBuilder contain() {
        return QueryBuilders.wildcardQuery(name, String.format("*%s*", value));
    }
}
