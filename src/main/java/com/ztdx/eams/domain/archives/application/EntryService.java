package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.utils.FileHandler;
import com.ztdx.eams.domain.archives.application.task.EntryAsyncTask;
import com.ztdx.eams.domain.archives.model.*;
import com.ztdx.eams.domain.archives.model.Dictionary;
import com.ztdx.eams.domain.archives.model.entryItem.EntryItemConverter;
import com.ztdx.eams.domain.archives.model.event.EntryBoxNumberValidateEvent;
import com.ztdx.eams.domain.archives.repository.*;
import com.ztdx.eams.domain.archives.repository.elasticsearch.EntryElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.elasticsearch.OriginalTextElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.mongo.EntryMongoRepository;
import com.ztdx.eams.domain.archives.repository.mongo.IdGeneratorRepository;
import com.ztdx.eams.domain.archives.repository.mongo.IdGeneratorValue;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.join.query.JoinQueryBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.BasicQuery.query;
import static org.springframework.data.mongodb.core.query.Criteria.where;


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

    private OriginalTextElasticsearchRepository originalTextElasticsearchRepository;

    private MongoOperations mongoOperations;

    private IdGeneratorRepository idGeneratorRepository;

    private EntryAsyncTask entryAsyncTask;

    private ApplicationContext applicationContext;

    private ContentTypeRepository contentTypeRepository;

    private DictionaryRepository dictionaryRepository;

    private DictionaryClassificationRepository dictionaryClassificationRepository;


    public EntryService(EntryElasticsearchRepository entryElasticsearchRepository, EntryMongoRepository entryMongoRepository, DescriptionItemRepository descriptionItemRepository, CatalogueRepository catalogueRepository, ArchivesRepository archivesRepository, ArchivesGroupRepository archivesGroupRepository, ElasticsearchOperations elasticsearchOperations, OriginalTextElasticsearchRepository originalTextElasticsearchRepository, MongoOperations mongoOperations, IdGeneratorRepository idGeneratorRepository, EntryAsyncTask entryAsyncTask, ApplicationContext applicationContext, ContentTypeRepository contentTypeRepository, DictionaryRepository dictionaryRepository, DictionaryClassificationRepository dictionaryClassificationRepository) {
        this.entryElasticsearchRepository = entryElasticsearchRepository;
        this.entryMongoRepository = entryMongoRepository;
        this.descriptionItemRepository = descriptionItemRepository;
        this.catalogueRepository = catalogueRepository;
        this.archivesRepository = archivesRepository;
        this.archivesGroupRepository = archivesGroupRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.originalTextElasticsearchRepository = originalTextElasticsearchRepository;
        this.mongoOperations = mongoOperations;
        this.idGeneratorRepository = idGeneratorRepository;
        this.entryAsyncTask = entryAsyncTask;
        this.applicationContext = applicationContext;
        this.contentTypeRepository = contentTypeRepository;
        this.dictionaryRepository = dictionaryRepository;
        this.dictionaryClassificationRepository = dictionaryClassificationRepository;
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
        this.convertEntryItems(entry, EntryItemConverter::from, true, true);
        entry = entryMongoRepository.save(entry);

        entryAsyncTask.copyItemsFieldToSystemField(entry);
        return entry;
    }

    public Entry update(Entry entry) {
        if (entry.getCatalogueId() == 0) {
            throw new InvalidArgumentException("目录id不存在");
        }

        if (StringUtils.isEmpty(entry.getId())) {
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
        this.convertEntryItems(update, EntryItemConverter::from, false, true);
        update = entryMongoRepository.save(update);

        entryAsyncTask.copyItemsFieldToSystemField(update);
        return update;
    }

    public void initIndex(int catalogueId) {
        try {
            entryElasticsearchRepository.createIndex(this.getIndexName(catalogueId));
        } catch (IOException e) {
            throw new BusinessException("索引初始化失败", e);
        }
    }

    public Page<Entry> search(int catalogueId, String queryString, QueryBuilder itemQuery, Pageable pageable, int isDeleted) {
        return search(catalogueId, queryString, itemQuery, null, null, pageable, isDeleted);
    }

    public Page<Entry> search(int catalogueId
            , String queryString
            , QueryBuilder itemQuery
            , String parentId
            , Integer owner
            , Pageable pageable
            , int isDeleted) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (queryString != null && queryString.length() > 0) {
            query.must(queryStringQuery(queryString).defaultOperator(Operator.AND));
        }

        if (parentId != null) {
            if (!StringUtils.isEmpty(parentId)) {
                query.filter(termQuery("parentId", parentId));
            } else {
                query.filter(boolQuery().mustNot(existsQuery("parentId")));
            }
        } else {
            //query.must(existsQuery("parentId"));
        }

        if (owner != null) {
            query.filter(termQuery("owner", owner));
        }

        if (itemQuery != null) {
            query.filter(itemQuery);
        }

        query.filter(termQuery("gmtDeleted", isDeleted));

        Page<Entry> searchResult = entryElasticsearchRepository.search(
                query, pageable, new String[]{getIndexName(catalogueId)}
        );

        List<String> ids = searchResult.getContent().stream().map(Entry::getId).collect(Collectors.toList());

        List<Entry> result = (List<Entry>) entryMongoRepository.findAllById(ids, getIndexName(catalogueId));

        result.sort(Comparator.comparing(a -> ids.indexOf(a.getId())));

        result.forEach(a -> {
            convertEntryItems(a, EntryItemConverter::format, false);
            Map<String, Object> items = a.getItems();
            items.put("id", a.getId());
        });

        return PageableExecutionUtils.getPage(result, pageable, searchResult::getTotalElements);
    }

    private void convertEntryItems(Entry entry, BiFunction<Entry, DescriptionItem, Object> operator, boolean isGenerator, boolean isValidate) {
        Map<String, DescriptionItem> descriptionItemMap = this.getDescriptionItems(entry.getCatalogueId());
        Map<String, Object> convert = new HashMap<>();

        descriptionItemMap.forEach((key, item) -> {
            Object val;
            if (isGenerator && item.getIsIncrement() == 1 && item.getIncrement() > 0) {
                String idKey = String.format(IdGeneratorValue.ENTRY_ITEM_INCREMENT_FORMAT, entry.getCatalogueId(), item.getMetadataName());
                val = idGeneratorRepository.get(idKey, item.getIncrement());
            } else {
                val = operator.apply(entry, item);
            }

            if (isValidate) {
                entryValidate(entry, val, item);
            }

            if (val != null) {
                convert.put(key, val);
            }
        });

        entry.setItems(convert);
    }

    private void convertEntryItems(Entry entry, BiFunction<Entry, DescriptionItem, Object> operator, boolean isGenerator) {
        convertEntryItems(entry, operator, isGenerator, false);
    }

    private void entryValidate(Entry entry, Object val, DescriptionItem item) {
        if (val == null) {
            return;
        }
        switch (item.getPropertyType()) {
            case BoxNumber:
                applicationContext.publishEvent(
                        new EntryBoxNumberValidateEvent(
                                this
                                , entry.getArchiveId()
                                , val.toString()));
                break;
            default:
                break;
        }
    }

    private Map<String, DescriptionItem> getDescriptionItems(int catalogueId) {
        return descriptionItemRepository.findByCatalogueId(catalogueId).stream().collect(Collectors.toMap(DescriptionItem::getMetadataName, (d) -> d, (d1, d2) -> d2));
    }

    public Map<Integer, Long> aggsCatalogueCount(List<Integer> catalogueIds, List<Integer> archiveContentType, String keyWord) {
        Collection<String> indices = new ArrayList<>();
        if (catalogueIds == null || catalogueIds.size() == 0) {
            indices.add(INDEX_NAME_PREFIX + "*");
        } else {
            catalogueIds.forEach(a -> indices.add(getIndexName(a)));
        }

        SearchRequestBuilder srBuilder = elasticsearchOperations.getClient().prepareSearch(indices.toArray(new String[0]));
        srBuilder.setTypes("record");

        srBuilder.addAggregation(
                AggregationBuilders
                        .terms("catalogueId")
                        .field("catalogueId")
                        .size(50)
                        .order(Terms.Order.count(false)));
        BoolQueryBuilder query;
        if (archiveContentType != null && archiveContentType.size() > 0) {
            query = QueryBuilders.boolQuery().filter(
                    QueryBuilders.termsQuery("archiveContentType", archiveContentType));
        } else {
            query = QueryBuilders.boolQuery();
        }

        if (keyWord != null && !"".equals(keyWord)) {
            query.must(QueryBuilders.queryStringQuery(keyWord).defaultOperator(Operator.AND));
        }

        query.filter(QueryBuilders.termQuery("gmtDeleted", 0));

        srBuilder.setQuery(query).setSize(0);
        Map<Integer, Long> result = new HashMap<>();
        ((Terms) srBuilder.get().getAggregations().asList().get(0)).getBuckets().forEach(
                a -> result.put(a.getKeyAsNumber().intValue(), a.getDocCount()));

        return result;
    }

    public void deleteOrReduction(int catalogueId, Collection<String> entryIds, int isDeleted) {
        if (entryIds == null || !entryIds.iterator().hasNext()) {
            return;
        }
        Iterable<Entry> list = entryMongoRepository.findAllById(entryIds, getIndexName(catalogueId));
        list.forEach(a -> a.setGmtDeleted(isDeleted));

        //如果目录类型为案卷，将其卷内条目也删除
        Catalogue folderCatalogue = catalogueRepository.findById(catalogueId).orElse(null);
        assert folderCatalogue != null;
        if (folderCatalogue.getCatalogueType().equals(CatalogueType.Folder)) {
            //通过档案库id和目录类型获得卷内目录
            Optional<Catalogue> folderFileCatalogue = catalogueRepository.findByArchivesIdAndCatalogueType(folderCatalogue.getArchivesId(), CatalogueType.FolderFile);

            if (!folderFileCatalogue.isPresent()) {
                throw new InvalidArgumentException("卷内目录不存在");
            }

            //获得卷内条目集合
            Iterable<Entry> folderFileEntryList = entryMongoRepository.findAll(query(where("parentId").in(entryIds).and("gmtDeleted").is(0)), getIndexName(folderFileCatalogue.get().getId()));
            folderFileEntryList.forEach(entry -> entry.setGmtDeleted(isDeleted));

            entryMongoRepository.saveAll(folderFileEntryList);
            entryAsyncTask.indexAll(folderFileEntryList, folderFileCatalogue.get().getId());
        }

        entryMongoRepository.saveAll(list);
        entryAsyncTask.indexAll(list, catalogueId);
    }

    public Entry get(int catalogueId, String id) {
        return entryMongoRepository.findById(id, getIndexName(catalogueId)).orElse(null);
    }

    private String getIndexName(Integer catalogueId) {
        if (catalogueId == null) {
            return INDEX_NAME_PREFIX + "*";
        } else {
            return String.format(INDEX_NAME_PREFIX + "%d", catalogueId);
        }
    }

    public AggregatedPage<OriginalText> searchFulltext(List<Integer> archiveContentType
            , List<String> searchParams
            , String includeWords
            , String rejectWords
            , Pageable pageable) {
        return searchFulltext(archiveContentType, searchParams, includeWords, null, rejectWords, null, null, null, pageable);
    }

    public AggregatedPage<OriginalText> searchFulltext(List<Integer> archiveContentType
            , List<String> searchParams
            , String queryString
            , String includeWords
            , String rejectWords
            , Collection<Integer> catalogueIds
            , Map<String, Object> items
            , List<TermsAggregationParam> aggs
            , Pageable pageable) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        if (catalogueIds == null || catalogueIds.size() == 0) {
            return null;
        }

        makeFulltextQuery(
                archiveContentType
                , searchParams
                , queryString
                , includeWords
                , rejectWords
                , catalogueIds
                , items
                , query);

        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        builder.withQuery(query);
        builder.withPageable(pageable);

        addAggs(aggs, builder);

        HighlightBuilder.Field field = new HighlightBuilder.Field("contentIndex");
        field.requireFieldMatch(false);
        field.fragmentSize(100);
        field.numOfFragments(2);

        builder.withHighlightFields(field);

        String[] indices = catalogueIds.stream().map(a -> INDEX_NAME_PREFIX + a).collect(Collectors.toList()).toArray(new String[]{});

        return (AggregatedPage<OriginalText>) originalTextElasticsearchRepository.search(builder.build(), indices);
    }

    private void makeFulltextQuery(
            List<Integer> archiveContentType
            , List<String> searchParams
            , String queryString
            , String includeWords
            , String rejectWords
            , Collection<Integer> catalogueIds
            , Map<String, Object> items
            , BoolQueryBuilder query
    ) {
        BoolQueryBuilder queryByOr = QueryBuilders.boolQuery();
        BoolQueryBuilder queryAgain = QueryBuilders.boolQuery();
        BoolQueryBuilder fileQueryByAnd = QueryBuilders.boolQuery();
        BoolQueryBuilder entryQueryByAnd = QueryBuilders.boolQuery();

        addEntryQuery(entryQueryByAnd, catalogueIds, items);

        if (archiveContentType != null && archiveContentType.size() > 0) {
            entryQueryByAnd.filter(QueryBuilders.termsQuery("archiveContentType", archiveContentType));
        }

        fileQueryByAnd.filter(termQuery("gmtDeleted", 0));
        entryQueryByAnd.filter(termQuery("gmtDeleted", 0));

        if (searchParams.contains(SearchFulltextOption.entry.name())) {
            if (!StringUtils.isEmpty(queryString)) {
                queryByOr.should(JoinQueryBuilders.hasParentQuery(
                        "record"
                        , queryStringQuery(queryString).field(FULL_CONTENT).defaultOperator(Operator.AND)
                        , false));
            }
            if (!StringUtils.isEmpty(includeWords)) {
                queryAgain.should(JoinQueryBuilders.hasParentQuery(
                        "record"
                        , queryStringQuery(includeWords).field(FULL_CONTENT).defaultOperator(Operator.AND)
                        , false));
            }
            if (!StringUtils.isEmpty(rejectWords)) {
                entryQueryByAnd.mustNot(queryStringQuery(rejectWords).field(FULL_CONTENT).defaultOperator(Operator.AND));
            }
        }

        if (searchParams.contains(SearchFulltextOption.file.name())) {
            if (!StringUtils.isEmpty(queryString)) {
                queryByOr.should(queryStringQuery(queryString).defaultOperator(Operator.AND));
            }
            if (!StringUtils.isEmpty(includeWords)) {
                queryAgain.should(queryStringQuery(includeWords).defaultOperator(Operator.AND));
            }
            if (!StringUtils.isEmpty(rejectWords)) {
                fileQueryByAnd.mustNot(queryStringQuery(rejectWords).defaultOperator(Operator.AND));
            }
        }
        query.must(fileQueryByAnd);
        query.must(JoinQueryBuilders.hasParentQuery(
                "record"
                , entryQueryByAnd
                , false));
        query.must(queryByOr);
        query.must(queryAgain);
    }

    public List<TermsAggregationResult> convertAggregationToResult(Aggregations aggregations, List<TermsAggregationParam> params) {
        Map<String, TermsAggregationParam> map =
                params.stream().collect(Collectors.toMap(TermsAggregationParam::getField, a -> a));


        List<TermsAggregationResult> result = new ArrayList<>();

        if (aggregations == null) {
            return result;
        }

        aggregations.forEach(aggregation -> {

            TermsAggregationParam param = map.get(aggregation.getName());

            TermsAggregationResult termsAggregationResult = new TermsAggregationResult(
                    aggregation.getName()
                    , param.getName()
                    , 0
            );

            result.add(termsAggregationResult);

            if (Terms.class.isAssignableFrom(aggregation.getClass())) {
                Terms terms = (Terms) aggregation;
                termsAggregationResult.getChildren().addAll(convertBucketsToResult(terms.getBuckets(), param.getChildren()));
            }

            termsAggregationResult.setCount(
                    termsAggregationResult
                            .getChildren()
                            .stream()
                            .reduce(0, (a, b) -> a + b.getCount(), Integer::sum));
        });

        return result;
    }

    private Collection<? extends TermsAggregationResult> convertBucketsToResult(List<? extends Terms.Bucket> buckets, List<TermsAggregationParam> params) {
        List<TermsAggregationResult> result = new ArrayList<>();

        if (buckets == null) {
            return result;
        }

        buckets.forEach(bucket -> {

            TermsAggregationResult termsAggregationResult = new TermsAggregationResult(
                    bucket.getKeyAsString()
                    , bucket.getKeyAsString()
                    , (int) bucket.getDocCount()
            );

            result.add(termsAggregationResult);

            termsAggregationResult.getChildren().addAll(convertAggregationToResult(bucket.getAggregations(), params));
        });

        return result;
    }

    private void addEntryQuery(BoolQueryBuilder entryQuery, Collection<Integer> catalogueIds, Map<String, Object> items) {
        if (catalogueIds == null || catalogueIds.size() != 1 || items == null || items.size() == 0) {
            return;
        }

        int catalogueId = catalogueIds.iterator().next();

        Map<String, DescriptionItemDataType> map =
                descriptionItemRepository
                        .findByCatalogueId(catalogueId)
                        .stream()
                        .collect(Collectors.toMap(DescriptionItem::getMetadataName, DescriptionItem::getDataType));

        items.forEach((field, value) -> {
            String searchFieldName;
            if (field.indexOf("_") == 0) {
                searchFieldName = field.substring(1);
            } else {
                searchFieldName = String.format("items.%s", field);
            }

            switch (map.get(field)) {
                case Array:
                case Integer:
                case Double:
                case Date:
                    entryQuery.must(QueryBuilders.termQuery(searchFieldName, value));
                    break;
                case String:
                    if (value.toString().contains("*") || value.toString().contains("?")) {
                        entryQuery.must(QueryBuilders.wildcardQuery(searchFieldName, value.toString()));
                    } else {
                        entryQuery.must(QueryBuilders.termQuery(searchFieldName, value));
                    }
                    break;
                case Text:
                    entryQuery.must(QueryBuilders.matchQuery(searchFieldName, value).operator(Operator.AND));
                    break;
            }
        });
    }

    private void addAggs(List<TermsAggregationParam> aggs, SearchRequestBuilder builder) {

        if (aggs == null || aggs.size() == 0) {
            return;
        }

        aggs.forEach(termsAggregationParam -> {
            AbstractAggregationBuilder agg = addChildAggs(termsAggregationParam);
            builder.addAggregation(agg);
        });
    }

    private void addAggs(List<TermsAggregationParam> aggs, NativeSearchQueryBuilder builder) {

        if (aggs == null || aggs.size() == 0) {
            return;
        }

        aggs.forEach(termsAggregationParam -> {
            AbstractAggregationBuilder agg = addChildAggs(termsAggregationParam);
            builder.addAggregation(agg);
        });
    }

    private AbstractAggregationBuilder addChildAggs(TermsAggregationParam termsAggregationParam) {
        AbstractAggregationBuilder agg = AggregationBuilders
                .terms(termsAggregationParam.getField())
                .field(termsAggregationParam.getField())
                .size(termsAggregationParam.getSize())
                .order(Terms.Order.count(false));

        addChildAggs(termsAggregationParam.getChildren(), agg);
        return agg;
    }

    private void addChildAggs(List<TermsAggregationParam> termsAggregationParams, AbstractAggregationBuilder agg) {
        if (termsAggregationParams.size() > 0) {
            termsAggregationParams.forEach(child -> {
                AggregationBuilder aggregationBuilder = addChildAggs(child);
                if (aggregationBuilder != null) {
                    agg.subAggregation(aggregationBuilder);
                }
            });
        }
    }

    public Iterable<Entry> findAllById(Collection<String> entryIds, Integer catalogueId) {
        if (catalogueId == null) {
            return entryElasticsearchRepository.search(QueryBuilders.termsQuery("id", entryIds), new String[]{getIndexName(null)});
        } else {
            return entryMongoRepository.findAllById(entryIds, getIndexName(catalogueId));
        }
    }

    /**
     * 更改批量鉴定
     */
    public void batchIdentification(List<String> entryIds, Integer catalogueId, Integer isOpen, boolean isExpired, boolean isEndangered, boolean isLose) {

        //通过条目id和目录id获得所有的条目集合
        Iterable<Entry> entryList = entryMongoRepository.findAllById(entryIds, "archive_record_" + catalogueId);

        //遍历条目集合给每个条目设置鉴定状态
        for (Entry entry : entryList) {
            entry.setIsOpen(OpenStatus.create(isOpen));
            entry.setExpired(isExpired);
            entry.setEndangered(isEndangered);
            entry.setLose(isLose);
        }

        //存进mongoDb
        entryMongoRepository.saveAll(entryList);

        entryAsyncTask.indexAll(entryList, catalogueId);
    }

    /**
     * 查看单个鉴定信息
     */
    public Map<String, Object> getIdentification(String entryId, Integer cid) {

        Map<String, Object> result = new HashMap<>();
        //通过id获得条目信息
        Optional<Entry> find = entryMongoRepository.findById(entryId, "archive_record_" + cid);
        if (find.isPresent()) {

            Entry entry = find.get();
            result.put("isOpen", entry.getIsOpen());
            result.put("isExpired", entry.isExpired());
            result.put("isEndangered", entry.isEndangered());
            result.put("isLose", entry.isLose());

            return result;
        }

        return null;

    }

    /**
     * 组新卷
     *
     * @param folderFileEntryIds    卷内条目id集合
     * @param folderFileCatalogueId 卷内目录ID
     * @param entry                 案卷条目
     */
    public void setNewVolume(List<String> folderFileEntryIds, int folderFileCatalogueId, Entry entry) {

        //通过条目id和目录id获得所有的条目集合
        Iterable<Entry> entryList = entryMongoRepository.findAllById(folderFileEntryIds, this.getIndexName(folderFileCatalogueId));

        //通过卷内目录id获得目录
        Catalogue folderFileCatalogue = catalogueRepository.findById(folderFileCatalogueId).orElse(null);
        //通过档案库id和目录类型为案卷获得案卷目录
        assert folderFileCatalogue != null;
        Optional<Catalogue> folderCatalogueList = catalogueRepository.findByArchivesIdAndCatalogueType(folderFileCatalogue.getArchivesId(), CatalogueType.Folder);

        if (!folderCatalogueList.isPresent()) {
            throw new InvalidArgumentException("卷内目录不存在");
        }

        //把案卷目录id设置到Entry上
        folderCatalogueList.ifPresent(catalogue -> entry.setCatalogueId(catalogue.getId()));

        //新增案卷
        Entry newEntry = save(entry);

        //修改卷内parentID
        for (Entry entryForList : entryList) {
            entryForList.setParentId(newEntry.getId());
        }

        entryMongoRepository.saveAll(entryList);

        entryAsyncTask.indexAll(entryList, folderFileCatalogueId);
    }

    /**
     * 拆卷(通过卷内)
     *
     * @param folderFileEntryIds    卷内条目id集合
     * @param folderFileCatalogueId 卷内目录ID
     */
    public void separateVolume(List<String> folderFileEntryIds, int folderFileCatalogueId) {

        //获得卷内条目集合
        Iterable<Entry> folderFileEntryList = entryMongoRepository.findAllById(folderFileEntryIds, this.getIndexName(folderFileCatalogueId));

        //置空卷内条目中的上级id
        for (Entry entry : folderFileEntryList) {
            entry.setParentId(null);
        }

        entryMongoRepository.saveAll(folderFileEntryList);

        entryAsyncTask.indexAll(folderFileEntryList, folderFileCatalogueId);
    }

    /**
     * 拆卷(通过案卷)
     *
     * @param folderEntryId     案卷条目id
     * @param folderCatalogueId 案卷目录ID
     */
    public void separateVolumeForFolder(String folderEntryId, int folderCatalogueId) {

        //获取案卷目录
        Catalogue folderCatalogue = catalogueRepository.findById(folderCatalogueId).orElse(null);
        //通过档案库id和目录类型获得卷内目录
        assert folderCatalogue != null;
        Optional<Catalogue> folderFileCatalogue = catalogueRepository.findByArchivesIdAndCatalogueType(folderCatalogue.getArchivesId(), CatalogueType.FolderFile);

        if (!folderFileCatalogue.isPresent()) {
            throw new InvalidArgumentException("卷内目录不存在");
        }

        //获得卷内条目集合
        Iterable<Entry> folderFileEntryList = entryMongoRepository.findAll(query(where("parentId").is(folderEntryId)), getIndexName(folderFileCatalogue.get().getId()));

        //置空卷内条目中的上级id
        folderFileEntryList.forEach(entry -> entry.setParentId(null));

        entryMongoRepository.saveAll(folderFileEntryList);

        entryAsyncTask.indexAll(folderFileEntryList, folderFileCatalogue.get().getId());
    }


    public Page<Entry> scrollEntry(boolean archivingAll, int catalogueId, Collection<String> srcData, int page, int size) {
        Query query;
        if (archivingAll) {
            query = query(where("gmtDeleted")
                    .is(0))
                    .with(PageRequest.of(page, size));
        } else {
            query = query(where("gmtDeleted")
                    .is(0)
                    .and("_id")
                    .in(srcData))
                    .with(PageRequest.of(page, size));
        }
        long total = mongoOperations.count(query, getIndexName(catalogueId));
        List<Entry> list = entryMongoRepository.findAll(query, getIndexName(catalogueId));
        return PageableExecutionUtils.getPage(list, PageRequest.of(page, size), () -> total);
    }

    public Page<Entry> scrollSubEntry(int catalogueId, Collection<String> parentIds, int page, int size) {
        Query query = query(where("gmtDeleted")
                .is(0)
                .and("parentId")
                .in(parentIds));
        long total = mongoOperations.count(query, getIndexName(catalogueId));
        List<Entry> list = entryMongoRepository.findAll(query, getIndexName(catalogueId));
        return PageableExecutionUtils.getPage(list, PageRequest.of(page, size), () -> total);
    }

    //TODO @lijie mongo中的索引 parentId parentCatalogueId catalogueId srcEntryId srcCatalogueId
    public List<ArchivingResult> archivingEntry(
            int trgId
            , Integer parentTrgId
            , List<String> srcFields
            , List<String> trgFields
            , Map<String, String> srcData
            , Map<String, String> parentData
            , Iterable<Entry> entries
            , int owner
            , Function<Entry, String> getTitle
    ) {

        Catalogue trgCatalogue = catalogueRepository.findById(trgId).orElse(null);
        Assert.notNull(trgCatalogue, "目标目录不存在");

        Archives trgArchives = archivesRepository.findById(trgCatalogue.getArchivesId()).orElse(null);
        Assert.notNull(trgArchives, "目标档案库不存在");

        ArchivesGroup trgArchivesGroup = archivesGroupRepository.findById(trgArchives.getArchivesGroupId()).orElse(null);
        Assert.notNull(trgArchivesGroup, "目标档案库分组不存在");

        List<Entry> targets = new ArrayList<>();
        List<ArchivingResult> error = new ArrayList<>();
        entries.forEach(a -> {
            String parentId = null;
            if (parentData != null) {
                parentId = parentData.getOrDefault(a.getParentId(), null);
            }
            String id = srcData.getOrDefault(a.getId(), null);

            String msg = "成功";
            ArchivingResult.Status status = ArchivingResult.Status.success;
            try {
                Entry add = this.copy(
                        a
                        , id
                        , trgId
                        , trgCatalogue
                        , trgArchives
                        , trgArchivesGroup
                        , parentTrgId
                        , parentId
                        , owner
                        , srcFields
                        , trgFields);
                targets.add(add);
            } catch (EntryValueConverException e) {
                msg = e.getMessage();
                status = ArchivingResult.Status.failure;
            }

            error.add(
                    new ArchivingResult(
                            a.getId()
                            , a.getParentId()
                            , getTitle.apply(a)
                            , msg
                            , ArchivingResult.Type.entry
                            , status
                            , a.getCatalogueId())
            );
        });

        entryMongoRepository.saveAll(targets);
        entryAsyncTask.indexAll(targets, trgId);

        return error;
    }

    public Map<String, String> generatorId(Collection<String> srcIds) {
        return srcIds.parallelStream().collect(Collectors.toMap(a -> a, b -> UUID.randomUUID().toString()));
    }

    private Entry copy(
            Entry entry
            , String id
            , int trgId
            , Catalogue catalogue
            , Archives archive
            , ArchivesGroup archivesGroup
            , Integer parentCatalogueId
            , String parentId
            , int owner
            , List<String> srcFields
            , List<String> trgFields) {
        Entry result = new Entry();
        result.setId(id);
        result.setCatalogueId(trgId);
        result.setCatalogueType(catalogue.getCatalogueType());
        result.setArchiveId(archive.getId());
        result.setArchiveType(archive.getType());
        result.setArchiveContentType(archive.getContentTypeId());
        result.setFondsId(archivesGroup.getFondsId());
        result.setOwner(owner);
        result.setParentCatalogueId(parentCatalogueId);
        result.setParentId(parentId);
        result.setSrcCatalogueId(entry.getCatalogueId());
        result.setSrcEntryId(entry.getId());
        result.setGmtCreate(new Date());
        result.setGmtModified(new Date());
        Map<String, Object> items = new HashMap<>();
        for (int i = 0; i < srcFields.size(); i++) {
            String srcField = srcFields.get(i);
            String trgField = trgFields.get(i);
            Object srcValue = entry.getItems().getOrDefault(srcField, null);
            items.put(trgField, srcValue);
        }
        result.setItems(items);

        this.convertEntryItems(result, EntryItemConverter::from, true, true);

        return result;
    }

    public void inBox(int catalogueId, Collection<String> ids, String boxCode) {
        DescriptionItem item =
                descriptionItemRepository.findByCatalogueIdAndPropertyType(catalogueId, PropertyType.BoxNumber);
        if (item == null) {
            throw new InvalidArgumentException("没有盒号字段");
        }

        Iterable<Entry> list = findAllById(ids, catalogueId);
        if (list == null || !list.iterator().hasNext()) {
            throw new InvalidArgumentException("条目不存在");
        }

        list.forEach(entry -> entry.getItems().put(item.getMetadataName(), boxCode));

        entryMongoRepository.saveAll(list);
        entryAsyncTask.indexAll(list, catalogueId);
    }

    public Set<String> unBox(int catalogueId, Collection<String> ids) {
        DescriptionItem item =
                descriptionItemRepository.findByCatalogueIdAndPropertyType(catalogueId, PropertyType.BoxNumber);
        if (item == null) {
            throw new InvalidArgumentException("没有盒号字段");
        }

        Iterable<Entry> list = findAllById(ids, catalogueId);
        if (list == null || !list.iterator().hasNext()) {
            throw new InvalidArgumentException("条目不存在");
        }

        Set<String> result = new HashSet<>();
        list.forEach(entry -> {
            Object boxCode = entry.getItems().getOrDefault(item.getMetadataName(), null);
            if (boxCode == null || StringUtils.isEmpty(boxCode.toString())) {
                return;
            }

            //盒号字段置空
            entry.getItems().put(item.getMetadataName(), null);

            result.add(boxCode.toString());
        });

        entryMongoRepository.saveAll(list);
        entryAsyncTask.indexAll(list, catalogueId);

        return result;
    }

    public List<GroupCount> groupCountPageCountByBox(Collection<String> boxCodes, int catalogueId) {

        DescriptionItem boxNoItem =
                descriptionItemRepository.findByCatalogueIdAndPropertyType(catalogueId, PropertyType.BoxNumber);
        if (boxNoItem == null) {
            return null;
        }

        DescriptionItem pageTotalItem =
                descriptionItemRepository.findByCatalogueIdAndPropertyType(catalogueId, PropertyType.PageTotal);
        if (pageTotalItem == null) {
            return null;
        }

        String boxNoField = String.format("items.%s", boxNoItem.getMetadataName());
        String pageTotalField = String.format("items.%s", pageTotalItem.getMetadataName());

        Aggregation agg = Aggregation.newAggregation(
                match(where(boxNoField).in(boxCodes).and("gmtDeleted").is(0))
                , group(boxNoField).sum(pageTotalField).as("count")
                , project("count").and("key").previousOperation()
        );

        return mongoOperations.aggregate(agg, getIndexName(catalogueId), GroupCount.class).getMappedResults();
    }

    public Map<String, List<String>> groupByBox(Collection<String> boxCodes, int catalogueId) {
        DescriptionItem boxNoItem =
                descriptionItemRepository.findByCatalogueIdAndPropertyType(catalogueId, PropertyType.BoxNumber);
        if (boxNoItem == null) {
            return null;
        }

        String boxNoField = String.format("items.%s", boxNoItem.getMetadataName());

        return entryMongoRepository.findAll(
                query(where(boxNoField).in(boxCodes).and("gmtDeleted").is(0))
                , getIndexName(catalogueId)
        ).stream()
                .collect(
                        Collectors.groupingBy(
                                (Entry a) -> a.getItems().get(boxNoItem.getMetadataName()).toString()
                                , Collector.of(
                                        ArrayList::new
                                        , (list, entry) -> list.add(entry.getId())
                                        , (left, right) -> {
                                            left.addAll(right);
                                            return left;
                                        })
                        ));
    }

    public void unBoxByBoxCode(int catalogueId, List<String> boxCodes) {

        DescriptionItem item =
                descriptionItemRepository.findByCatalogueIdAndPropertyType(catalogueId, PropertyType.BoxNumber);
        if (item == null) {
            throw new InvalidArgumentException("没有盒号字段");
        }

        String boxNumberColumnName = String.format("items.%s", item.getMetadataName());

        List<Entry> searchResult = entryMongoRepository.findAll(
                query(where(boxNumberColumnName).in(boxCodes))
                , getIndexName(catalogueId)
        );

        searchResult.forEach(entry -> entry.getItems().put(item.getMetadataName(), null));

        entryMongoRepository.saveAll(searchResult);
        entryAsyncTask.indexAll(searchResult, catalogueId);
    }

    public Page<Entry> listInBox(int catalogueId, String boxCode, Pageable pageable) {
        DescriptionItem boxNumberItem = descriptionItemRepository.findByCatalogueIdAndPropertyType(catalogueId, PropertyType.BoxNumber);
        String prefix = "items.%s";
        String boxNumberColumnName = String.format(prefix, boxNumberItem.getMetadataName());

        Query query = query(where(boxNumberColumnName).is(boxCode)).with(pageable);
        long total = mongoOperations.count(query, getIndexName(catalogueId));
        List<Entry> list = entryMongoRepository.findAll(query, getIndexName(catalogueId));
        return PageableExecutionUtils.getPage(list, pageable, () -> total);
    }

    //统计（按档案类型-保管期限）
    public Map<String, Object> statisticsTypeTerm(int fondsId, int beginYear, int endYear) {
        //查询全宗下归档库除去案卷所有目录
        List<Integer> catalogueIds = catalogueRepository.findCatalogueIdByfondsId(fondsId);

        Collection<String> indices = new ArrayList<>();
        if (catalogueIds == null || catalogueIds.size() == 0) {
            indices.add(INDEX_NAME_PREFIX + "*");
        } else {
            catalogueIds.forEach(a -> indices.add(getIndexName(a)));
        }
        SearchRequestBuilder srBuilder = elasticsearchOperations.getClient().prepareSearch(indices.toArray(new String[0]));
//        SearchRequestBuilder srBuilder = elasticsearchOperations.getClient().prepareSearch("archive_record_10");
        srBuilder.setTypes("record").addAggregation(
                AggregationBuilders
                        .terms("archiveContentType")
                        .field("archiveContentType")
                        .subAggregation(AggregationBuilders
                                .terms("timeLimitForStorage")
                                .field("timeLimitForStorage.keyword"))).setSize(0);

        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.filter(QueryBuilders.termQuery("gmtDeleted", 0));
        query.filter(QueryBuilders.rangeQuery("year").gte(beginYear).lte(endYear));
        srBuilder.setQuery(query);

        SearchResponse response = srBuilder.get();

        List<ContentType> contentTypes = contentTypeRepository.findAll();

        Optional<DictionaryClassification> optional = dictionaryClassificationRepository.findByCode("BGQX");
        if (!optional.isPresent()) {
            throw new BusinessException("保管期限词典分类不存在");
        }
        List<Dictionary> dictionaries = dictionaryRepository.findByClassificationId(optional.get().getId());

        List<Terms.Bucket> typeBuckets = (List<Terms.Bucket>) ((Terms) response.getAggregations().get("archiveContentType")).getBuckets();

        HashMap<String, Object> result = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();

        //内容类型
        for (Terms.Bucket typeBucket : typeBuckets) {
            //档案内容类型总数
            long otherTotal = typeBucket.getDocCount();

            for (ContentType contentType : contentTypes) {
                if (contentType.getId() == typeBucket.getKeyAsNumber().intValue()) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("name", contentType.getName());

                    //保管期限
                    Terms terms = typeBucket.getAggregations().get("timeLimitForStorage");
                    List<Terms.Bucket> timeLimitBuckets = (List<Terms.Bucket>) terms.getBuckets();
                    for (Dictionary dictionary : dictionaries) {
                        long timeLimitTotal = 0;
                        for (Terms.Bucket timeLimitBucket : timeLimitBuckets) {

                            if (dictionary.getName().equals(timeLimitBucket.getKey())
                                    || dictionary.getCode().equals(timeLimitBucket.getKey())
                                    || (dictionary.getCode() + " " + dictionary.getName()).equals(timeLimitBucket.getKey())
                                    || (dictionary.getName() + " " + dictionary.getCode()).equals(timeLimitBucket.getKey())) {
                                //减去使用的保管期限类型数量
                                otherTotal -= timeLimitBucket.getDocCount();

                                timeLimitTotal = timeLimitBucket.getDocCount();
                            }
                        }
                        map.put(dictionary.getName(), timeLimitTotal);
                    }
                    map.put("其他", otherTotal);
                    map.put("合计", typeBucket.getDocCount());
                    items.add(map);
                }
            }
        }
        List<String> timeLimitNames = new ArrayList<>();
        dictionaries.forEach(a -> timeLimitNames.add(a.getName()));

        timeLimitNames.add("其他");
        timeLimitNames.add("合计");
        result.put("fields", timeLimitNames);
        result.put("items", items);
        return result;
    }

    //统计（按档案类型-年度）
    public Map<String, Object> statisticsTypeYear(int fondsId, int beginYear, int endYear) {
        //查询全宗下归档库除去案卷所有目录
        List<Integer> catalogueIds = catalogueRepository.findCatalogueIdByfondsId(fondsId);

        Collection<String> indices = new ArrayList<>();
        if (catalogueIds == null || catalogueIds.size() == 0) {
            indices.add(INDEX_NAME_PREFIX + "*");
        } else {
            catalogueIds.forEach(a -> indices.add(getIndexName(a)));
        }
        SearchRequestBuilder srBuilder = elasticsearchOperations.getClient().prepareSearch(indices.toArray(new String[0]));
//        SearchRequestBuilder srBuilder = elasticsearchOperations.getClient().prepareSearch(INDEX_NAME_PREFIX + "10");
        srBuilder.setTypes("record").addAggregation(
                AggregationBuilders
                        .terms("year")
                        .field("year")
                        .subAggregation(AggregationBuilders
                                .terms("archiveContentType")
                                .field("archiveContentType"))).setSize(0);

        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.filter(QueryBuilders.termQuery("gmtDeleted", 0));
        query.filter(QueryBuilders.rangeQuery("year").gte(beginYear).lte(endYear));
        srBuilder.setQuery(query);

        SearchResponse response = srBuilder.get();

        List<ContentType> contentTypes = contentTypeRepository.findAll();

        List<Terms.Bucket> yearBuckets = (List<Terms.Bucket>) ((Terms) response.getAggregations().get("year")).getBuckets();

        HashMap<String, Object> result = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();

        //年度
        for (int year = beginYear; year <= endYear; year++) {

            HashMap<String, Object> map = new HashMap<>();
            map.put("name", year);
            boolean flag = false;

            for (Terms.Bucket yearBucket : yearBuckets) {
                if (year == yearBucket.getKeyAsNumber().intValue()) {
                    flag = true;

                    //档案内容类型
                    Terms terms = yearBucket.getAggregations().get("archiveContentType");
                    List<Terms.Bucket> typeBuckets = (List<Terms.Bucket>) terms.getBuckets();
                    for (ContentType contentType : contentTypes) {
                        long typeTotal = 0;
                        for (Terms.Bucket typeBucket : typeBuckets) {

                            if (contentType.getId() == typeBucket.getKeyAsNumber().intValue()) {

                                typeTotal = typeBucket.getDocCount();
                            }
                        }
                        map.put(contentType.getName(), typeTotal);
                    }
                    map.put("合计", yearBucket.getDocCount());
                }
            }
            if (!flag) {
                contentTypes.forEach(a -> {
                    map.put(a.getName(), 0);
                });
                map.put("合计", 0);
            }
            items.add(map);
        }
        List<String> contentTypeNames = new ArrayList<>();
        contentTypes.forEach(a -> contentTypeNames.add(a.getName()));

        contentTypeNames.add("合计");
        result.put("fields", contentTypeNames);
        result.put("items", items);
        return result;

    }

    /*
     * 获取导入Excel模板
     * */
    public XSSFWorkbook excelTemplate(int catalogueId) {
        //根据目录类型确定库结构
        Catalogue catalogue = catalogueRepository.findById(catalogueId).orElse(null);
        XSSFWorkbook wb = new XSSFWorkbook();

        List<DescriptionItem> itemList = descriptionItemRepository.findByCatalogueId(catalogueId);
        List<List<String>> content = new ArrayList<>();
        List<String> title = new ArrayList<>();
        itemList.forEach(a -> title.add(a.getDisplayName()));
        content.add(title);
        if (catalogue.getCatalogueType().equals(CatalogueType.Folder)) {
            //传统立卷
            //通过档案库id和目录类型获得卷内目录
            Optional<Catalogue> folderFileCatalogue = catalogueRepository.findByArchivesIdAndCatalogueType(catalogue.getArchivesId(), CatalogueType.FolderFile);

            if (!folderFileCatalogue.isPresent()) {
                throw new InvalidArgumentException("卷内目录不存在");
            }
            List<DescriptionItem> folderFileItems = descriptionItemRepository.findByCatalogueId(folderFileCatalogue.get().getId());
            List<List<String>> folderFileContent = new ArrayList<>();
            List<String> folderFileTitle = new ArrayList<>();
            folderFileItems.forEach(a -> folderFileTitle.add(a.getDisplayName()));
            folderFileContent.add(folderFileTitle);
            FileHandler.buildXSSFWorkbook("案卷", content, wb);
            FileHandler.buildXSSFWorkbook("卷内", folderFileContent, wb);
        } else {
            //项目
            if (catalogue.getCatalogueType().equals(CatalogueType.File)) {
                FileHandler.buildXSSFWorkbook("一文一件", content, wb);
            } else {
                FileHandler.buildXSSFWorkbook("项目", content, wb);

            }
        }
        return wb;
    }

    //导入Excel文件条目数据
    public XSSFWorkbook importEntry(int catalogueId, File tmpFile, int userId) {
        Catalogue catalogue = catalogueRepository.findById(catalogueId).orElse(null);
        if (catalogue == null) {
            throw new InvalidArgumentException("目录不存在");
        }
        Archives archives = archivesRepository.findById(catalogue.getArchivesId()).orElse(null);
        if (archives == null) {
            throw new InvalidArgumentException("档案库不存在");
        }

        ArchivesGroup archivesGroup = archivesGroupRepository.findById(archives.getArchivesGroupId()).orElse(null);
        if (archivesGroup == null) {
            throw new InvalidArgumentException("档案库分组不存在");
        }
        //读取数据
        List<List<List<String>>> data = FileHandler.xlsxRead(tmpFile);
        //存储错误数据的Excel对象
        XSSFWorkbook wb = null;

        //所有的著录项
        List<DescriptionItem> descriptionItems = descriptionItemRepository.findByCatalogueId(catalogueId);

        List<List<String>> errorData = this.importCatalogueEntry(archivesGroup, archives, catalogue, data.get(0), descriptionItems, userId);


        //传统立卷
        if (catalogue.getCatalogueType().equals(CatalogueType.Folder)) {
            Optional<Catalogue> folderFileCatalogue = catalogueRepository.findByArchivesIdAndCatalogueType(catalogue.getArchivesId(), CatalogueType.FolderFile);
            if (!folderFileCatalogue.isPresent()) {
                throw new InvalidArgumentException("卷内目录不存在");
            }
            List<DescriptionItem> folderFileItems = descriptionItemRepository.findByCatalogueId(folderFileCatalogue.get().getId());

            List<List<String>> folderFileErrorData = this.importCatalogueEntry(archivesGroup, archives, folderFileCatalogue.get(), data.get(1), folderFileItems, userId);
            if (folderFileErrorData.size() != 1 || errorData.size() != 1) {
                FileHandler.buildXSSFWorkbook("案卷", errorData, wb);
                FileHandler.buildXSSFWorkbook("卷内", folderFileErrorData, wb);
            }
        } else {
            //项目
            if (errorData.size() != 1) {
                if (catalogue.getCatalogueType().equals(CatalogueType.File)) {
                    FileHandler.buildXSSFWorkbook("一文一件", errorData, wb);
                } else {
                    FileHandler.buildXSSFWorkbook("项目", errorData, wb);
                }
            }

        }
        return wb;
    }

    public List<List<String>> importCatalogueEntry(ArchivesGroup archivesGroup
            , Archives archives
            , Catalogue catalogue
            , List<List<String>> data
            , List<DescriptionItem> descriptionItems
            , int userId) {

        List<List<String>> errorData = new ArrayList<>();
        //导入文件的title
        List<String> title = data.get(0);

        List<String> errorTitle = title;
        errorTitle.add("错误信息");
        errorData.add(errorTitle);
        //将导入文件title与著录项对应
        List<DescriptionItem> importItems = new ArrayList<>();

        DescriptionItem del = null;
        for (String displayName : title) {
            for (DescriptionItem descriptionItem : descriptionItems) {
                if (descriptionItem.getDisplayName().equals(displayName)) {
                    importItems.add(descriptionItem);
                    del = descriptionItem;
                }
            }
            descriptionItems.remove(del);
        }
        if (importItems.size() != title.size()) {
            data.forEach(row -> row.add("著录项校验失败"));
            errorData.addAll(data);
            return errorData;
        }
        List<Entry> entries = new ArrayList<>();
        //校验并保存数据
        data.remove(0);//去除标题行
        for (List<String> row : data) {
            Entry entry = new Entry();
            entry.setOwner(userId);
            entry.setArchiveId(catalogue.getArchivesId());
            entry.setCatalogueId(catalogue.getId());
            entry.setCatalogueType(catalogue.getCatalogueType());
            entry.setArchiveContentType(archives.getContentTypeId());
            entry.setArchiveType(archives.getType());
            entry.setFondsId(archivesGroup.getFondsId());
            entry.setId(UUID.randomUUID().toString());
            entry.setGmtCreate(new Date());
            entry.setGmtModified(new Date());
            HashMap<String, Object> entryItems = new HashMap<>();
            //遍历每行数据
            for (int i = 0; i < importItems.size(); i++) {
                entryItems.put(importItems.get(i).getMetadataName(), row.get(i));
            }
            entry.setItems(entryItems);
            try {
                this.convertEntryItems(entry, EntryItemConverter::from, true, true);
            } catch (Exception e) {
                row.add(e.getMessage());
                errorData.add(row);
            }
            entries.add(entry);
        }
        entryMongoRepository.saveAll(entries);
        entryAsyncTask.indexAll(entries, catalogue.getId());
        return errorData;
    }
}
