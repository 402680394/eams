package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.model.*;
import com.ztdx.eams.domain.archives.model.entryItem.EntryItemConverter;
import com.ztdx.eams.domain.archives.repository.ArchivesGroupRepository;
import com.ztdx.eams.domain.archives.repository.ArchivesRepository;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import com.ztdx.eams.domain.archives.repository.DescriptionItemRepository;
import com.ztdx.eams.domain.archives.repository.elasticsearch.EntryElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.mongo.EntryMongoRepository;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class EntryService {

    private EntryElasticsearchRepository entryElasticsearchRepository;

    private EntryMongoRepository entryMongoRepository;

    private DescriptionItemRepository descriptionItemRepository;

    private CatalogueRepository catalogueRepository;

    private ArchivesRepository archivesRepository;

    private ArchivesGroupRepository archivesGroupRepository;

    private ElasticsearchOperations elasticsearchOperations;

    public EntryService(EntryElasticsearchRepository entryElasticsearchRepository, EntryMongoRepository entryMongoRepository, DescriptionItemRepository descriptionItemRepository, CatalogueRepository catalogueRepository, ArchivesRepository archivesRepository, ArchivesGroupRepository archivesGroupRepository, ElasticsearchOperations elasticsearchOperations) {
        this.entryElasticsearchRepository = entryElasticsearchRepository;
        this.entryMongoRepository = entryMongoRepository;
        this.descriptionItemRepository = descriptionItemRepository;
        this.catalogueRepository = catalogueRepository;
        this.archivesRepository = archivesRepository;
        this.archivesGroupRepository = archivesGroupRepository;
        this.elasticsearchOperations = elasticsearchOperations;
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
        this.convertEntryItems(entry);
        entryMongoRepository.save(entry);
        return entryElasticsearchRepository.save(entry);
    }

    public Entry update(Entry entry) {
        if (entry.getCatalogueId() == 0){
            throw new InvalidArgumentException("目录id不存在");
        }
        Optional<Entry> find = entryMongoRepository.findById(entry.getId(), "archive_record_" + entry.getCatalogueId());
        if (!find.isPresent()) {
            return save(entry);
        }
        find.get().setItems(entry.getItems());
        find.get().setGmtModified(new Date());
        this.convertEntryItems(entry);
        entryMongoRepository.save(find.get());
        return entryElasticsearchRepository.save(find.get());
    }

    public Page<Entry> search(int catalogueId, String queryString, Map<String, Object> itemQuery, Pageable pageable) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (queryString != null && queryString.length() > 0) {
            query.must(queryStringQuery(queryString));
        }
        query.must().addAll(parseQuery(catalogueId, itemQuery));
        query.filter(termQuery("gmtDeleted", 0));

        Page<Entry> result = entryElasticsearchRepository.search(
                query, pageable, new String[]{"archive_record_" + catalogueId}
        );
        result.stream().forEach(a -> {
            //TODO lijie 显示前需要按照著录项要求做输出的转换
            convertEntryItems(a);
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

    private void convertEntryItems(Entry entry) {
        Map<String, DescriptionItem> descriptionItemMap = this.getDescriptionItems(entry.getCatalogueId());
        Map<String, Object> convert = new HashMap<>();
        entry.getItems().forEach((key, vaule) -> {
            DescriptionItem item = descriptionItemMap.get(key);
            if (item != null) {
                Object val = EntryItemConverter.from(vaule, item);
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
        /*SearchRequestBuilder sb = elasticsearchOperations.getClient().prepareSearch("archive_record_*");

        sb.addAggregation(AggregationBuilders
                .global("agg")
                .subAggregation(AggregationBuilders.terms("catalogueId").field("catalogueId")));
        sb.setQuery(QueryBuilders.matchAllQuery());
        Global agg = sb.get().getAggregations().get("agg");
        return ((LongTerms)agg.getAggregations().asList().get(0)).getBuckets().get(0);*/
        return aggsCatalogueCount(null,null,"");
    }

    public Map<Integer, Long> aggsCatalogueCount(List<Integer> catalogueIds, List<Integer> archiveContentType, String keyWord) {
        Collection<String> indices = new ArrayList<>();
        if (catalogueIds == null || catalogueIds.size() == 0){
            indices.add("archive_record_*");
        }else{
            catalogueIds.forEach(a -> indices.add(String.format("archive_record_%d", a)));
        }

        SearchRequestBuilder srBuilder = elasticsearchOperations.getClient().prepareSearch(indices.toArray(new String[0]));

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
        Iterable<Entry> list = entryMongoRepository.findAllById(deletes, "archive_record_" + catalogueId);
        list.forEach(a -> a.setGmtDeleted(1));
        entryMongoRepository.saveAll(list);
        entryElasticsearchRepository.saveAll(list);
    }

    public Entry get(int catalogueId, String id) {
        return entryMongoRepository.findById(id, "archive_record_" + catalogueId).orElse(null);
    }
}
