package com.ztdx.eams.basic.repository;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

public interface CustomElasticsearchOperations extends ElasticsearchOperations {
    String delete(String indexName, String type, String id, String routing);
}
