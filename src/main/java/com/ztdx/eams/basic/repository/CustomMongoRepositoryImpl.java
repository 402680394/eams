package com.ztdx.eams.basic.repository;

import com.ztdx.eams.basic.repository.annotation.IndexNamePostfix;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.data.util.StreamUtils;
import org.springframework.data.util.Streamable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CustomMongoRepositoryImpl<T, ID> extends SimpleMongoRepository<T, ID> implements CustomMongoRepository<T, ID> {

    private final MongoOperations mongoOperations;

    private final MongoEntityInformation<T, ID> entityInformation;

    private Class<T> entityClass;

    public CustomMongoRepositoryImpl(MongoEntityInformation<T, ID> metadata, MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
        this.mongoOperations = mongoOperations;
        this.entityInformation = metadata;
    }

    public <S extends T> S insert(S entity) {
        Assert.notNull(entity, "Entity must not be null!");
        this.mongoOperations.insert(entity, this.getCollectionName(entity));
        return entity;
    }

    public <S extends T> List<S> insert(Iterable<S> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");
        List<S> list = (List)Streamable.of(entities).stream().collect(StreamUtils.toUnmodifiableList());
        if (list.isEmpty()) {
            return list;
        } else {
            this.mongoOperations.insert(list, this.getCollectionName(list.get(0)));
            return list;
        }
    }

    public <S extends T> S save(S entity) {
        Assert.notNull(entity, "Entity must not be null!");
        if (this.entityInformation.isNew(entity)) {
            this.mongoOperations.insert(entity, this.getCollectionName(entity));
        } else {
            this.mongoOperations.save(entity, this.getCollectionName(entity));
        }

        return entity;
    }

    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");
        Streamable<S> source = Streamable.of(entities);
        boolean allNew = source.stream().allMatch(this.entityInformation::isNew);

        if (allNew) {
            List<S> result = (List)source.stream().collect(Collectors.toList());
            this.mongoOperations.insertAll(result);
            return result;
        } else {
            return (List)source.stream().map(this::save).collect(Collectors.toList());
        }
    }

    public boolean existsById(ID id, String indexName) {
        Assert.notNull(id, "The given id must not be null!");
        return this.mongoOperations.exists(this.getIdQuery(id), this.entityInformation.getJavaType(), indexName);
    }


    public long count(String indexName) {
        return this.mongoOperations.getCollection(indexName).count();
    }


    public List<T> findAll(String indexName) {
        return this.findAll(new Query(), indexName);
    }


    public List<T> findAll(Sort sort, String indexName) {
        Assert.notNull(sort, "Sort must not be null!");
        return this.findAll((new Query()).with(sort), indexName);
    }

    public <S extends T> List<S> findAll(Example<S> example, String indexName) {
        return this.findAll(example, Sort.unsorted(), indexName);
    }


    public <S extends T> List<S> findAll(Example<S> example, Sort sort, String indexName) {
        Assert.notNull(example, "Sample must not be null!");
        Assert.notNull(sort, "Sort must not be null!");
        Query q = (new Query((new Criteria()).alike(example))).with(sort);
        return this.mongoOperations.find(q, example.getProbeType(), indexName);
    }


    public Page<T> findAll(Pageable pageable, String indexName) {
        Assert.notNull(pageable, "Pageable must not be null!");
        Long count = this.count(indexName);
        List<T> list = this.findAll((new Query()).with(pageable), indexName);
        return PageableExecutionUtils.getPage(list, pageable, () -> count);
    }

    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable, String indexName) {
        return findAll(example, pageable, Sort.unsorted(), indexName);
    }

    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable, Sort sort, String indexName) {
        Assert.notNull(example, "Sample must not be null!");
        Assert.notNull(pageable, "Pageable must not be null!");
        Assert.notNull(sort, "Sort must not be null!");
        Query q = (new Query((new Criteria()).alike(example))).with(pageable).with(sort);
        List<S> list = this.mongoOperations.find(q, example.getProbeType(), indexName);
        return PageableExecutionUtils.getPage(list, pageable,
                () -> this.mongoOperations.count(q, example.getProbeType(), indexName));
    }

    public Optional<T> findById(ID id, String indexName) {
        Assert.notNull(id, "The given id must not be null!");
        return Optional.of(this.mongoOperations.findById(id, this.entityInformation.getJavaType(), indexName));
    }


    public Iterable<T> findAllById(Iterable<ID> ids, String indexName) {
        return this.findAll(new Query((new Criteria(this.entityInformation.getIdAttribute())).in((Collection) Streamable.of(ids).stream().collect(StreamUtils.toUnmodifiableList()))), indexName);
    }

    public void delete(T entity) {
        Assert.notNull(entity, "The given entity must not be null!");
        this.deleteById(this.entityInformation.getRequiredId(entity), getCollectionName(entity));
    }

    public void deleteById(ID id, String indexName) {
        Assert.notNull(id, "The given id must not be null!");
        this.mongoOperations.remove(this.getIdQuery(id), this.entityInformation.getJavaType(), indexName);
    }

    public void deleteAll(String indexName) {
        this.mongoOperations.remove(new Query(), indexName);
    }

    private Query getIdQuery(Object id) {
        return new Query(this.getIdCriteria(id));
    }

    private Criteria getIdCriteria(Object id) {
        return Criteria.where(this.entityInformation.getIdAttribute()).is(id);
    }

    private List<T> findAll(@Nullable Query query, String indexName) {
        return query == null ? Collections.emptyList() : this.mongoOperations.find(query, this.entityInformation.getJavaType(), indexName);
    }

    private String getCollectionName(T entity) {
        Field[] fields = entity.getClass().getDeclaredFields();

        for (Field field: fields) {
            if (field.isAnnotationPresent(IndexNamePostfix.class)){
                field.setAccessible(true);
                try {
                    ID result = (ID) field.get(entity);
                    return String.format("%s_%d", entityInformation.getCollectionName(), result);
                }catch(Exception ignored){

                }
            }
        }
        return entityInformation.getCollectionName();
    }
}
