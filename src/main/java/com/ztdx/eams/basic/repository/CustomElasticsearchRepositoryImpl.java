package com.ztdx.eams.basic.repository;

import com.ztdx.eams.basic.repository.annotation.IndexNamePostfix;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.DefaultResultMapper;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ResultsMapper;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.data.elasticsearch.repository.support.AbstractElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public class CustomElasticsearchRepositoryImpl<T, ID extends Serializable> extends AbstractElasticsearchRepository<T, ID> {
    private ResultsMapper resultsMapper;

    public CustomElasticsearchRepositoryImpl(ElasticsearchOperations elasticsearchOperations) {
        super(elasticsearchOperations);
        ElasticsearchConverter elasticsearchConverter = (new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext()));
        this.resultsMapper = (new DefaultResultMapper(elasticsearchConverter.getMappingContext()));
    }

    public CustomElasticsearchRepositoryImpl(ElasticsearchEntityInformation<T, ID> metadata, ElasticsearchOperations elasticsearchOperations) {
        super(metadata, elasticsearchOperations);
        ElasticsearchConverter elasticsearchConverter = (new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext()));
        this.resultsMapper = (new DefaultResultMapper(elasticsearchConverter.getMappingContext()));
    }

    public CustomElasticsearchRepositoryImpl() {
    }

    public Class<T> getEntityClass() {
        if (!this.isEntityClassSet()) {
            try {
                this.entityClass = this.resolveReturnedClassFromGenericType();
            } catch (Exception var2) {
                throw new InvalidDataAccessApiUsageException("Unable to resolve EntityClass. Please use according setter!", var2);
            }
        }

        return this.entityClass;
    }

    private boolean isEntityClassSet() {
        return this.entityClass != null;
    }

    private Class<T> resolveReturnedClassFromGenericType() {
        ParameterizedType parameterizedType = this.resolveReturnedClassFromGenericType(this.getClass());
        return (Class)parameterizedType.getActualTypeArguments()[0];
    }

    private ParameterizedType resolveReturnedClassFromGenericType(Class<?> clazz) {
        Object genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)genericSuperclass;
            //Type rawtype = parameterizedType.getRawType();
            //if (SimpleElasticsearchRepository.class.equals(rawtype)) {
                return parameterizedType;
            //}
        }

        return this.resolveReturnedClassFromGenericType(clazz.getSuperclass());
    }

    
    public <S extends T> S index(S entity) {
        return this.save(entity);
    }

    
    public Iterable<T> search(QueryBuilder queryBuilder) {
        return search(queryBuilder, new String[] {this.getIndexNamePrefix()});
    }

    
    public Page<T> search(QueryBuilder queryBuilder, Pageable pageable) {
        return search(queryBuilder, pageable, new String[] {this.getIndexNamePrefix()});
    }

    
    public Page<T> search(SearchQuery searchQuery) {
        return search(searchQuery, new String[] {this.getIndexNamePrefix()});
    }


    public Iterable<T> search(QueryBuilder query, String[] indices) {
        SearchQuery searchQuery = (new NativeSearchQueryBuilder()).withQuery(query).build();
        searchQuery.addIndices(indices);
        int count = (int)this.elasticsearchOperations.count(searchQuery, this.getEntityClass());
        if (count == 0) {
            return new PageImpl(Collections.emptyList());
        } else {
            searchQuery.setPageable(PageRequest.of(0, count));
            return this.elasticsearchOperations.queryForPage(searchQuery, this.getEntityClass());
        }
    }

    
    public Page<T> search(QueryBuilder query, Pageable pageable, String[] indices) {
        SearchQuery searchQuery = (new NativeSearchQueryBuilder()).withQuery(query).withPageable(pageable).build();
        searchQuery.addIndices(indices);
        return this.elasticsearchOperations.queryForPage(searchQuery, this.getEntityClass());
    }

    
    public Page<T> search(SearchQuery query, String[] indices) {
        query.addIndices(indices);
        return this.elasticsearchOperations.queryForPage(query, this.getEntityClass());
    }

    
    public Page<T> searchSimilar(T entity, String[] fields, Pageable pageable) {
        Assert.notNull(entity, "Cannot search similar records for 'null'.");
        Assert.notNull(pageable, "'pageable' cannot be 'null'");
        MoreLikeThisQuery query = new MoreLikeThisQuery();
        query.setId(this.stringIdRepresentation(this.getId(entity)));
        query.setPageable(pageable);
        if (fields != null) {
            query.addFields(fields);
        }
        query.setIndexName(this.getIndexName(entity));
        query.setType(this.getIndexType());

        return this.elasticsearchOperations.moreLikeThis(query, this.getEntityClass());
    }

    
    public void refresh() {
        refresh(getIndexNamePrefix());
    }

    public void refresh(String indexName) {
        this.elasticsearchOperations.refresh(indexName);
    }

    
    public <S extends T> S save(S entity) {
        Assert.notNull(entity, "Cannot save 'null' entity.");
        this.elasticsearchOperations.index(this.createIndexQuery(entity));
        this.elasticsearchOperations.refresh(this.getIndexName(entity));
        return entity;
    }

    
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        Assert.notNull(entities, "Cannot insert 'null' as a List.");
        List<IndexQuery> queries = new ArrayList();

        for (S s : entities) {
            queries.add(this.createIndexQuery(s));
        }

        this.elasticsearchOperations.bulkIndex(queries);
        if (entities.iterator().hasNext()){
            this.elasticsearchOperations.refresh(this.getIndexName(entities.iterator().next()));
        }
        return entities;
    }

    
    public Optional<T> findById(ID id) {
        return findById(id, this.getIndexNamePrefix());
    }

    
    public boolean existsById(ID id) {
        return existsById(id, this.getIndexNamePrefix());
    }

    
    public Iterable<T> findAll() {
        return findAll(this.getIndexNamePrefix());
    }

    
    public Iterable<T> findAllById(Iterable<ID> ids) {
        return findAllById(ids, this.getIndexNamePrefix());
    }

    
    public long count() {
        return count(this.getIndexNamePrefix());
    }

    
    public void deleteById(ID id) {
        deleteById(id, this.getIndexNamePrefix());
    }


    public boolean existsById(ID id, String indexName) {
        return this.findById(id, indexName) != null;
    }

    
    public long count(String indexName) {
        SearchQuery query = (new NativeSearchQueryBuilder()).withQuery(QueryBuilders.matchAllQuery()).build();
        query.addIndices(indexName);
        query.addTypes(this.getIndexType());
        return this.elasticsearchOperations.count(query, this.getEntityClass());
    }

    
    public Optional<T> findById(ID id, String indexName) {
        GetQuery query = new GetQuery();
        query.setId(this.stringIdRepresentation(id));
        Client client = elasticsearchOperations.getClient();
        GetResponse response = client.prepareGet(indexName, this.getIndexType(), query.getId()).execute().actionGet();
        T entity = resultsMapper.mapResult(response, getEntityClass());
        return Optional.ofNullable(entity);
    }

    
    public Iterable<T> findAll(String indexName) {
        int itemCount = (int)this.count(indexName);
        return (Iterable)(itemCount == 0 ? new PageImpl(Collections.emptyList()) : this.findAll((Pageable)PageRequest.of(0, Math.max(1, itemCount)),indexName));
    }

    
    public Iterable<T> findAllById(Iterable<ID> ids, String indexName) {
        Assert.notNull(ids, "ids can't be null.");
        SearchQuery query = (new NativeSearchQueryBuilder()).withIds(this.stringIdsRepresentation(ids)).build();
        query.addIndices(indexName);
        query.addTypes(getIndexType());
        return this.elasticsearchOperations.multiGet(query, this.getEntityClass());
    }

    
    public Iterable<T> findAll(Sort sort, String indexName) {
        int itemCount = (int)this.count(indexName);
        if (itemCount == 0) {
            return new PageImpl(Collections.emptyList());
        } else {
            SearchQuery query = (new NativeSearchQueryBuilder()).withQuery(QueryBuilders.matchAllQuery()).withPageable(PageRequest.of(0, itemCount, sort)).build();
            query.addIndices(indexName);
            query.addTypes(this.getIndexType());
            return this.elasticsearchOperations.queryForPage(query, this.getEntityClass());
        }
    }

    
    public Page<T> findAll(Pageable pageable, String indexName) {
        SearchQuery query = (new NativeSearchQueryBuilder()).withQuery(QueryBuilders.matchAllQuery()).withPageable(pageable).build();
        query.addIndices(indexName);
        query.addTypes(this.getIndexType());
        return this.elasticsearchOperations.queryForPage(query, this.getEntityClass());
    }

    
    public void deleteById(ID id, String indexName) {
        Assert.notNull(id, "Cannot delete entity with id 'null'.");
        this.elasticsearchOperations.delete(indexName, this.getIndexType(), this.stringIdRepresentation(id));
        this.elasticsearchOperations.refresh(indexName);
    }

    
    public void delete(T entity) {
        Assert.notNull(entity, "Cannot delete 'null' entity.");
        this.deleteById(this.getId(entity),this.getIndexName(entity));
        this.elasticsearchOperations.refresh(this.getIndexName(entity));
    }

    
    public void deleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "Cannot delete 'null' list.");

        for (T entity : entities) {
            this.delete(entity);
        }
    }

    
    public void deleteAll() {
        deleteAll(this.getIndexNamePrefix());
    }


    public void deleteAll(String indexName) {
        DeleteQuery deleteQuery = new DeleteQuery();
        deleteQuery.setQuery(QueryBuilders.matchAllQuery());
        deleteQuery.setIndex(indexName);
        deleteQuery.setType(this.getIndexType());
        this.elasticsearchOperations.delete(deleteQuery);
        this.elasticsearchOperations.refresh(indexName);
    }

    protected String stringIdRepresentation(ID id) {
        return String.valueOf(id);
    }

    private List<String> stringIdsRepresentation(Iterable<ID> ids) {
        Assert.notNull(ids, "ids can't be null.");
        List<String> stringIds = new ArrayList<>();

        for (ID id : ids) {
            stringIds.add(this.stringIdRepresentation(id));
        }

        return stringIds;
    }

    private ID getId(T entity) {
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field: fields) {
            if (field.isAnnotationPresent(Id.class)){
                field.setAccessible(true);
                try {
                    return (ID) field.get(entity);
                }catch(Exception ignored){

                }
            }
        }
        return null;
    }

    private String getIndexName(T entity) {
        Field[] fields = entity.getClass().getDeclaredFields();

        for (Field field: fields) {
            if (field.isAnnotationPresent(IndexNamePostfix.class)){
                field.setAccessible(true);
                try {
                    ID result = (ID) field.get(entity);
                    return String.format("%s_%d",getIndexNamePrefix(),result);
                }catch(Exception ignored){

                }
            }
        }
        return getIndexNamePrefix();
    }

    private String getIndexType(){
        Class<T> clazz = getEntityClass();
        if (clazz.isAnnotationPresent(Document.class)) {
            Document document = clazz.getAnnotation(Document.class);
            return document.type().isEmpty()?getEntityClass().getSimpleName():document.type();
        }
        return getEntityClass().getSimpleName();
    }

    private String getIndexNamePrefix(){
        Class<T> clazz = getEntityClass();
        if (clazz.isAnnotationPresent(Document.class)) {
            Document document = clazz.getAnnotation(Document.class);
            return document.indexName().isEmpty()?getEntityClass().getSimpleName():document.indexName();
        }
        return getEntityClass().getSimpleName();
    }

    private IndexQuery createIndexQuery(T entity) {
        IndexQuery query = new IndexQuery();
        query.setObject(entity);
        query.setId(this.stringIdRepresentation(this.getId(entity)));
        query.setIndexName(this.getIndexName(entity));
        query.setType(this.getIndexType());
        //TODO 没有启用版本控制和父节点
        //query.setVersion(this.extractVersionFromBean(entity));
        //query.setParentId(this.extractParentIdFromBean(entity));
        return query;
    }

    
    public Iterable<T> findAll(Sort sort) {
        return findAll(sort, this.getIndexNamePrefix());
    }

    
    public Page<T> findAll(Pageable pageable) {
        return findAll(pageable, this.getIndexNamePrefix());
    }
}
