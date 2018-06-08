package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.model.*;
import com.ztdx.eams.domain.archives.model.entryItem.EntryItemConverter;
import com.ztdx.eams.domain.archives.repository.ArchivesGroupRepository;
import com.ztdx.eams.domain.archives.repository.ArchivesRepository;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import com.ztdx.eams.domain.archives.repository.DescriptionItemRepository;
import com.ztdx.eams.domain.archives.repository.elasticsearch.EntryElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.elasticsearch.OriginalTextElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.mongo.EntryMongoRepository;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.data.mongodb.core.query.Update;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.BasicQuery.query;


@Service
public class EntryService {

    private static final String FULL_CONTENT = "full_content";
    private static final String INDEX_NAME_PREFIX = "archive_record_";

    private EntryElasticsearchRepository entryElasticsearchRepository;

    private EntryMongoRepository entryMongoRepository;

    private DescriptionItemRepository descriptionItemRepository;

    private CatalogueRepository catalogueRepository;

    private ArchivesRepository archivesRepository;

    private ArchivesGroupRepository archivesGroupRepository;

    private ElasticsearchOperations elasticsearchOperations;

    //这个暂时留着做测试用，测试条目和原文的级联关系
    private OriginalTextElasticsearchRepository originalTextElasticsearchRepository;

    private MongoOperations mongoOperations;

    public EntryService(EntryElasticsearchRepository entryElasticsearchRepository, EntryMongoRepository entryMongoRepository, DescriptionItemRepository descriptionItemRepository, CatalogueRepository catalogueRepository, ArchivesRepository archivesRepository, ArchivesGroupRepository archivesGroupRepository, ElasticsearchOperations elasticsearchOperations, OriginalTextElasticsearchRepository originalTextElasticsearchRepository, MongoOperations mongoOperations) {
        this.entryElasticsearchRepository = entryElasticsearchRepository;
        this.entryMongoRepository = entryMongoRepository;
        this.descriptionItemRepository = descriptionItemRepository;
        this.catalogueRepository = catalogueRepository;
        this.archivesRepository = archivesRepository;
        this.archivesGroupRepository = archivesGroupRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.originalTextElasticsearchRepository = originalTextElasticsearchRepository;
        this.mongoOperations = mongoOperations;
    }

    public Entry save(Entry entry) {
        Catalogue catalog = catalogueRepository.findById(entry.getCatalogueId()).orElse(null);
        if (catalog == null) {
            throw new InvalidArgumentException("目录id不存在");
        }

        Archives archives = archivesRepository.findById(catalog.getArchivesId()).orElse(null);
        if (archives == null) {
            throw new InvalidArgumentException("档案库不存在");
        }

        ArchivesGroup archivesGroup = archivesGroupRepository.findById(archives.getArchivesGroupId()).orElse(null);
        if (archivesGroup == null) {
            throw new InvalidArgumentException("档案库分组不存在");
        }

        entry.setArchiveId(catalog.getArchivesId());
        entry.setCatalogueType(catalog.getCatalogueType());
        entry.setArchiveContentType(archives.getContentTypeId());
        entry.setArchiveType(archives.getType());
        entry.setFondsId(archivesGroup.getFondsId());
        entry.setId(UUID.randomUUID().toString());
        entry.setGmtCreate(new Date());
        entry.setGmtModified(new Date());
        this.convertEntryItems(entry, EntryItemConverter::from);
        entry = entryMongoRepository.save(entry);

        /*try {
            originalTextElasticsearchRepository.createIndex(this.getIndexName(entry.getCatalogueId()));
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        initIndex(entry.getCatalogueId());
        entryElasticsearchRepository.save(entry);

        /*OriginalText originalText = new OriginalText();
        originalText.setCatalogueId(entry.getCatalogueId());
        originalText.setEntryId(entry.getId());
        originalText.setTitle("测试");
        originalText.setType("测试");
        originalText.setCreateTime(Date.from(Instant.now()));
        originalText.setGmtCreate(Date.from(Instant.now()));
        originalText.setGmtModified(Date.from(Instant.now()));
        originalTextElasticsearchRepository.save(originalText);*/

        index(entry);
        return entry;
    }

