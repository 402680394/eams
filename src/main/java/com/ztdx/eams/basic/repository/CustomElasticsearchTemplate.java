package com.ztdx.eams.basic.repository;

import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.Client;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.EntityMapper;
import org.springframework.data.elasticsearch.core.ResultsMapper;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;

public class CustomElasticsearchTemplate extends ElasticsearchTemplate implements CustomElasticsearchOperations {
    private Client client;
    //private ElasticsearchConverter elasticsearchConverter;
    //private ResultsMapper resultsMapper;
    public CustomElasticsearchTemplate(Client client) {
        super(client);
    }

    public CustomElasticsearchTemplate(Client client, EntityMapper entityMapper) {
        super(client, entityMapper);
    }

    public CustomElasticsearchTemplate(Client client, ElasticsearchConverter elasticsearchConverter, EntityMapper entityMapper) {
        super(client, elasticsearchConverter, entityMapper);
    }

    public CustomElasticsearchTemplate(Client client, ResultsMapper resultsMapper) {
        super(client, resultsMapper);
    }

    public CustomElasticsearchTemplate(Client client, ElasticsearchConverter elasticsearchConverter) {
        super(client, elasticsearchConverter);
    }

    public CustomElasticsearchTemplate(Client client, ElasticsearchConverter elasticsearchConverter, ResultsMapper resultsMapper) {
        super(client, elasticsearchConverter, resultsMapper);
        this.client = client;
        //this.elasticsearchConverter = elasticsearchConverter;
        //this.resultsMapper = resultsMapper;
    }

    public String delete(String indexName, String type, String id, String routing) {
        DeleteRequestBuilder deleteRequestBuilder = this.client.prepareDelete(indexName, type, id);
        deleteRequestBuilder.setRouting(routing);
        return (deleteRequestBuilder.execute().actionGet()).getId();
    }
}
