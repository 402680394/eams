package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.application.task.EntryAsyncTask;
import com.ztdx.eams.domain.archives.model.*;
import com.ztdx.eams.domain.archives.model.entryItem.EntryItemConverter;
import com.ztdx.eams.domain.archives.repository.ArchivesGroupRepository;
import com.ztdx.eams.domain.archives.repository.ArchivesRepository;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import com.ztdx.eams.domain.archives.repository.DescriptionItemRepository;
import com.ztdx.eams.domain.archives.repository.elasticsearch.EntryElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.elasticsearch.OriginalTextElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.mongo.EntryMongoRepository;
import com.ztdx.eams.domain.archives.repository.mongo.IdGeneratorRepository;
import com.ztdx.eams.domain.archives.repository.mongo.IdGeneratorValue;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.join.query.JoinQueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;
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

    public EntryService(EntryElasticsearchRepository entryElasticsearchRepository, EntryMongoRepository entryMongoRepository, DescriptionItemRepository descriptionItemRepository, CatalogueRepository catalogueRepository, ArchivesRepository archivesRepository, ArchivesGroupRepository archivesGroupRepository, ElasticsearchOperations elasticsearchOperations, OriginalTextElasticsearchRepository originalTextElasticsearchRepository, MongoOperations mongoOperations, IdGeneratorRepository idGeneratorRepository, EntryAsyncTask entryAsyncTask) {
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
        this.convertEntryItems(entry, EntryItemConverter::from, true);
        entry = entryMongoRepository.save(entry);

        entryAsyncTask.index(entry);
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
        this.convertEntryItems(entry, EntryItemConverter::from, false);
        update = entryMongoRepository.save(update);

        entryAsyncTask.index(update);
        return update;
    }

    private void initIndex(int catalogueId) {
        try {
            entryElasticsearchRepository.createIndex(this.getIndexName(catalogueId));
        } catch (IOException e) {
            throw new BusinessException("索引初始化失败", e);
        }
    }

    public Page<Entry> search(int catalogueId, String queryString, QueryBuilder itemQuery, Pageable pageable) {
        return search(catalogueId, queryString, itemQuery, null, null, pageable);
    }

    public Page<Entry> search(int catalogueId
            , String queryString
            , QueryBuilder itemQuery
            , String parentId
            , Integer owner
            , Pageable pageable) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (queryString != null && queryString.length() > 0) {
            query.must(queryStringQuery(queryString).defaultOperator(Operator.AND));
        }

        if (parentId != null){
            if (!StringUtils.isEmpty(parentId)) {
                query.filter(termQuery("parentId", parentId));
            }else{
                query.filter(boolQuery().mustNot(existsQuery("parentId")));
            }
        }else{
            //query.must(existsQuery("parentId"));
        }

        if (owner != null){
            query.filter(termQuery("owner", owner));
        }

        if (itemQuery != null) {
            query.filter(itemQuery);
        }

        query.filter(termQuery("gmtDeleted", 0));

        Page<Entry> searchResult = entryElasticsearchRepository.search(
                query, pageable, new String[]{getIndexName(catalogueId)}
        );

        List<String> ids = searchResult.getContent().stream().map(Entry::getId).collect(Collectors.toList());

        List<Entry> result = (List<Entry>)entryMongoRepository.findAllById(ids, getIndexName(catalogueId));

        result.sort(Comparator.comparing(a -> ids.indexOf(a.getId())));

        result.forEach(a -> {
            convertEntryItems(a, EntryItemConverter::format, false);
            Map<String, Object> items = a.getItems();
            items.put("id", a.getId());
        });

        return PageableExecutionUtils.getPage(result , pageable, searchResult::getTotalElements);
    }

    private void convertEntryItems(Entry entry, BiFunction<Object, DescriptionItem, Object> operator, boolean isGenerator) {
        Map<String, DescriptionItem> descriptionItemMap = this.getDescriptionItems(entry.getCatalogueId());
        Map<String, Object> convert = new HashMap<>();

        descriptionItemMap.forEach((key, item) -> {
            Object val;
            if (isGenerator && item.getIsIncrement() == 1 && item.getIncrement() > 1){
                String idKey = String.format(IdGeneratorValue.ENTRY_ITEM_INCREMENT_FORMAT, entry.getCatalogueId(), item.getMetadataName());
                val = idGeneratorRepository.get(idKey, item.getIncrement());
            }else {
                Object value = entry.getItems().getOrDefault(item.getMetadataName(), null);
                val = operator.apply(value, item);
            }
            if (val != null) {
                convert.put(key, val);
            }
        });

        entry.setItems(convert);
    }

    private Map<String, DescriptionItem> getDescriptionItems(int catalogueId) {
        return descriptionItemRepository.findByCatalogueId(catalogueId).stream().collect(Collectors.toMap(DescriptionItem::getMetadataName, (d) -> d, (d1, d2) -> d2));
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
        }else{
            query = QueryBuilders.boolQuery();
        }

        if (keyWord != null && !"".equals(keyWord)){
            query.must(QueryBuilders.queryStringQuery(keyWord).defaultOperator(Operator.AND));
        }

        query.filter(QueryBuilders.termQuery("gmtDeleted", 0));

        srBuilder.setQuery(query).setSize(0);
        Map<Integer, Long> result = new HashMap<>();
        ((Terms)srBuilder.get().getAggregations().asList().get(0)).getBuckets().forEach(
                a -> result.put(a.getKeyAsNumber().intValue(),a.getDocCount()));

        return result;
    }

    public void delete(int catalogueId, Iterable<String> deletes) {
        if(deletes == null || !deletes.iterator().hasNext()){
            return;
        }
        Iterable<Entry> list = entryMongoRepository.findAllById(deletes, getIndexName(catalogueId));
        list.forEach(a -> a.setGmtDeleted(1));
        entryMongoRepository.saveAll(list);
        entryElasticsearchRepository.saveAll(list);
    }

    public Entry get(int catalogueId, String id) {
        return entryMongoRepository.findById(id, getIndexName(catalogueId)).orElse(null);
    }

    private String getIndexName(Integer catalogueId){
        if (catalogueId == null) {
            return INDEX_NAME_PREFIX + "*";
        }else{
            return String.format(INDEX_NAME_PREFIX + "%d", catalogueId);
        }
    }

    public AggregatedPage<OriginalText> searchFulltext(Set<Integer> archiveContentType
            , Set<String> searchParams
            , String includeWords
            , String rejectWords
            , Pageable pageable) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        BoolQueryBuilder fileQuery = QueryBuilders.boolQuery();
        BoolQueryBuilder parentQuery = QueryBuilders.boolQuery();

        if (!StringUtils.isEmpty(includeWords)) {
            if (searchParams.contains(SearchFulltextOption.file.name())) {
                fileQuery.must(queryStringQuery(includeWords).defaultOperator(Operator.AND));
            }
            parentQuery.must(queryStringQuery(includeWords).field(FULL_CONTENT).defaultOperator(Operator.AND));
        }

        if (!StringUtils.isEmpty(rejectWords)) {
            if (searchParams.contains(SearchFulltextOption.file.name())) {
                fileQuery.mustNot(queryStringQuery(rejectWords).defaultOperator(Operator.AND));
            }
            parentQuery.mustNot(queryStringQuery(rejectWords).field(FULL_CONTENT).defaultOperator(Operator.AND));
        }

        if (archiveContentType != null && archiveContentType.size() > 0) {
            parentQuery.filter(QueryBuilders.termsQuery("archiveContentType", archiveContentType));
        }

        parentQuery.filter(termQuery("gmtDeleted", 0));

        if (searchParams.contains(SearchFulltextOption.entry.name())) {
            query.should(JoinQueryBuilders.hasParentQuery(
                    "record",
                    parentQuery,
                    false));
        }

        if (searchParams.contains(SearchFulltextOption.file.name())) {
            query.should(fileQuery);
        }

        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        //builder.withIndices(INDEX_NAME_PREFIX + "*");
        //builder.withTypes("originalText");
        builder.withQuery(query);
        builder.withPageable(pageable);

        HighlightBuilder.Field field = new HighlightBuilder.Field("contentIndex");
        field.requireFieldMatch(false);
        field.fragmentSize(100);
        field.numOfFragments(2);

        builder.withHighlightFields(field);

        return (AggregatedPage<OriginalText>) originalTextElasticsearchRepository.search(builder.build(),new String[] {INDEX_NAME_PREFIX + "*"});
    }

    public Iterable<Entry> findAllById(Set<String> entryIds, Integer catalogueId) {
        if (catalogueId == null) {
            return entryElasticsearchRepository.search(QueryBuilders.termsQuery("id", entryIds), new String[]{getIndexName(null)});
        }else {
            return entryElasticsearchRepository.findAllById(entryIds, getIndexName(catalogueId));
        }
    }

    /**
     * 更改批量鉴定
     */
    public void batchIdentification(List<String> entryIds,Integer catalogueId,Integer isOpen,boolean isExpired,boolean isEndangered,boolean isLose){

        //通过条目id和目录id获得所有的条目集合
        Iterable<Entry> entryList = entryMongoRepository.findAllById(entryIds, "archive_record_" + catalogueId);

        //遍历条目集合给每个条目设置鉴定状态
        for (Entry entry:entryList) {
            entry.setIsOpen(OpenStatus.create(isOpen));
            entry.setExpired(isExpired);
            entry.setEndangered(isEndangered);
            entry.setLose(isLose);
        }

        //存进mongoDb
        entryMongoRepository.saveAll(entryList);

    }

    /**
     * 查看单个鉴定信息
     */
    public Map<String,Object> getIdentification(String entryId,Integer cid){

        Map<String,Object> result = new HashMap<>();
        //通过id获得条目信息
        Optional<Entry> find = entryMongoRepository.findById(entryId,"archive_record_"+cid);
        if (find.isPresent()){

            Entry entry = find.get();
            result.put("isOpen",entry.getIsOpen());
            result.put("isExpired",entry.isExpired());
            result.put("isEndangered",entry.isEndangered());
            result.put("isLose",entry.isLose());

            return result;
        }

        return null;

    }

    /**
     * 组新卷
     * @param folderFileEntryIds 卷内条目id集合
     * @param folderFileCatalogueId 卷内目录ID
     * @param entry 案卷条目
     */
    public void setNewVolume(List<String> folderFileEntryIds, int folderFileCatalogueId, Entry entry){

        //通过条目id和目录id获得所有的条目集合
        Iterable<Entry> entryList = entryMongoRepository.findAllById(folderFileEntryIds, this.getIndexName(folderFileCatalogueId));

        //通过卷内目录id获得目录
        Catalogue folderFileCatalogue = catalogueRepository.findById(folderFileCatalogueId).orElse(null);
        //通过档案库id和目录类型为案卷获得案卷目录
        assert folderFileCatalogue != null;
        Optional<Catalogue> folderCatalogueList = catalogueRepository.findByArchivesIdAndCatalogueType(folderFileCatalogue.getArchivesId(),CatalogueType.Folder);

        if (!folderCatalogueList.isPresent()){
            throw new InvalidArgumentException("卷内目录不存在");
        }

        //把案卷目录id设置到Entry上
        folderCatalogueList.ifPresent(catalogue -> entry.setCatalogueId(catalogue.getId()));

        //新增案卷
        Entry newEntry = save(entry);

        //修改卷内parentID
        for (Entry entryForList:entryList) {
            entryForList.setParentId(newEntry.getId());
        }

        entryMongoRepository.saveAll(entryList);

        entryAsyncTask.indexAll(entryList, folderFileCatalogueId);
    }

    /**
     * 拆卷
     * @param folderFileEntryIds 卷内条目id集合
     * @param folderFileCatalogueId 卷内目录ID
     */
    public void separateVolume(List<String> folderFileEntryIds,int folderFileCatalogueId){

        //获得卷内条目集合
        Iterable<Entry> folderFileEntryList = entryMongoRepository.findAllById(folderFileEntryIds,this.getIndexName(folderFileCatalogueId));

        //置空卷内条目中的上级id
        for (Entry entry:folderFileEntryList) {
            entry.setParentId(null);
        }

        entryMongoRepository.saveAll(folderFileEntryList);

        entryAsyncTask.indexAll(folderFileEntryList, folderFileCatalogueId);
    }



    public Page<Entry> scrollEntry(boolean archivingAll, int catalogueId, Collection<String> srcData, int page, int size){
        Query query;
        if (archivingAll) {
            query = query(where("gmtDeleted")
                    .is(0))
                    .with(PageRequest.of(page, size));
        }else{
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

    public Page<Entry> scrollSubEntry(int catalogueId, Collection<String> parentIds, int page, int size){
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
            }catch (EntryValueConverException e){
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

    public Map<String, String> generatorId(Collection<String> srcIds){
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
            , List<String> trgFields){
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
        for (int i = 0; i < srcFields.size(); i ++){
            String srcField = srcFields.get(i);
            String trgField = trgFields.get(i);
            Object srcValue = entry.getItems().getOrDefault(srcField, null);
            items.put(trgField, srcValue);
        }
        result.setItems(items);

        this.convertEntryItems(result, EntryItemConverter::from, true);

        return result;
    }
}