    public Entry update(Entry entry) {
        if (entry.getCatalogueId() == 0){
            throw new InvalidArgumentException("目录id不存在");
        }

        if (StringUtils.isEmpty(entry.getId())){
            throw new InvalidArgumentException("id字段不存在");
        }

        Optional<Entry> find = entryMongoRepository.findById(entry.getId(), getIndexName(entry.getCatalogueId()));
        if (!find.isPresent()) {
            return save(entry);
        }
        Entry update = find.get();
        update.setItems(entry.getItems());
        update.setGmtModified(new Date());
        update.setVersion(entry.getVersion());
        this.convertEntryItems(entry, EntryItemConverter::from);
        update = entryMongoRepository.save(update);

        initIndex(entry.getCatalogueId());
        entryElasticsearchRepository.save(update);

        index(update);
        return update;
    }

    public void index(Entry entry){
        initIndex(entry.getCatalogueId());
        entryElasticsearchRepository.save(entry);
        mongoOperations.updateFirst(
                query(where("_id").is(entry.getId()))
                , Update.update("indexVersion", entry.getVersion())
                , this.getIndexName(entry.getCatalogueId()));
    }

    private void initIndex(int catalogueId) {
        try {
            entryElasticsearchRepository.createIndex(this.getIndexName(catalogueId));
        } catch (IOException e) {
            throw new BusinessException("索引初始化失败", e);
        }
    }

    public void putMapping(int catalogueId) throws IOException {
        initIndex(catalogueId);

        List<DescriptionItem> list = descriptionItemRepository.findByCatalogueId(catalogueId);
        XContentBuilder contentBuilder;
        contentBuilder = XContentFactory.jsonBuilder().startObject()
                .startObject(getIndexType(Entry.class))
                .startObject("properties");

        for (DescriptionItem descriptionItem : list) {
            addSingleFieldMapping(contentBuilder, descriptionItem);
        }

        contentBuilder
                .startObject("full_content")
                .field("type", FieldType.text.name().toLowerCase())
                .endObject();

        contentBuilder.endObject().endObject().endObject();

        entryElasticsearchRepository.putMapping(this.getIndexName(catalogueId), contentBuilder);
    }

    private FieldType convertDescriptionItemDateType(DescriptionItemDataType dataType){
        switch (dataType){
            case Date:
                return FieldType.Date;
            case Double:
                return FieldType.Double;
            case Integer:
                return FieldType.Integer;
            case Array:
                return FieldType.text;
            case String:
                return FieldType.keyword;
            case Text:
                return FieldType.text;
        }
        return FieldType.Auto;
    }

    private void addSingleFieldMapping(XContentBuilder xContentBuilder, DescriptionItem descriptionItem) throws IOException {
        xContentBuilder.startObject(descriptionItem.getMetadataName());

        FieldType fieldType = convertDescriptionItemDateType(descriptionItem.getDataType());
        if (FieldType.Auto != fieldType) {
            xContentBuilder.field("type", fieldType.name().toLowerCase());
        }

        if (descriptionItem.getIsIndex() == 1){
            xContentBuilder.field("copy_to", FULL_CONTENT);
        }

        xContentBuilder.field("index", true);

        //xContentBuilder.field("search_analyzer", "");
        //xContentBuilder.field("analyzer", "");

        xContentBuilder.endObject();
    }

    private <T> String getIndexType(Class<T> clazz){
        if (clazz.isAnnotationPresent(Document.class)) {
            Document document = clazz.getAnnotation(Document.class);
            return document.type().isEmpty()?clazz.getSimpleName():document.type();
        }
        return clazz.getSimpleName();
    }

    public Page<Entry> search(int catalogueId, String queryString, Map<String, Object> itemQuery, Pageable pageable) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (queryString != null && queryString.length() > 0) {
            query.must(queryStringQuery(queryString));
        }
        query.must().addAll(parseQuery(catalogueId, itemQuery));
        query.filter(termQuery("gmtDeleted", 0));

        initIndex(catalogueId);

