package com.ztdx.eams.domain.archives.application.task;

import com.google.common.collect.Iterables;
import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.task.Job;
import com.ztdx.eams.basic.utils.StringUtils;
import com.ztdx.eams.domain.archives.model.*;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import com.ztdx.eams.domain.archives.repository.DescriptionItemRepository;
import com.ztdx.eams.domain.archives.repository.elasticsearch.EntryElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.elasticsearch.OriginalTextElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.mongo.EntryMongoRepository;
import com.ztdx.eams.domain.archives.repository.mongo.OriginalTextMongoRepository;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class EntryAsyncTask {

    private static final String FULL_CONTENT = "full_content";
    private static final String INDEX_NAME_PREFIX = "archive_record_";

    private MongoOperations mongoOperations;

    private ElasticsearchOperations elasticsearchOperations;

    private EntryElasticsearchRepository entryElasticsearchRepository;

    private EntryMongoRepository entryMongoRepository;

    private DescriptionItemRepository descriptionItemRepository;

    private OriginalTextElasticsearchRepository originalTextElasticsearchRepository;

    private OriginalTextMongoRepository originalTextMongoRepository;

    private CatalogueRepository catalogueRepository;

    public EntryAsyncTask(EntryElasticsearchRepository entryElasticsearchRepository, DescriptionItemRepository descriptionItemRepository, MongoOperations mongoOperations, ElasticsearchOperations elasticsearchOperations, OriginalTextElasticsearchRepository originalTextElasticsearchRepository, CatalogueRepository catalogueRepository, EntryMongoRepository entryMongoRepository, OriginalTextMongoRepository originalTextMongoRepository) {
        this.entryElasticsearchRepository = entryElasticsearchRepository;
        this.descriptionItemRepository = descriptionItemRepository;
        this.mongoOperations = mongoOperations;
        this.elasticsearchOperations = elasticsearchOperations;
        this.originalTextElasticsearchRepository = originalTextElasticsearchRepository;
        this.catalogueRepository = catalogueRepository;
        this.entryMongoRepository = entryMongoRepository;
        this.originalTextMongoRepository = originalTextMongoRepository;
    }

    @Job
    public void indexAll(Iterable<Entry> entries, int catalogueId){
        indexAllJob(entries, catalogueId);
    }

    public void indexAllJob(Iterable<Entry> entries, int catalogueId){
        System.out.println("进入到indexAll");
        System.out.println("线程名称：" + Thread.currentThread().getName());
        if (entries == null || !entries.iterator().hasNext()){
            return;
        }
        initIndex(catalogueId);
        entryElasticsearchRepository.saveAll(entries);
        Set<String> ids = StreamSupport.stream(entries.spliterator(), true).map(Entry::getId).collect(Collectors.toSet());
        //采用索引更新时间方案
        mongoOperations.updateFirst(
                query(where("_id").in(ids))
                , new Update().currentDate("indexDate")
                , this.getIndexName(catalogueId));
    }

    public void index(Entry entry){
        System.out.println("线程名称：" + Thread.currentThread().getName());
        initIndex(entry.getCatalogueId());
        entryElasticsearchRepository.save(entry);
        //暂时没有启用版本方案
        /*mongoOperations.updateFirst(
                query(where("_id").is(entry.getId()))
                , Update.update("indexVersion", entry.getVersion())
                , this.getIndexName(entry.getCatalogueId()));*/
        //采用索引更新时间方案
        mongoOperations.updateFirst(
                query(where("_id").is(entry.getId()))
                , new Update().currentDate("indexDate")
                , this.getIndexName(entry.getCatalogueId()));
    }

    @Async
    public void copyItemsFieldToSystemField(Entry entry) {
        List<PropertyType> propertyTypes = Arrays.asList(
                  PropertyType.Rank
                , PropertyType.CarrierType
                , PropertyType.ClassificationNumber
                , PropertyType.department
                , PropertyType.RecordType
                , PropertyType.TimeLimitForStorage
                , PropertyType.Year
        );

        List<DescriptionItem> items = descriptionItemRepository.findByCatalogueId(entry.getCatalogueId());
        Map<PropertyType, String> map = items.stream().collect(Collectors.toMap(DescriptionItem::getPropertyType, DescriptionItem::getMetadataName));
        map.forEach((propertyType, fieldName) -> {

            if (!propertyTypes.contains(propertyType)) {
                return;
            }

            Object value = entry.getItems().getOrDefault(fieldName, null);
            if (value == null){
                return;
            }

            String methodName = String.format("set%s",StringUtils.toUpperCaseFirstOne(fieldName));
            try {
                Method method = Entry.class.getDeclaredMethod(methodName, String.class);
                method.invoke(entry, value.toString());
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
            }

            /*switch (propertyType){
                case Rank:
                    entry.setRank(value.toString());
                    break;
                case CarrierType:
                    entry.setCarrierType(value.toString());
                    break;
                case ClassificationNumber:
                    entry.setClassificationNumber(value.toString());
                    break;
                case department:

                    break;
                case RecordType:
                    break;
                case TimeLimitForStorage:
                    break;
                case Year:
                    break;
                default:
                    break;
            }*/
        });

        entryMongoRepository.save(entry);
        index(entry);
    }

    public void test(String uuid) {
        rebuild();
        //rebuildById(Integer.parseInt(uuid));
    }

    private void rebuildById(int id) {
        try {
            elasticsearchOperations.deleteIndex(this.getIndexName(id));
            originalTextElasticsearchRepository.createIndex(this.getIndexName(id));

            initIndex(id);
            putMapping(id);

            rebuildCatalogueEntry(id);
            rebuildCatalogueOriginalText(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void rebuild(){
        catalogueRepository.findAll().forEach(a -> rebuildById(a.getId()));
    }

    private void rebuildCatalogueEntry(int catalogueId){
        long total = entryMongoRepository.count(getIndexName(catalogueId));
        long pageCount = total / 100 + 1;
        for (long i=0;i < pageCount; i++){
            Page<Entry> list = entryMongoRepository.findAll(PageRequest.of((int)i,100, Sort.by(Sort.Direction.ASC, "gmtCreate")), getIndexName(catalogueId));
            if (list.getContent().size() == 0){
                return;
            }
            entryElasticsearchRepository.saveAll(list);
        }
    }

    private void rebuildCatalogueOriginalText(int catalogueId){
        long total = originalTextMongoRepository.count("archive_record_originalText_" + catalogueId);
        long pageCount = total / 100 + 1;
        for (long i=0;i < pageCount; i++){
            Page<OriginalText> list = originalTextMongoRepository.findAll(
                    PageRequest.of(
                            (int)i,100, Sort.by(Sort.Direction.ASC, "gmtCreate"))
                    , "archive_record_originalText_" + catalogueId);
            if (list.getContent().size() == 0){
                return;
            }

            List<String> ids = list.getContent().stream().map(OriginalText::getEntryId).collect(Collectors.toList());

            Iterable<Entry> list1 = entryMongoRepository.findAllById(ids, "archive_record_" + catalogueId);

            Map<String, Entry> map = StreamSupport.stream(list1.spliterator(), true).collect(Collectors.toMap(Entry::getId, a -> a));

            list.forEach(a -> {
                Entry entry = map.getOrDefault(a.getEntryId(), null);
                if (entry == null){
                    return;
                }
                a.setArchiveContentType(entry.getArchiveContentType());
                a.setFondsId(entry.getFondsId());
            });

            originalTextElasticsearchRepository.saveAll(list);
        }
    }

    private void putMapping(int catalogueId) throws IOException {
        initIndex(catalogueId);

        List<DescriptionItem> list = descriptionItemRepository.findByCatalogueId(catalogueId);
        XContentBuilder contentBuilder;
        contentBuilder = XContentFactory.jsonBuilder().startObject()
                .startObject(getIndexType(Entry.class))
                .startObject("properties");

        contentBuilder
                .startObject("full_content")
                .field("type", FieldType.text.name().toLowerCase())
                .endObject();

        contentBuilder.startObject("items")
                .startObject("properties");
        for (DescriptionItem descriptionItem : list) {
            addSingleFieldMapping(contentBuilder, descriptionItem);
        }

        contentBuilder.endObject().endObject().endObject().endObject().endObject();

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
            if (FieldType.text == fieldType) {
                xContentBuilder.startObject("fields");
                xContentBuilder.startObject("keyword");
                xContentBuilder.field("type", "keyword");
                xContentBuilder.field("ignore_above", 256);
                xContentBuilder.endObject().endObject();
            }
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

    private void initIndex(int catalogueId) {
        try {
            entryElasticsearchRepository.createIndex(this.getIndexName(catalogueId));
        } catch (IOException e) {
            throw new BusinessException("索引初始化失败", e);
        }
    }

    private String getIndexName(Integer catalogueId){
        if (catalogueId == null) {
            return INDEX_NAME_PREFIX + "*";
        }else{
            return String.format(INDEX_NAME_PREFIX + "%d", catalogueId);
        }
    }
}
