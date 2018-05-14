package com.ztdx.eams.basic.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CustomMongoRepository<T, ID> extends MongoRepository<T, ID> {
    void setCollectionSubNo(int collectionSubNo);
}
