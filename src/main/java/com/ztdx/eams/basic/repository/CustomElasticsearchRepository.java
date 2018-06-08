package com.ztdx.eams.basic.repository;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

@NoRepositoryBean
public interface CustomElasticsearchRepository<T, ID extends Serializable> extends ElasticsearchRepository<T, ID> {

    Iterable<T> search(QueryBuilder var1, String[] indices);

    Page<T> search(QueryBuilder var1, Pageable var2, String[] indices);

    Page<T> search(SearchQuery var1, String[] indices);

    void refresh(String indexName);

    boolean existsById(ID id, String indexName);

    long count(String indexName);

    Optional<T> findById(ID id, String indexName);

    Iterable<T> findAll(String indexName);

    Iterable<T> findAllById(Iterable<ID> ids, String indexName);

    Iterable<T> findAll(Sort sort, String indexName);

    Page<T> findAll(Pageable pageable, String indexName);

    void deleteById(ID id, String indexName);

    void deleteAll(String indexName);

    boolean createIndex(String indexName) throws IOException;

    boolean putMapping(String indexName, Object mapping);
}
