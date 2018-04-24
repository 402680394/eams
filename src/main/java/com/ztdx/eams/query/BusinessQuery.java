package com.ztdx.eams.query;

import com.ztdx.eams.query.jooq.Tables;
import com.ztdx.eams.query.jooq.tables.*;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by li on 2018/4/18.
 */
@Service
public class BusinessQuery {

    private final DSLContext dslContext;

    private BusinessClassification businessClassification = Tables.BUSINESS_CLASSIFICATION;

    private BusinessDictionaryClassification businessDictionaryClassification = Tables.BUSINESS_DICTIONARY_CLASSIFICATION;

    private BusinessDictionary businessDictionary = Tables.BUSINESS_DICTIONARY;

    private SysFonds sysFonds = Tables.SYS_FONDS;

    private BusinessMetadataStandards businessMetadataStandards = Tables.BUSINESS_METADATA_STANDARDS;

    private BusinessMetadata businessMetadata = Tables.BUSINESS_METADATA;

    @Autowired
    public BusinessQuery(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    /**
     * 获取全宗下全部档案分类树形列表.
     */
    public Map<String, Object> getClassificationTreeMap(UInteger fondsId) {
        Map<String, Object> resultMap= new HashMap<>();
        //伪造档案分类根节点，便于递归查询子档案分类
        resultMap.put("id", UInteger.valueOf(0));
        //查询
        resultMap = getSubClassificationTreeMap(getAllClassificationList(fondsId), resultMap);
        //拼装返回数据信息
        resultMap.put("items", resultMap.get("subClassification"));
        //去除根档案分类数据
        resultMap.remove("id");
        resultMap.remove("subClassification");
        return resultMap;
    }

    /**
     * 获取全宗下全部档案分类.
     */
    private List<Map<String, Object>> getAllClassificationList(UInteger fondsId) {
        return dslContext.select(
                businessClassification.ID.as("id"),
                businessClassification.CLASSIFICATION_CODE.as("code"),
                businessClassification.CLASSIFICATION_NAME.as("name"),
                businessClassification.REMARK.as("remark"),
                businessClassification.RETENTION_PERIOD.as("retentionPeriod"),
                businessClassification.PARENT_ID.as("parentId"),
                businessClassification.ORDER_NUMBER.as("orderNumber"))
                .from(businessClassification)
                .where(businessClassification.FONDS_ID.equal(fondsId))
                .orderBy(businessClassification.ORDER_NUMBER)
                .fetch().intoMaps();
    }

    /**
     * 通过根节点递归获取档案分类树形列表
     */
    public Map<String, Object> getSubClassificationTreeMap(List<Map<String, Object>> dataList, Map<String, Object> treeMap) {
        //创建一个空子档案分类列表
        List<Map<String, Object>> subClassificationList = new ArrayList<Map<String, Object>>();
        //遍历档案分类数据，并递归添加子档案分类的下级档案分类
        for (Map<String, Object> map : dataList) {
            if (map.get("parentId").equals(treeMap.get("id"))) {
                map = getSubClassificationTreeMap(dataList, map);
                //将递归添加后的子档案分类放入子档案分类列表
                subClassificationList.add(map);
            }
        }
        //将子档案分类列表加入根节点档案分类信息
        if(!subClassificationList.isEmpty()){
            treeMap.put("subClassification", subClassificationList);
        }
        return treeMap;
    }

    /**
     * 获取档案分类详情.
     */
    public Map<String, Object> getClassification(UInteger id) {
        return dslContext.select(businessClassification.ID.as("id"),
                businessClassification.CLASSIFICATION_CODE.as("code"),
                businessClassification.CLASSIFICATION_NAME.as("name"),
                businessClassification.REMARK.as("remark"),
                businessClassification.RETENTION_PERIOD.as("retentionPeriod"),
                businessClassification.PARENT_ID.as("parentId"))
                .from(businessClassification).where(businessClassification.ID.equal(id)).fetch().intoMaps().get(0);
    }

    /**
     * 获取全宗词典分类树形列表.
     */
    public Map<String, Object> getDictionaryClassificationTreeMap() {
        Map<String, Object> resultMap= new HashMap<String, Object>();
        //伪造全宗根节点，便于递归查询
        resultMap.put("id", UInteger.valueOf(0));
        //查询
        resultMap = getSubDictionaryClassificationTreeMap(getAllFondsList(), getAllDictionaryClassificationList(), resultMap);
        //去除根节点数据
        resultMap.remove("id");
        return resultMap;
    }

    /**
     * 获取全部词典分类.
     */
    private List<Map<String, Object>> getAllDictionaryClassificationList() {
        return dslContext.select(
                businessDictionaryClassification.ID.as("id"),
                businessDictionaryClassification.CLASSIFICATION_CODE.as("code"),
                businessDictionaryClassification.CLASSIFICATION_NAME.as("name"),
                businessDictionaryClassification.REMARK.as("remark"),
                businessDictionaryClassification.FONDS_ID.as("fondsId"))
                .from(businessDictionaryClassification)
                .fetch().intoMaps();
    }

    /**
     * 获取全部全宗.
     */
    private List<Map<String, Object>> getAllFondsList() {
        return dslContext.select(
                sysFonds.ID.as("id"),
                sysFonds.FONDS_CODE.as("code"),
                sysFonds.FONDS_NAME.as("name"),
                sysFonds.PARENT_ID.as("parentId"),
                sysFonds.ORDER_NUMBER.as("orderNumber"),
                sysFonds.FONDS_TYPE.as("type"))
                .from(sysFonds)
                .orderBy(sysFonds.FONDS_TYPE, sysFonds.ORDER_NUMBER)
                .fetch().intoMaps();
    }

    /**
     * 通过根节点递归获取全宗词典分类树形列表.
     */
    public Map<String, Object> getSubDictionaryClassificationTreeMap(List<Map<String, Object>> dataFondsList, List<Map<String, Object>> dataDCList, Map<String, Object> treeMap) {
        //创建一个空的词典分类列表
        List<Map<String, Object>> subDictionaryClassificationList = new ArrayList<>();

        //遍历词典分类数据，并将子词典分类加入词典分类列表
        for (Map<String, Object> map : dataDCList) {
            if (map.get("fondsId").equals(treeMap.get("id"))) {
                subDictionaryClassificationList.add(map);
            }
        }
        //将子词典分类列表加入根节点
        if(!subDictionaryClassificationList.isEmpty()){
            treeMap.put("subDictionaryClassification", subDictionaryClassificationList);

        }

        //创建一个空的子全宗列表
        List<Map<String, Object>> subFondsList = new ArrayList<>();
        //遍历全宗数据，并将全宗加入全宗列表
        for (Map<String, Object> map : dataFondsList) {
            if (map.get("parentId").equals(treeMap.get("id"))) {
                //递归添加子全宗所属词典分类与下级全宗
                map = getSubDictionaryClassificationTreeMap(dataFondsList, dataDCList, map);
                subFondsList.add(map);
            }
        }
        //将子全宗列表加入跟节点
        if(!subFondsList.isEmpty()){
            treeMap.put("subFonds", subFondsList);

        }

        return treeMap;
    }

    /**
     * 获取词典分类详情.
     */
    public Map<String, Object> getDictionaryClassification(UInteger id) {
        return dslContext.select(businessDictionaryClassification.ID.as("id"),
                businessDictionaryClassification.CLASSIFICATION_CODE.as("code"),
                businessDictionaryClassification.CLASSIFICATION_NAME.as("name"),
                businessDictionaryClassification.REMARK.as("remark"))
                .from(businessDictionaryClassification).where(businessDictionaryClassification.ID.equal(id)).fetch().intoMaps().get(0);
    }

    /**
     * 获取全宗所属词典分类列表.
     */
    public Map<String, Object> getDictionaryClassificationListByFonds(UInteger fondsId) {
        Map<String, Object> resultMap= new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(businessDictionaryClassification.ID.as("id"),
                businessDictionaryClassification.CLASSIFICATION_CODE.as("code"),
                businessDictionaryClassification.CLASSIFICATION_NAME.as("name"),
                businessDictionaryClassification.REMARK.as("remark"))
                .from(businessDictionaryClassification).where(businessDictionaryClassification.FONDS_ID.equal(fondsId)).fetch().intoMaps();
        resultMap.put("items", list);
        return resultMap;
    }

    /**
     * 通过词典分类与名称查询词典列表
     */
    public Map<String, Object> getDictionaryList(UInteger dictionaryClassificationId, String name) {
        Map<String, Object> resultMap= new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(businessDictionary.ID.as("id"),
                businessDictionary.DICTIONARY_CODE.as("code"),
                businessDictionary.DICTIONARY_NAME.as("name"),
                businessDictionaryClassification.CLASSIFICATION_NAME.as("classificationName"),
                businessDictionary.BUSINESS_LEVEL.as("businessLevel"),
                businessDictionary.BUSINESS_EXPANSION.as("businessExpansion"),
                businessDictionary.REMARK.as("remark"))
                .from(businessDictionary, businessDictionaryClassification)
                .where(businessDictionary.DICTIONARY_CLASSIFICATION_ID.equal(dictionaryClassificationId)
                        , businessDictionary.DICTIONARY_CLASSIFICATION_ID.equal(businessDictionaryClassification.ID)
                        , businessDictionary.DICTIONARY_NAME.like("%" + name + "%"))
                .fetch().intoMaps();
        resultMap.put("items", list);
        return resultMap;
    }

    /**
     * 获取词典详情.
     */
    public Map<String, Object> getDictionary(UInteger id) {
        return dslContext.select(businessDictionary.ID.as("id"),
                businessDictionary.DICTIONARY_CODE.as("code"),
                businessDictionary.DICTIONARY_NAME.as("name"),
                businessDictionary.BUSINESS_LEVEL.as("businessLevel"),
                businessDictionary.BUSINESS_EXPANSION.as("businessExpansion"),
                businessDictionary.REMARK.as("remark"))
                .from(businessDictionary).where(businessDictionary.ID.equal(id)).fetch().intoMaps().get(0);
    }

    /**
     * 获取元数据规范列表.
     */
    public Map<String, Object> getMetadataStandardsList(String name) {
        Map<String, Object> resultMap= new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(businessMetadataStandards.ID.as("id"),
                businessMetadataStandards.METADATA_STANDARDS_CODE.as("code"),
                businessMetadataStandards.METADATA_STANDARDS_NAME.as("name"),
                businessMetadataStandards.CHARACTER_SET.as("characterSet"),
                businessMetadataStandards.RELEASE_ORGANIZATION.as("releaseOrganization"),
                businessMetadataStandards.DESCRIPTION_FILE.as("descriptionFile"),
                businessMetadataStandards.EDITION.as("edition"),
                businessMetadataStandards.ORDER_NUMBER.as("orderNumber"),
                businessMetadataStandards.FLAG.as("flag"),
                businessMetadataStandards.REMARK.as("remark"))
                .from(businessMetadataStandards)
                .where(businessMetadataStandards.METADATA_STANDARDS_NAME.like("%" + name + "%"))
                .orderBy(businessMetadataStandards.ORDER_NUMBER)
                .fetch().intoMaps();
        resultMap.put("items", list);
        return resultMap;
    }

    /**
     * 获取元数据规范详情.
     */
    public Map<String, Object> getMetadataStandards(UInteger id) {
        return dslContext.select(businessMetadataStandards.ID.as("id"),
                businessMetadataStandards.METADATA_STANDARDS_CODE.as("code"),
                businessMetadataStandards.METADATA_STANDARDS_NAME.as("name"),
                businessMetadataStandards.CHARACTER_SET.as("characterSet"),
                businessMetadataStandards.RELEASE_ORGANIZATION.as("releaseOrganization"),
                businessMetadataStandards.DESCRIPTION_FILE.as("descriptionFile"),
                businessMetadataStandards.EDITION.as("edition"),
                businessMetadataStandards.FLAG.as("flag"),
                businessMetadataStandards.REMARK.as("remark"))
                .from(businessMetadataStandards)
                .where(businessMetadataStandards.ID.equal(id)).fetch().intoMaps().get(0);
    }
    /**
     * 获取元数据列表.
     */
    public Map<String, Object> getMetadataList(UInteger metadataStandardsId, String name) {
        Map<String, Object> resultMap= new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(businessMetadata.ID.as("id"),
                businessMetadata.DISPLAY_NAME.as("displayName"),
                businessMetadata.METADATA_NAME.as("name"),
                businessMetadata.FIELD_PROPERTIES.as("fieldProperties"),
                businessMetadata.DATA_TYPE.as("dataType"),
                businessMetadata.FIELD_WIDTH.as("fieldWidth"),
                businessMetadata.FIELD_PRECISION.as("fieldPrecision"),
                businessMetadata.PARENT_ID.as("parentId"),
                businessMetadata.METADATA_STANDARDS_ID.as("metadataStandardsId"),
                businessMetadata.DEFAULT_VALUE.as("defaultValue"),
                businessMetadata.METADATA_DEFINITION.as("definition"),
                businessMetadata.OBJECTIVE.as("objective"),
                businessMetadata.METADATA_CONSTRAINT.as("constraint"),
                businessMetadata.ELEMENT_TYPE.as("elementType"),
                businessMetadata.CODING_MODIFICATION.as("codingModification"),
                businessMetadata.RELATED_ELEMENTS.as("relatedElements"),
                businessMetadata.METADATA_RANGE.as("range"),
                businessMetadata.INFORMATION_SOURCES.as("informationSources"),
                businessMetadata.ORDER_NUMBER.as("orderNumber"),
                businessMetadata.REMARK.as("remark"))
                .from(businessMetadata)
                .where(businessMetadata.ID.equal(metadataStandardsId), businessMetadata.METADATA_NAME.like("%" + name + "%"))
                .orderBy(businessMetadata.ORDER_NUMBER)
                .fetch().intoMaps();
        resultMap.put("items", list);
        return resultMap;
    }
    /**
     * 获取元数据详情.
     */
    public Map<String,Object> getMetadata(UInteger id) {
        return dslContext.select(businessMetadata.ID.as("id"),
                businessMetadata.DISPLAY_NAME.as("displayName"),
                businessMetadata.METADATA_NAME.as("name"),
                businessMetadata.FIELD_PROPERTIES.as("fieldProperties"),
                businessMetadata.DATA_TYPE.as("dataType"),
                businessMetadata.FIELD_WIDTH.as("fieldWidth"),
                businessMetadata.FIELD_PRECISION.as("fieldPrecision"),
                businessMetadata.PARENT_ID.as("parentId"),
                businessMetadata.METADATA_STANDARDS_ID.as("metadataStandardsId"),
                businessMetadata.DEFAULT_VALUE.as("defaultValue"),
                businessMetadata.METADATA_DEFINITION.as("definition"),
                businessMetadata.OBJECTIVE.as("objective"),
                businessMetadata.METADATA_CONSTRAINT.as("constraint"),
                businessMetadata.ELEMENT_TYPE.as("elementType"),
                businessMetadata.CODING_MODIFICATION.as("codingModification"),
                businessMetadata.RELATED_ELEMENTS.as("relatedElements"),
                businessMetadata.METADATA_RANGE.as("range"),
                businessMetadata.INFORMATION_SOURCES.as("informationSources"),
                businessMetadata.ORDER_NUMBER.as("orderNumber"),
                businessMetadata.REMARK.as("remark"))
                .from(businessMetadata)
                .where(businessMetadata.ID.equal(id)).fetch().intoMaps().get(0);
    }
}
