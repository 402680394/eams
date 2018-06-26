package com.ztdx.eams.domain.archives.model.condition;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class TextConditionQueryBuilder extends AbstractConditionEsQueryBuilder {
    public TextConditionQueryBuilder(String name, Object value) {
        super(name, value);
    }

    @Override
    public QueryBuilder contain() {
        if (value.toString().contains("*") || value.toString().contains("?")){
            return QueryBuilders.wildcardQuery(name, value.toString());
        }else{
            return QueryBuilders.matchQuery(name, value);
        }
    }
}