        Page<Entry> result = entryElasticsearchRepository.search(
                query, pageable, new String[]{getIndexName(catalogueId)}
        );
        result.stream().forEach(a -> {
            convertEntryItems(a, EntryItemConverter::format);
            Map<String, Object> items = a.getItems();
            items.put("id", a.getId());
        });
        return result;
    }

    private List<QueryBuilder> parseQuery(int catalogueId, Map<String, Object> itemQuery) {
        List<QueryBuilder> query = new ArrayList<>();

        Map<String, DescriptionItem> descriptionItemMap = this.getDescriptionItems(catalogueId);
        itemQuery.forEach((key, value) -> {
            DescriptionItem item = descriptionItemMap.get(key);
            if (item != null) {
                DescriptionItemDataType dataType = item.getDataType();
                switch (dataType) {
                    case String: {
                        query.add(QueryBuilders.wildcardQuery(key, value + "*"));
                        break;
                    }
                    case Integer:
                    case Double:
                    case Date: {
                        if (value instanceof ArrayList) {
                            ArrayList list = (ArrayList)value;
                            if (list.size() < 2){
                                throw new InvalidArgumentException(key+"字段的查询格式错误，[开始区间,结束区间]");
                            }
                            Object start = EntryItemConverter.from(list.get(0), item);
                            Object end = EntryItemConverter.from(list.get(1), item);
                            if (start != null){
                                query.add(QueryBuilders.rangeQuery(key).from(start));
                            }
                            if (end != null){
                                query.add(QueryBuilders.rangeQuery(key).to(end));
                            }
                        } else {
                            Object convert = EntryItemConverter.from(value, item);
                            assert convert != null;
                            query.add(QueryBuilders.termQuery(key, convert));
                        }
                    }
                    case Array: {
                        Object convert = EntryItemConverter.from(value, item);
                        assert convert != null;
                        query.add(QueryBuilders.termsQuery(key, (ArrayList) convert));
                    }
                    default:

                }
            }
        });
        return query;
    }

    private void convertEntryItems(Entry entry, BiFunction<Object, DescriptionItem, Object> operator) {
        Map<String, DescriptionItem> descriptionItemMap = this.getDescriptionItems(entry.getCatalogueId());
        Map<String, Object> convert = new HashMap<>();
        entry.getItems().forEach((key, vaule) -> {
            DescriptionItem item = descriptionItemMap.get(key);
            if (item != null) {
                Object val = operator.apply(vaule, item);
                if (val != null) {
                    convert.put(key, val);
                }
            }
        });
        entry.setItems(convert);
    }

    private Map<String, DescriptionItem> getDescriptionItems(int catalogueId) {
        return descriptionItemRepository.findByCatalogueId(catalogueId).stream().collect(Collectors.toMap(DescriptionItem::getMetadataName, (d) -> d, (d1, d2) -> d2));
    }

    public Object test(String uuid) {
        try {
            putMapping(Integer.parseInt(uuid));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<Integer, Long> aggsCatalogueCount(List<Integer> catalogueIds, List<Integer> archiveContentType, String keyWord) {
        Collection<String> indices = new ArrayList<>();
        if (catalogueIds == null || catalogueIds.size() == 0){
            indices.add(INDEX_NAME_PREFIX +"*");
        }else{
            catalogueIds.forEach(a -> indices.add(getIndexName(a)));
        }

        SearchRequestBuilder srBuilder = elasticsearchOperations.getClient().prepareSearch(indices.toArray(new String[0]));
        srBuilder.setTypes("record");

        srBuilder.addAggregation(AggregationBuilders.terms("catalogueId").field("catalogueId"));
        BoolQueryBuilder query;
        if (archiveContentType != null && archiveContentType.size() > 0) {
            query = QueryBuilders.boolQuery().must(
                    QueryBuilders.termsQuery("archiveContentType", archiveContentType));
        }else{
            query = QueryBuilders.boolQuery();
        }

        if (keyWord != null && !"".equals(keyWord)){
            query.must(QueryBuilders.queryStringQuery(keyWord));
        }

        srBuilder.setQuery(query);
        Map<Integer, Long> result = new HashMap<>();
        ((Terms)srBuilder.get().getAggregations().asList().get(0)).getBuckets().forEach(a -> {
            result.put(a.getKeyAsNumber().intValue(),a.getDocCount());
        });

        return result;
    }

    public void delete(int catalogueId, List<String> deletes) {
        Iterable<Entry> list = entryMongoRepository.findAllById(deletes, getIndexName(catalogueId));
        list.forEach(a -> a.setGmtDeleted(1));
        entryMongoRepository.saveAll(list);
        entryElasticsearchRepository.saveAll(list);
    }

    public Entry get(int catalogueId, String id) {
        return entryMongoRepository.findById(id, getIndexName(catalogueId)).orElse(null);
    }

    private String getIndexName(int catalogueId){
        return String.format(INDEX_NAME_PREFIX + "%d", catalogueId);
    }
}
