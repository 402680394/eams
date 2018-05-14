package com.ztdx.eams.basic.config;

import com.ztdx.eams.basic.repository.CustomElasticsearchRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(
        basePackages = "com.ztdx.eams.domain.archives.repository.elasticsearch",
        repositoryBaseClass = CustomElasticsearchRepositoryImpl.class
)
public class ElasticsearchConfig {
}
