package com.ztdx.eams.basic.repository;

import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.data.util.StreamUtils;
import org.springframework.data.util.Streamable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoRepositoryBean
public class CustomMongoRepositoryImpl<T, ID> implements CustomMongoRepository<T, ID> {

    private final MongoOperations mongoOperations;
    private final MongoEntityInformation<T, ID> entityInformation;
    private Integer collectionSubNo;

    public CustomMongoRepositoryImpl(MongoEntityInformation<T, ID> metadata, MongoOperations mongoOperations) {
        Assert.notNull(metadata, "MongoEntityInformation must not be null!");
        Assert.notNull(mongoOperations, "MongoOperations must not be null!");
        this.entityInformation = metadata;
        this.mongoOperations = mongoOperations;
        this.collectionSubNo = null;
    }

    public <S extends T> S save(S entity) {
        Assert.notNull(entity, "Entity must not be null!");
        if (this.entityInformation.isNew(entity)) {
            this.mongoOperations.insert(entity, this.getCollectionName());
        } else {
            this.mongoOperations.save(entity, this.getCollectionName());
        }

        return entity;
    }

    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");
        Streamable<S> source = Streamable.of(entities);
        boolean allNew = source.stream().allMatch((it) -> {
            return this.entityInformation.isNew(it);
        });
        if (allNew) {
            List<S> result = (List)source.stream().collect(Collectors.toList());
            this.mongoOperations.insertAll(result);
            return result;
        } else {
            return (List)source.stream().map(this::save).collect(Collectors.toList());
        }
    }

    public Optional<T> findById(ID id) {
        Assert.notNull(id, "The given id must not be null!");
        return Optional.ofNullable(this.mongoOperations.findById(id, this.entityInformation.getJavaType(), this.getCollectionName()));
    }

    public boolean existsById(ID id) {
        Assert.notNull(id, "The given id must not be null!");
        return this.mongoOperations.exists(this.getIdQuery(id), this.entityInformation.getJavaType(), this.getCollectionName());
    }

    public long count() {
        return this.mongoOperations.getCollection(this.getCollectionName()).count();
    }

    public void deleteById(ID id) {
        Assert.notNull(id, "The given id must not be null!");
        this.mongoOperations.remove(this.getIdQuery(id), this.entityInformation.getJavaType(), this.getCollectionName());
    }

    public void delete(T entity) {
        Assert.notNull(entity, "The given entity must not be null!");
        this.deleteById(this.entityInformation.getRequiredId(entity));
    }

    public void deleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");
        entities.forEach(this::delete);
    }

    public void deleteAll() {
        this.mongoOperations.remove(new Query(), this.getCollectionName());
    }

    public List<T> findAll() {
        return this.findAll(new Query());
    }

    public Iterable<T> findAllById(Iterable<ID> ids) {
        return this.findAll(new Query((new Criteria(this.entityInformation.getIdAttribute())).in((Collection)Streamable.of(ids).stream().collect(StreamUtils.toUnmodifiableList()))));
    }

    public Page<T> findAll(Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null!");
        Long count = this.count();
        List<T> list = this.findAll((new Query()).with(pageable));
        return new PageImpl(list, pageable, count);
    }

    public List<T> findAll(Sort sort) {
        Assert.notNull(sort, "Sort must not be null!");
        return this.findAll((new Query()).with(sort));
    }

    public <S extends T> S insert(S entity) {
        Assert.notNull(entity, "Entity must not be null!");
        this.mongoOperations.insert(entity, this.getCollectionName());
        return entity;
    }

    public <S extends T> List<S> insert(Iterable<S> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");
        List<S> list = (List)Streamable.of(entities).stream().collect(StreamUtils.toUnmodifiableList());
        if (list.isEmpty()) {
            return list;
        } else {
            this.mongoOperations.insertAll(list);
            return list;
        }
    }

    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        Assert.notNull(example, "Sample must not be null!");
        Assert.notNull(pageable, "Pageable must not be null!");
        Query q = (new Query((new Criteria()).alike(example))).with(pageable);
        List<S> list = this.mongoOperations.find(q, example.getProbeType(), this.getCollectionName());
        return PageableExecutionUtils.getPage(list, pageable, () -> {
            return this.mongoOperations.count(q, example.getProbeType(), this.getCollectionName());
        });
    }

    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        Assert.notNull(example, "Sample must not be null!");
        Assert.notNull(sort, "Sort must not be null!");
        Query q = (new Query((new Criteria()).alike(example))).with(sort);
        return this.mongoOperations.find(q, example.getProbeType(), this.getCollectionName());
    }

    public <S extends T> List<S> findAll(Example<S> example) {
        return this.findAll(example, Sort.unsorted());
    }

    public <S extends T> Optional<S> findOne(Example<S> example) {
        Assert.notNull(example, "Sample must not be null!");
        Query q = new Query((new Criteria()).alike(example));
        return Optional.ofNullable(this.mongoOperations.findOne(q, example.getProbeType(), this.getCollectionName()));
    }

    public <S extends T> long count(Example<S> example) {
        Assert.notNull(example, "Sample must not be null!");
        Query q = new Query((new Criteria()).alike(example));
        return this.mongoOperations.count(q, example.getProbeType(), this.getCollectionName());
    }

    public <S extends T> boolean exists(Example<S> example) {
        Assert.notNull(example, "Sample must not be null!");
        Query q = new Query((new Criteria()).alike(example));
        return this.mongoOperations.exists(q, example.getProbeType(), this.getCollectionName());
    }

    public void setCollectionSubNo(int collectionSubNo) {
        this.collectionSubNo = collectionSubNo;
    }

    private String getCollectionName() {
        String prefix = entityInformation.getCollectionName();
        if (collectionSubNo != null){
            return String.format("%s_%d", prefix, collectionSubNo);
        }else{
            return prefix;
        }
    }

    private Query getIdQuery(Object id) {
        return new Query(this.getIdCriteria(id));
    }

    private Criteria getIdCriteria(Object id) {
        return Criteria.where(this.entityInformation.getIdAttribute()).is(id);
    }

    private List<T> findAll(@Nullable Query query) {
        return query == null ? Collections.emptyList() : this.mongoOperations.find(query, this.entityInformation.getJavaType(), this.getCollectionName());
    }
}
