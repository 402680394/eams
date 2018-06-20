package com.ztdx.eams.domain.archives.model.condition;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class TextConditionQueryBuilder extends AbstractConditionEsQueryBuilder<String> {
    public TextConditionQueryBuilder(String name, String value) {
        super(name, value);
    }

    @Override
    public QueryBuilder contain() {
        if (value.contains("*") || value.contains("?")){
            return QueryBuilders.wildcardQuery(name, value);
        }else{
            return QueryBuilders.matchQuery(name, value);
        }
    }
}
