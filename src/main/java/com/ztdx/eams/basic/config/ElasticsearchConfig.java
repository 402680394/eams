package com.ztdx.eams.basic.config;

import com.ztdx.eams.basic.repository.CustomElasticsearchRepositoryImpl;
import com.ztdx.eams.basic.repository.CustomElasticsearchResultMapper;
import com.ztdx.eams.basic.repository.CustomElasticsearchTemplate;
import org.elasticsearch.client.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(
        basePackages = "com.ztdx.eams.domain.archives.repository.elasticsearch",
        repositoryBaseClass = CustomElasticsearchRepositoryImpl.class
)
public class ElasticsearchConfig {
    @Bean
    public CustomElasticsearchTemplate elasticsearchTemplate(
            Client client,
            ElasticsearchConverter converter) {
        try {
            return new CustomElasticsearchTemplate(client, converter, new CustomElasticsearchResultMapper(converter.getMappingContext()));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
