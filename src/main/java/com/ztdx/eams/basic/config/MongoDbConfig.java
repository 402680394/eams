package com.ztdx.eams.basic.config;

import com.ztdx.eams.basic.repository.CustomMongoRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "com.ztdx.eams.domain.archives.repository.mongo",
        repositoryBaseClass = CustomMongoRepositoryImpl.class
)
public class MongoDbConfig {
}
