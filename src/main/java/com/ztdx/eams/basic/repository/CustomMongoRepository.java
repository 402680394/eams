package com.ztdx.eams.basic.repository;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface CustomMongoRepository<T, ID> extends MongoRepository<T, ID> {

    boolean existsById(ID id, String collectionName);

    long count(String collectionName);

    List<T> findAll();

    List<T> findAll(String collectionName);

    List<T> findAll(Sort var1);

    List<T> findAll(Sort sort, String collectionName);

    <S extends T> List<S> findAll(Example<S> example, String collectionName);

    <S extends T> List<S> findAll(Example<S> example, Sort sort, String collectionName);

    <S extends T> Page<S> findAll(Example<S> example, Pageable pageable, String collectionName);

    <S extends T> Page<S> findAll(Example<S> example, Pageable pageable, Sort sort, String collectionName);

    Page<T> findAll(Pageable pageable, String collectionName);

    Optional<T> findById(ID id, String collectionName);

    Iterable<T> findAllById(Iterable<ID> ids, String collectionName);

    void deleteById(ID id, String collectionName);

    void deleteAll(String collectionName);

    List<T> findAll(@Nullable Query query, String collectionName);
}
