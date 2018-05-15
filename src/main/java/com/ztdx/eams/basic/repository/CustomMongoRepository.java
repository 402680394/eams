package com.ztdx.eams.basic.repository;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface CustomMongoRepository<T, ID> extends MongoRepository<T, ID> {

    boolean existsById(ID id, String indexName);

    long count(String indexName);

    List<T> findAll();

    List<T> findAll(String indexName);

    List<T> findAll(Sort var1);

    List<T> findAll(Sort sort, String indexName);

    <S extends T> List<S> findAll(Example<S> example, String indexName);

    <S extends T> List<S> findAll(Example<S> example, Sort sort, String indexName);

    <S extends T> Page<S> findAll(Example<S> example, Pageable pageable, String indexName);

    <S extends T> Page<S> findAll(Example<S> example, Pageable pageable, Sort sort, String indexName);

    Page<T> findAll(Pageable pageable, String indexName);

    Optional<T> findById(ID id, String indexName);

    Iterable<T> findAllById(Iterable<ID> ids, String indexName);

    void deleteById(ID id, String indexName);

    void deleteAll(String indexName);
}
