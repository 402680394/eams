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

    public boolean existsById(ID id, String collectionName) {
        Assert.notNull(id, "The given id must not be null!");
        return this.mongoOperations.exists(this.getIdQuery(id), this.entityInformation.getJavaType(), collectionName);
    }


    public long count(String collectionName) {
        return this.mongoOperations.getCollection(collectionName).count();
    }


    public List<T> findAll(String collectionName) {
        return this.findAll(new Query(), collectionName);
    }


    public List<T> findAll(Sort sort, String collectionName) {
        Assert.notNull(sort, "Sort must not be null!");
        return this.findAll((new Query()).with(sort), collectionName);
    }

    public <S extends T> List<S> findAll(Example<S> example, String collectionName) {
        return this.findAll(example, Sort.unsorted(), collectionName);
    }


    public <S extends T> List<S> findAll(Example<S> example, Sort sort, String collectionName) {
        Assert.notNull(example, "Sample must not be null!");
        Assert.notNull(sort, "Sort must not be null!");
        Query q = (new Query((new Criteria()).alike(example))).with(sort);
        return this.mongoOperations.find(q, example.getProbeType(), collectionName);
    }


    public Page<T> findAll(Pageable pageable, String collectionName) {
        Assert.notNull(pageable, "Pageable must not be null!");
        Long count = this.count(collectionName);
        List<T> list = this.findAll((new Query()).with(pageable), collectionName);
        return PageableExecutionUtils.getPage(list, pageable, () -> count);
    }

    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable, String collectionName) {
        return findAll(example, pageable, Sort.unsorted(), collectionName);
    }

    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable, Sort sort, String collectionName) {
        Assert.notNull(example, "Sample must not be null!");
        Assert.notNull(pageable, "Pageable must not be null!");
        Assert.notNull(sort, "Sort must not be null!");
        Query q = (new Query((new Criteria()).alike(example))).with(pageable).with(sort);
        List<S> list = this.mongoOperations.find(q, example.getProbeType(), collectionName);
        return PageableExecutionUtils.getPage(list, pageable,
                () -> this.mongoOperations.count(q, example.getProbeType(), collectionName));
    }

    public Optional<T> findById(ID id, String collectionName) {
        Assert.notNull(id, "The given id must not be null!");
        return Optional.ofNullable(this.mongoOperations.findById(id, this.entityInformation.getJavaType(), collectionName));
    }


    public Iterable<T> findAllById(Iterable<ID> ids, String collectionName) {
        return this.findAll(new Query((new Criteria(this.entityInformation.getIdAttribute())).in(Streamable.of(ids).stream().collect(StreamUtils.toUnmodifiableList()))), collectionName);
    }

    public void delete(T entity) {
        Assert.notNull(entity, "The given entity must not be null!");
        this.deleteById(this.entityInformation.getRequiredId(entity), getCollectionName(entity));
    }

    public void deleteById(ID id, String collectionName) {
        Assert.notNull(id, "The given id must not be null!");
        this.mongoOperations.remove(this.getIdQuery(id), this.entityInformation.getJavaType(), collectionName);
    }

    public List<T> findAll(@Nullable Query query, String collectionName) {
        return query == null ? Collections.emptyList() : this.mongoOperations.find(query, this.entityInformation.getJavaType(), collectionName);
    }

    public void deleteAll(String collectionName) {
        this.mongoOperations.remove(new Query(), collectionName);
    }

    private Query getIdQuery(Object id) {
        return new Query(this.getIdCriteria(id));
    }

    private Criteria getIdCriteria(Object id) {
        return Criteria.where(this.entityInformation.getIdAttribute()).is(id);
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
