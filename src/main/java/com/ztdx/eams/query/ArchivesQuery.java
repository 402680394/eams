package com.ztdx.eams.query;

import com.ztdx.eams.domain.archives.model.CatalogueType;
import com.ztdx.eams.domain.archives.model.Structure;
import com.ztdx.eams.query.jooq.Tables;
import com.ztdx.eams.query.jooq.tables.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by li on 2018/5/3.
 */
@Service
public class ArchivesQuery {

    private final DSLContext dslContext;

    private SysFonds sysFonds = Tables.SYS_FONDS;

    private ArchivesGroup archivesGroup = Tables.ARCHIVES_GROUP;

    private Archives archives = Tables.ARCHIVES;

    private ArchivesCatalogue archivesCatalogue = Tables.ARCHIVES_CATALOGUE;

    private ArchivesContentType archivesContentType = Tables.ARCHIVES_CONTENT_TYPE;

    private ArchivesFileType archivesFileType = Tables.ARCHIVES_FILE_TYPE;

    private ArchivesDescriptionItem archivesDescriptionItem = Tables.ARCHIVES_DESCRIPTION_ITEM;

    private BusinessClassification businessClassification = Tables.BUSINESS_CLASSIFICATION;

    private BusinessDictionaryClassification businessDictionaryClassification = Tables.BUSINESS_DICTIONARY_CLASSIFICATION;

    private BusinessDictionary businessDictionary = Tables.BUSINESS_DICTIONARY;

    private BusinessMetadataStandards businessMetadataStandards = Tables.BUSINESS_METADATA_STANDARDS;

    private BusinessMetadata businessMetadata = Tables.BUSINESS_METADATA;

    private ArchivesArchivalcodeRuler archivesArchivalcodeRuler = Tables.ARCHIVES_ARCHIVALCODE_RULER;

    @Autowired
    public ArchivesQuery(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    /**
     * 通过父档案分类获取档案分类树.
     */
    public Map<String, Object> getClassificationTreeMapByParent(UInteger parentId) {
        Map<String, Object> resultMap = new HashMap<>();
        //伪造档案分类根节点，便于递归查询子档案分类
        resultMap.put("id", parentId);
        //查询
        resultMap = getSubClassificationTreeMap(getAllClassificationList(), resultMap);
        //拼装返回数据信息
        if (null != resultMap.get("children")) {
            resultMap.put("items", resultMap.get("children"));
        } else {
            resultMap.put("items", new ArrayList<Map<String, Object>>());
        }
        //去除根档案分类数据
        resultMap.remove("id");
        resultMap.remove("children");
        return resultMap;
    }

    /**
     * 获取所属全宗的档案分类树.
     */
    public Map<String, Object> getClassificationTreeMap(UInteger fondsId) {
        Map<String, Object> resultMap = new HashMap<>();
        //伪造档案分类根节点，便于递归查询子档案分类
        resultMap.put("id", UInteger.valueOf(1));
        //查询
        resultMap = getSubClassificationTreeMap(getClassificationsByFondsId(fondsId), resultMap);
        //拼装返回数据信息
        if (null != resultMap.get("children")) {
            resultMap.put("items", resultMap.get("children"));
        } else {
            resultMap.put("items", new ArrayList<Map<String, Object>>());
        }
        //去除根档案分类数据
        resultMap.remove("id");
        resultMap.remove("children");
        return resultMap;
    }

    /**
     * 通过上级节点递归获取档案分类树形列表
     */
    public Map<String, Object> getSubClassificationTreeMap(List<Map<String, Object>> dataList, Map<String, Object> treeMap) {
        //创建一个空子档案分类列表
        List<Map<String, Object>> subClassificationList = new ArrayList<>();
        //遍历档案分类数据，并递归添加子档案分类的下级档案分类
        for (Map<String, Object> map : dataList) {
            if (treeMap.get("id").equals(map.get("parentId"))) {
                map = getSubClassificationTreeMap(dataList, map);
                //将递归添加后的子档案分类放入子档案分类列表
                subClassificationList.add(map);
            }
        }
        //将子档案分类列表加入根节点档案分类信息
        if (!subClassificationList.isEmpty()) {
            treeMap.put("children", subClassificationList);
        }
        return treeMap;
    }

    /**
     * 获取全宗下全部档案分类.
     */
    private List<Map<String, Object>> getClassificationsByFondsId(UInteger fondsId) {
        return dslContext.select(
                businessClassification.ID.as("id"),
                businessClassification.CLASSIFICATION_CODE.as("code"),
                businessClassification.CLASSIFICATION_NAME.as("name"),
                DSL.decode().when(businessClassification.REMARK.isNull(), "").otherwise(businessClassification.REMARK).as("remark"),
                businessClassification.RETENTION_PERIOD.as("retentionPeriod"),
                businessClassification.PARENT_ID.as("parentId"),
                businessClassification.ORDER_NUMBER.as("orderNumber"))
                .from(businessClassification)
                .where(businessClassification.FONDS_ID.equal(fondsId))
                .orderBy(businessClassification.ORDER_NUMBER)
                .fetch().intoMaps();
    }

    /**
     * 获取全部档案分类.
     */
    private List<Map<String, Object>> getAllClassificationList() {
        return dslContext.select(
                businessClassification.ID.as("id"),
                businessClassification.CLASSIFICATION_CODE.as("code"),
                businessClassification.CLASSIFICATION_NAME.as("name"),
                businessClassification.PARENT_ID.as("parentId"),
                businessClassification.FONDS_ID.as("fondsId"),
                businessClassification.ORDER_NUMBER.as("orderNumber"))
                .from(businessClassification)
                .orderBy(businessClassification.ORDER_NUMBER)
                .fetch().intoMaps();
    }

    /**
     * 获取全宗、档案分类树.
     */
    public Map<String, Object> getFondsAndClassificationTreeMap(Function<Integer, Boolean> hasFondsPermission) {
        Map<String, Object> resultMap = new HashMap<>();
        //伪造全宗根节点，便于递归查询
        resultMap.put("id", UInteger.valueOf(1));
        //查询
        resultMap = getSubFondsAndClassificationTreeMap(getAllFondsList(), getAllClassificationList(), hasFondsPermission, resultMap);

        if (null != resultMap.get("children")) {
            resultMap.put("items", resultMap.get("children"));
        } else {
            resultMap.put("items", new ArrayList<Map<String, Object>>());
        }
        //去除根节点数据
        resultMap.remove("id");
        resultMap.remove("children");
        return resultMap;
    }

    /**
     * 通过全宗上级节点递归获取全宗、档案分类树.
     */
    public Map<String, Object> getSubFondsAndClassificationTreeMap(List<Map<String, Object>> dataFondsList, List<Map<String, Object>> dataClassificationList, Function<Integer, Boolean> hasFondsPermission, Map<String, Object> treeMap) {
        //创建一个空的子列表
        List<Map<String, Object>> childrenList = new ArrayList<>();

        //遍历档案分类数据，并将子档案分类加入子列表
        for (Map<String, Object> map : dataClassificationList) {
            if (treeMap.get("id").equals(map.get("fondsId")) && 1 == ((UInteger) map.get("parentId")).intValue()) {
                Map<String, Object> childrenMap = map;
                childrenMap.put("childrenType", "Classification");
                childrenMap = getSubClassificationTreeMap(dataClassificationList, childrenMap);
                childrenList.add(childrenMap);
            }
        }

        //遍历全宗数据，并将子全宗加入子列表
        for (Map<String, Object> map : dataFondsList) {
            if (treeMap.get("id").equals(map.get("parentId"))) {
                Map<String, Object> childrenMap = map;
                childrenMap.put("childrenType", "Fonds");
                //递归添加子全宗所属档案分类与下级全宗
                childrenMap = getSubFondsAndClassificationTreeMap(dataFondsList, dataClassificationList, hasFondsPermission, childrenMap);
                childrenList.add(childrenMap);
            }
        }
        int fondsId = ((UInteger) treeMap.get("id")).intValue();
        if (!hasFondsPermission.apply(fondsId)) {
            return null;
        }
        if (!childrenList.isEmpty()) {
            treeMap.put("children", childrenList);
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
                DSL.decode().when(businessClassification.REMARK.isNull(), "").otherwise(businessClassification.REMARK).as("remark"),
                businessClassification.RETENTION_PERIOD.as("retentionPeriod"),
                businessClassification.PARENT_ID.as("parentId"))
                .from(businessClassification).where(businessClassification.ID.equal(id)).fetch().intoMaps().get(0);
    }

    /**
     * 获取全部词典分类.
     */
    private List<Map<String, Object>> getAllDictionaryClassificationList() {
        return dslContext.select(
                businessDictionaryClassification.ID.as("id"),
                businessDictionaryClassification.CLASSIFICATION_CODE.as("code"),
                businessDictionaryClassification.CLASSIFICATION_NAME.as("name"),
                DSL.decode().when(businessDictionaryClassification.REMARK.isNull(), "").otherwise(businessDictionaryClassification.REMARK).as("remark"),
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
                DSL.decode().when(sysFonds.REMARK.isNull(), "").otherwise(sysFonds.REMARK).as("remark"))
                .from(sysFonds)
                .where(sysFonds.GMT_DELETED.equal(0))
                .orderBy(sysFonds.ORDER_NUMBER)
                .fetch().intoMaps();
    }

    /**
     * 获取全宗、词典分类树.
     */
    public Map<String, Object> getDictionaryClassificationTreeMap(Function<Integer, Boolean> hasFondsPermission) {
        Map<String, Object> resultMap = new HashMap<>();
        //伪造全宗根节点，便于递归查询
        resultMap.put("id", UInteger.valueOf(1));
        //查询
        resultMap = getSubDictionaryClassificationTreeMap(getAllFondsList(), getAllDictionaryClassificationList(), hasFondsPermission, resultMap);

        if (null != resultMap.get("children")) {
            resultMap.put("items", resultMap.get("children"));
        } else {
            resultMap.put("items", new ArrayList<Map<String, Object>>());
        }
        //去除根节点数据
        resultMap.remove("id");
        resultMap.remove("children");
        return resultMap;
    }

    /**
     * 通过全宗上级节点递归获取全宗、词典分类树.
     */
    public Map<String, Object> getSubDictionaryClassificationTreeMap(List<Map<String, Object>> dataFondsList, List<Map<String, Object>> dataDCList, Function<Integer, Boolean> hasFondsPermission, Map<String, Object> treeMap) {
        //创建一个空的子列表
        List<Map<String, Object>> childrenList = new ArrayList<>();

        //遍历词典分类数据
        for (Map<String, Object> map : dataDCList) {
            if (map.get("fondsId").equals(treeMap.get("id"))) {
                Map<String, Object> childrenMap = map;
                childrenMap.put("childrenType", "DictionaryClassification");
                childrenList.add(childrenMap);
            }
        }

        //遍历全宗数据，并将子全宗加入子列表
        for (Map<String, Object> map : dataFondsList) {
            if (treeMap.get("id").equals(map.get("parentId"))) {
                Map<String, Object> childrenMap = map;
                childrenMap.put("childrenType", "Fonds");
                //递归添加子全宗所属词典分类与下级全宗
                childrenMap = getSubDictionaryClassificationTreeMap(dataFondsList, dataDCList, hasFondsPermission, childrenMap);
                childrenList.add(childrenMap);
            }
        }
        int fondsId = ((UInteger) treeMap.get("id")).intValue();
        if (!hasFondsPermission.apply(fondsId)) {
            return null;
        }
        if (!childrenList.isEmpty()) {
            treeMap.put("children", childrenList);
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
                DSL.decode().when(businessDictionaryClassification.REMARK.isNull(), "").otherwise(businessDictionaryClassification.REMARK).as("remark"))
                .from(businessDictionaryClassification).where(businessDictionaryClassification.ID.equal(id)).fetch().intoMaps().get(0);
    }

    /**
     * 获取全宗所属词典分类列表.
     */
    public Map<String, Object> getDictionaryClassificationListByFonds(UInteger fondsId) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(businessDictionaryClassification.ID.as("id"),
                businessDictionaryClassification.CLASSIFICATION_CODE.as("code"),
                businessDictionaryClassification.CLASSIFICATION_NAME.as("name"),
                DSL.decode().when(businessDictionaryClassification.REMARK.isNull(), "").otherwise(businessDictionaryClassification.REMARK).as("remark"))
                .from(businessDictionaryClassification).where(businessDictionaryClassification.FONDS_ID.equal(fondsId)).fetch().intoMaps();
        resultMap.put("items", list);
        return resultMap;
    }

    /**
     * 通过词典分类与名称查询词典列表
     */
    public Map<String, Object> getDictionaryList(UInteger dictionaryClassificationId, String name) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(businessDictionary.ID.as("id"),
                businessDictionary.DICTIONARY_CODE.as("code"),
                businessDictionary.DICTIONARY_NAME.as("name"),
                businessDictionaryClassification.CLASSIFICATION_NAME.as("classificationName"),
                businessDictionary.BUSINESS_LEVEL.as("businessLevel"),
                DSL.decode().when(businessDictionary.BUSINESS_EXPANSION.isNull(), "").otherwise(businessDictionary.BUSINESS_EXPANSION).as("businessExpansion"),
                DSL.decode().when(businessDictionary.REMARK.isNull(), "").otherwise(businessDictionary.REMARK).as("remark"))
                .from(businessDictionary, businessDictionaryClassification)
                .where(businessDictionary.DICTIONARY_CLASSIFICATION_ID.equal(dictionaryClassificationId)
                        , businessDictionary.DICTIONARY_CLASSIFICATION_ID.equal(businessDictionaryClassification.ID)
                        , businessDictionary.DICTIONARY_NAME.like("%" + name + "%"))
                .fetch().intoMaps();
        resultMap.put("items", list);
        return resultMap;
    }

    /**
     * 通过词典分类ID查询词典下拉列表
     */
    public Map<String, Object> getDictionarySelectList(UInteger dictionaryClassificationId) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(businessDictionary.ID.as("id"),
                businessDictionary.DICTIONARY_CODE.as("code"),
                businessDictionary.DICTIONARY_NAME.as("name"))
                .from(businessDictionary)
                .where(businessDictionary.DICTIONARY_CLASSIFICATION_ID.equal(dictionaryClassificationId))
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
                DSL.decode().when(businessDictionary.BUSINESS_EXPANSION.isNull(), "").otherwise(businessDictionary.BUSINESS_EXPANSION).as("businessExpansion"),
                DSL.decode().when(businessDictionary.REMARK.isNull(), "").otherwise(businessDictionary.REMARK).as("remark"))
                .from(businessDictionary).where(businessDictionary.ID.equal(id)).fetch().intoMaps().get(0);
    }

    /**
     * 获取元数据规范列表.
     */
    public Map<String, Object> getMetadataStandardsList(String name) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(businessMetadataStandards.ID.as("id"),
                businessMetadataStandards.METADATA_STANDARDS_CODE.as("code"),
                businessMetadataStandards.METADATA_STANDARDS_NAME.as("name"),
                businessMetadataStandards.CHARACTER_SET.as("characterSet"),
                DSL.decode().when(businessMetadataStandards.RELEASE_ORGANIZATION.isNull(), "").otherwise(businessMetadataStandards.RELEASE_ORGANIZATION).as("releaseOrganization"),
                DSL.decode().when(businessMetadataStandards.DESCRIPTION_FILE.isNull(), "").otherwise(businessMetadataStandards.DESCRIPTION_FILE).as("descriptionFile"),
                businessMetadataStandards.EDITION.as("edition"),
                businessMetadataStandards.ORDER_NUMBER.as("orderNumber"),
                businessMetadataStandards.FLAG.as("flag"),
                DSL.decode().when(businessMetadataStandards.REMARK.isNull(), "").otherwise(businessMetadataStandards.REMARK).as("remark"))
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
                DSL.decode().when(businessMetadataStandards.RELEASE_ORGANIZATION.isNull(), "").otherwise(businessMetadataStandards.RELEASE_ORGANIZATION).as("releaseOrganization"),
                DSL.decode().when(businessMetadataStandards.DESCRIPTION_FILE.isNull(), "").otherwise(businessMetadataStandards.DESCRIPTION_FILE).as("descriptionFile"),
                businessMetadataStandards.EDITION.as("edition"),
                businessMetadataStandards.FLAG.as("flag"),
                DSL.decode().when(businessMetadataStandards.REMARK.isNull(), "").otherwise(businessMetadataStandards.REMARK).as("remark"))
                .from(businessMetadataStandards)
                .where(businessMetadataStandards.ID.equal(id)).fetch().intoMaps().get(0);
    }

    /**
     * 获取元数据列表.
     */
    public Map<String, Object> getMetadataList(UInteger metadataStandardsId, String name, int pageNum, int pageSize) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(businessMetadata.ID.as("id"),
                businessMetadata.DISPLAY_NAME.as("displayName"),
                businessMetadata.METADATA_NAME.as("name"),
                businessMetadata.FIELD_PROPERTIES.as("fieldProperties"),
                businessMetadata.DATA_TYPE.as("dataType"),
                businessMetadata.FIELD_WIDTH.as("fieldWidth"),
                businessMetadata.FIELD_PRECISION.as("fieldPrecision"),
                DSL.decode().when(businessMetadata.FIELD_FORMAT.isNull(), "").otherwise(businessMetadata.FIELD_FORMAT).as("fieldFormat"),
                businessMetadata.METADATA_STANDARDS_ID.as("metadataStandardsId"),
                businessMetadata.DEFAULT_VALUE.as("defaultValue"),
                DSL.decode().when(businessMetadata.METADATA_DEFINITION.isNull(), "").otherwise(businessMetadata.METADATA_DEFINITION).as("definition"),
                DSL.decode().when(businessMetadata.OBJECTIVE.isNull(), "").otherwise(businessMetadata.OBJECTIVE).as("objective"),
                businessMetadata.METADATA_CONSTRAINT.as("constraint"),
                businessMetadata.ELEMENT_TYPE.as("elementType"),
                DSL.decode().when(businessMetadata.CODING_MODIFICATION.isNull(), "").otherwise(businessMetadata.CODING_MODIFICATION).as("codingModification"),
                DSL.decode().when(businessMetadata.RELATED_ELEMENTS.isNull(), "").otherwise(businessMetadata.RELATED_ELEMENTS).as("relatedElements"),
                DSL.decode().when(businessMetadata.METADATA_RANGE.isNull(), "").otherwise(businessMetadata.METADATA_RANGE).as("range"),
                DSL.decode().when(businessMetadata.INFORMATION_SOURCES.isNull(), "").otherwise(businessMetadata.INFORMATION_SOURCES).as("informationSources"),
                businessMetadata.ORDER_NUMBER.as("orderNumber"),
                DSL.decode().when(businessMetadata.REMARK.isNull(), "").otherwise(businessMetadata.REMARK).as("remark"))
                .from(businessMetadata)
                .where(businessMetadata.METADATA_STANDARDS_ID.equal(metadataStandardsId), businessMetadata.METADATA_NAME.like("%" + name.trim() + "%"))
                .orderBy(businessMetadata.ORDER_NUMBER)
                .limit((pageNum - 1) * pageSize, pageSize)
                .fetch().intoMaps();
        resultMap.put("items", list);
        return resultMap;
    }

    /**
     * 获取元数据详情.
     */
    public Map<String, Object> getMetadata(UInteger id) {
        return dslContext.select(businessMetadata.ID.as("id"),
                businessMetadata.DISPLAY_NAME.as("displayName"),
                businessMetadata.METADATA_NAME.as("name"),
                businessMetadata.FIELD_PROPERTIES.as("fieldProperties"),
                businessMetadata.DATA_TYPE.as("dataType"),
                businessMetadata.FIELD_WIDTH.as("fieldWidth"),
                businessMetadata.FIELD_PRECISION.as("fieldPrecision"),
                DSL.decode().when(businessMetadata.FIELD_FORMAT.isNull(), "").otherwise(businessMetadata.FIELD_FORMAT).as("fieldFormat"),
                businessMetadata.METADATA_STANDARDS_ID.as("metadataStandardsId"),
                businessMetadata.DEFAULT_VALUE.as("defaultValue"),
                DSL.decode().when(businessMetadata.METADATA_DEFINITION.isNull(), "").otherwise(businessMetadata.METADATA_DEFINITION).as("definition"),
                DSL.decode().when(businessMetadata.OBJECTIVE.isNull(), "").otherwise(businessMetadata.OBJECTIVE).as("objective"),
                businessMetadata.METADATA_CONSTRAINT.as("constraint"),
                businessMetadata.ELEMENT_TYPE.as("elementType"),
                DSL.decode().when(businessMetadata.CODING_MODIFICATION.isNull(), "").otherwise(businessMetadata.CODING_MODIFICATION).as("codingModification"),
                DSL.decode().when(businessMetadata.RELATED_ELEMENTS.isNull(), "").otherwise(businessMetadata.RELATED_ELEMENTS).as("relatedElements"),
                DSL.decode().when(businessMetadata.METADATA_RANGE.isNull(), "").otherwise(businessMetadata.METADATA_RANGE).as("range"),
                DSL.decode().when(businessMetadata.INFORMATION_SOURCES.isNull(), "").otherwise(businessMetadata.INFORMATION_SOURCES).as("informationSources"),
                businessMetadata.ORDER_NUMBER.as("orderNumber"),
                DSL.decode().when(businessMetadata.REMARK.isNull(), "").otherwise(businessMetadata.REMARK).as("remark"))
                .from(businessMetadata)
                .where(businessMetadata.ID.equal(id)).fetch().intoMaps().get(0);
    }

    /**
     * 获取库分组、库树
     */
    public Map<String, Object> getArchivesGroupToArchivesTreeMap(
            int fondsId
            , Byte archiveType
            , Function<Integer, Boolean> hasCataloguePermission
    ) {
        Map<String, Object> resultMap = new HashMap<>();
        //伪造全宗根节点，便于递归查询
        resultMap.put("id", UInteger.valueOf(1));
        resultMap.put("fondsId", UInteger.valueOf(fondsId));
        //查询
        resultMap = getSubArchivesGroupToArchivesTreeMap(
                getAllArchivesGroupList()
                , getAllArchivesList(archiveType)
                , getAllCatalogueList()
                , resultMap
                , hasCataloguePermission
        );
        //拼装返回数据信息
        if (null != resultMap && null != resultMap.get("children")) {
            resultMap.put("items", resultMap.get("children"));
        } else {
            resultMap = new HashMap<>();
            resultMap.put("items", new ArrayList<Map<String, Object>>());
            return resultMap;
        }
        //去除根节点数据
        resultMap.remove("id");
        resultMap.remove("fondsId");
        resultMap.remove("children");
        return resultMap;
    }

    /**
     * 通过上级节点递归获取库分组、库树.
     */
    private Map<String, Object> getSubArchivesGroupToArchivesTreeMap(
            List<Map<String, Object>> dataArchivesGroupList
            , List<Map<String, Object>> dataArchivesList
            , List<Map<String, Object>> dataCatalogueList
            , Map<String, Object> treeMap
            , Function<Integer, Boolean> hasCataloguePermission
    ) {

        //创建一个空的下级库节点列表
        List<Map<String, Object>> childrenList = new ArrayList<>();
        //遍历库数据，获取下级库节点
        for (Map<String, Object> map : dataArchivesList) {
            if (treeMap.get("id").equals(map.get("archivesGroupId"))) {
                //递归获取下级节点
                Map<String, Object> childrenMap = map;
                childrenMap.put("childrenType", "Archives");
                childrenMap = getSubCatalogueTreeMap(
                        dataCatalogueList
                        , childrenMap
                        , hasCataloguePermission
                );
                Object childrenMapChildren = childrenMap.getOrDefault("children", null);
                if (!(childrenMapChildren instanceof List)
                        || ((List) childrenMapChildren).size() == 0) {
                    continue;
                }
                childrenMap.remove("children");
                childrenList.add(childrenMap);
            }
        }

        //遍历库分组数据，并递归获取下级节点
        for (Map<String, Object> map : dataArchivesGroupList) {
            if (treeMap.get("fondsId").equals(map.get("fondsId")) && treeMap.get("id").equals(map.get("parentId"))) {
                Map<String, Object> childrenMap = map;
                childrenMap.put("childrenType", "ArchivesGroup");
                childrenMap = getSubArchivesGroupToArchivesTreeMap(
                        dataArchivesGroupList
                        , dataArchivesList
                        , dataCatalogueList
                        , childrenMap
                        , hasCataloguePermission
                );
                childrenList.add(childrenMap);
            }
        }
        //将递归获取的下属库分组树加入上级节点
        if (!childrenList.isEmpty()) {
            treeMap.put("children", childrenList);
        }
        return treeMap;
    }

    /**
     * 获取全宗、库分组、库树.
     */
    public Map<String, Object> getFondsToArchivesTreeMap(
            Byte archiveType
            , Function<Integer, Boolean> hasFondsPermission
            , Function<Integer, Boolean> hasCataloguePermission
    ) {
        Map<String, Object> resultMap = new HashMap<>();
        //伪造全宗根节点，便于递归查询
        resultMap.put("id", UInteger.valueOf(1));
        //查询
        resultMap = getSubFondsToArchivesTreeMap(
                getAllFondsList()
                , getAllArchivesGroupList()
                , getAllArchivesList(archiveType)
                , getAllCatalogueList()
                , resultMap
                , hasFondsPermission
                , hasCataloguePermission
        );
        //拼装返回数据信息
        if (null != resultMap && null != resultMap.get("children")) {
            resultMap.put("items", resultMap.get("children"));
        } else {
            resultMap = new HashMap<>();
            resultMap.put("items", new ArrayList<Map<String, Object>>());
            return resultMap;
        }
        //去除根节点数据
        resultMap.remove("id");
        resultMap.remove("children");
        return resultMap;
    }

    /**
     * 通过全宗上级节点递归获取全宗、库分组、库树.
     */
    private Map<String, Object> getSubFondsToArchivesTreeMap(
            List<Map<String, Object>> dataFondsList
            , List<Map<String, Object>> dataArchivesGroupList
            , List<Map<String, Object>> dataArchivesList
            , List<Map<String, Object>> dataCatalogueList
            , Map<String, Object> treeMap
            , Function<Integer, Boolean> hasFondsPermission
            , Function<Integer, Boolean> hasCataloguePermission
    ) {

        //创建一个空的子列表
        List<Map<String, Object>> childrenList = new ArrayList<>();

        Map<String, Object> archivesGroupTreeMap = new HashMap<>();
        //伪造库分组根节点，便于递归查询
        archivesGroupTreeMap.put("id", UInteger.valueOf(1));
        archivesGroupTreeMap.put("fondsId", treeMap.get("id"));
        //递归查询库分组、登记库、目录树
        archivesGroupTreeMap = getSubArchivesGroupToArchivesTreeMap(
                dataArchivesGroupList
                , dataArchivesList
                , dataCatalogueList
                , archivesGroupTreeMap
                , hasCataloguePermission
        );
        //添加查询后的下级库分组节点数据到本节点
        if (null != archivesGroupTreeMap.get("children")) {
            childrenList = (List) archivesGroupTreeMap.get("children");
        }

        //遍历全宗数据，获取下级全宗节点
        for (Map<String, Object> map : dataFondsList) {
            if (treeMap.get("id").equals(map.get("parentId"))) {
                //递归获取下级节点
                Map<String, Object> childrenMap = map;
                childrenMap.put("childrenType", "Fonds");
                childrenMap = getSubFondsToArchivesTreeMap(
                        dataFondsList
                        , dataArchivesGroupList
                        , dataArchivesList
                        , dataCatalogueList
                        , childrenMap
                        , hasFondsPermission
                        , hasCataloguePermission
                );
                if (null != childrenMap) {
                    childrenList.add(childrenMap);
                }

            }
        }
        //添加查询后的下级全宗节点数据到本节点
        if (!childrenList.isEmpty()) {
            boolean hasChildFonds = childrenList.stream().anyMatch(
                    a -> "Fonds".equals(a.getOrDefault("childrenType", null))
            );
            if (hasChildFonds) {
                treeMap.put("children", childrenList);
            } else {
                int fondsId = ((UInteger) treeMap.get("id")).intValue();
                if (!hasFondsPermission.apply(fondsId)) {
                    return null;
                }
                treeMap.put("children", childrenList);
            }
        } else {
            int fondsId = ((UInteger) treeMap.get("id")).intValue();
            if (!hasFondsPermission.apply(fondsId)) {
                return null;
            }
        }
        return treeMap;
    }


    /**
     * 获取全宗、库分组、库、目录树.
     */
    public Map<String, Object> getFondsToCatalogueTreeMap(
            Byte archiveType
            , Function<Integer, Boolean> hasFondsPermission
            , Function<Integer, Boolean> hasCataloguePermission
    ) {
        Map<String, Object> resultMap = new HashMap<>();
        //伪造全宗根节点，便于递归查询
        resultMap.put("id", UInteger.valueOf(1));
        //查询
        resultMap = getSubFondsToCatalogueTreeMap(
                getAllFondsList()
                , getAllArchivesGroupList()
                , getAllArchivesList(archiveType)
                , getAllCatalogueList()
                , resultMap
                , hasFondsPermission
                , hasCataloguePermission
        );

        //拼装返回数据信息
        if (null != resultMap && null != resultMap.get("children")) {
            resultMap.put("items", resultMap.get("children"));
        } else {
            resultMap = new HashMap<>();
            resultMap.put("items", new ArrayList<Map<String, Object>>());
            return resultMap;
        }
        //去除根节点数据
        resultMap.remove("id");
        resultMap.remove("children");
        return resultMap;
    }

    /**
     * 通过全宗上级节点递归获取全宗、库分组、库、目录树.
     */
    private Map<String, Object> getSubFondsToCatalogueTreeMap(
            List<Map<String, Object>> dataFondsList
            , List<Map<String, Object>> dataArchivesGroupList
            , List<Map<String, Object>> dataArchivesList
            , List<Map<String, Object>> dataCatalogueList
            , Map<String, Object> treeMap
            , Function<Integer, Boolean> hasFondsPermission
            , Function<Integer, Boolean> hasCataloguePermission
    ) {

        //创建一个空的子列表
        List<Map<String, Object>> childrenList = new ArrayList<>();

        Map<String, Object> archivesGroupTreeMap = new HashMap<>();
        //伪造库分组根节点，便于递归查询
        archivesGroupTreeMap.put("id", UInteger.valueOf(1));
        archivesGroupTreeMap.put("fondsId", treeMap.get("id"));
        //递归查询库分组、登记库、目录树
        archivesGroupTreeMap = getSubArchivesGroupToCatalogueTreeMap(
                dataArchivesGroupList
                , dataArchivesList
                , dataCatalogueList
                , archivesGroupTreeMap
                , hasCataloguePermission
        );
        //添加查询后的下级库分组节点数据到本节点
        if (null != archivesGroupTreeMap.get("children")) {
            childrenList = (List) archivesGroupTreeMap.get("children");
        }

        //遍历全宗数据，获取下级全宗节点
        for (Map<String, Object> map : dataFondsList) {
            if (treeMap.get("id").equals(map.get("parentId"))) {
                //递归获取下级节点
                Map<String, Object> childrenMap = map;
                childrenMap.put("childrenType", "Fonds");
                childrenMap = getSubFondsToCatalogueTreeMap(
                        dataFondsList
                        , dataArchivesGroupList
                        , dataArchivesList
                        , dataCatalogueList
                        , childrenMap
                        , hasFondsPermission
                        , hasCataloguePermission
                );
                if (null != childrenMap) {
                    childrenList.add(childrenMap);
                }

            }
        }
        //添加查询后的下级全宗节点数据到本节点
        if (!childrenList.isEmpty()) {
            boolean hasChildFonds = childrenList.stream().anyMatch(
                    a -> "Fonds".equals(a.getOrDefault("childrenType", null))
            );
            if (hasChildFonds) {
                treeMap.put("children", childrenList);
            } else {
                int fondsId = ((UInteger) treeMap.get("id")).intValue();
                if (!hasFondsPermission.apply(fondsId)) {
                    return null;
                }
                treeMap.put("children", childrenList);
            }
        } else {
            int fondsId = ((UInteger) treeMap.get("id")).intValue();
            if (!hasFondsPermission.apply(fondsId)) {
                return null;
            }
        }
        return treeMap;
    }

    /**
     * 通过上级节点递归获取库分组、库、目录树.
     */
    private Map<String, Object> getSubArchivesGroupToCatalogueTreeMap(
            List<Map<String, Object>> dataArchivesGroupList
            , List<Map<String, Object>> dataArchivesList
            , List<Map<String, Object>> dataCatalogueList
            , Map<String, Object> treeMap
            , Function<Integer, Boolean> hasCataloguePermission
    ) {

        //创建一个空的下级库节点列表
        List<Map<String, Object>> childrenList = new ArrayList<>();
        //遍历库数据，获取下级库节点
        for (Map<String, Object> map : dataArchivesList) {
            if (treeMap.get("id").equals(map.get("archivesGroupId"))) {
                //递归获取下级节点
                Map<String, Object> childrenMap = map;
                childrenMap.put("childrenType", "Archives");
                childrenMap = getSubCatalogueTreeMap(
                        dataCatalogueList
                        , childrenMap
                        , hasCataloguePermission
                );
                Object childrenMapChildren = childrenMap.getOrDefault("children", null);
                if (!(childrenMapChildren instanceof List)
                        || ((List) childrenMapChildren).size() == 0) {
                    continue;
                }
                CatalogueType catalogueType = null;
                switch (Structure.create((byte) childrenMap.get("structure"))) {
                    case ArticleOne: {
                        catalogueType = CatalogueType.File;
                        break;
                    }
                    case TraditionalArchives: {
                        catalogueType = CatalogueType.Folder;
                        break;
                    }
                    case Project: {
                        catalogueType = CatalogueType.Subject;
                        break;
                    }
                }
                for (Map<String, Object> m : (List<Map<String, Object>>) childrenMapChildren) {
                    if (CatalogueType.create((byte) m.get("catalogueType")).equals(catalogueType)) {
                        childrenMap.put("catalogueId", m.get("id"));
                    }
                }
                childrenList.add(childrenMap);
            }
        }

        //遍历库分组数据，并递归获取下级节点
        for (Map<String, Object> map : dataArchivesGroupList) {
            if (treeMap.get("fondsId").equals(map.get("fondsId")) && treeMap.get("id").equals(map.get("parentId"))) {
                Map<String, Object> childrenMap = map;
                childrenMap.put("childrenType", "ArchivesGroup");
                childrenMap = getSubArchivesGroupToCatalogueTreeMap(
                        dataArchivesGroupList
                        , dataArchivesList
                        , dataCatalogueList
                        , childrenMap
                        , hasCataloguePermission
                );
                childrenList.add(childrenMap);
            }
        }
        //将递归获取的下属库分组树加入上级节点
        if (!childrenList.isEmpty()) {
            treeMap.put("children", childrenList);
        }
        return treeMap;
    }

    /**
     * 通过上级库节点递归获取目录树.
     */
    private Map<String, Object> getSubCatalogueTreeMap(
            List<Map<String, Object>> dataCatalogueList
            , Map<String, Object> treeMap
            , Function<Integer, Boolean> hasCataloguePermission
    ) {
        //创建一个空的下级目录节点列表
        List<Map<String, Object>> childrenList = new ArrayList<>();
        //遍历目录数据，获取下级目录节点
        for (Map<String, Object> map : dataCatalogueList) {
            int catalogueId = ((UInteger) map.get("id")).intValue();
            if (treeMap.get("id").equals(map.get("archivesId"))
                    && hasCataloguePermission.apply(catalogueId)
            ) {
                //获取下级节点
                map.put("childrenType", "Catalogue");
                childrenList.add(map);
            }
        }
        //添加查询后的下级全宗节点数据到本节点
        if (!childrenList.isEmpty()) {
            treeMap.put("children", childrenList);
        }
        return treeMap;
    }


    /**
     * 获取全部库分组.
     */
    private List<Map<String, Object>> getAllArchivesGroupList() {
        return dslContext.select(
                archivesGroup.ID.as("id"),
                archivesGroup.NAME.as("name"),
                archivesGroup.FONDS_ID.as("fondsId"),
                archivesGroup.PARENT_ID.as("parentId"))
                .from(archivesGroup)
                .where(archivesGroup.GMT_DELETED.equal(0))
                .fetch().intoMaps();
    }

    /**
     * 获取档案库.
     */
    private List<Map<String, Object>> getAllArchivesList(Byte type) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(archives.GMT_DELETED.equal(0));
        if (null != type) {
            conditions.add(archives.TYPE.equal(type));
        }
        return dslContext.select(
                archives.ID.as("id"),
                archives.STRUCTURE.as("structure"),
                archives.NAME.as("name"),
                archives.ARCHIVES_GROUP_ID.as("archivesGroupId"),
                archives.TYPE.as("type"))
                .from(archives)
                .where(conditions)
                .fetch().intoMaps();
    }

    /**
     * 获取全部目录.
     */
    private List<Map<String, Object>> getAllCatalogueList() {
        return dslContext.select(
                archivesCatalogue.ID.as("id"),
                archivesCatalogue.CATALOGUE_TYPE.as("catalogueType"),
                archivesCatalogue.ARCHIVES_ID.as("archivesId"))
                .from(archivesCatalogue)
                .fetch().intoMaps();
    }

    /**
     * 查询库内容类型列表
     */
    public List<Map<String, Object>> getContentTypeList() {
        return dslContext.select(archivesContentType.ID.as("id"),
                archivesContentType.NAME.as("name"))
                .from(archivesContentType)
                .fetch().intoMaps();
    }

    /**
     * 查询文件类型列表
     */
    public List<Map<String, Object>> getFileTypeList() {
        return dslContext.select(archivesFileType.ID.as("id"),
                archivesFileType.NAME.as("name"))
                .from(archivesFileType)
                .fetch().intoMaps();
    }

    /**
     * 查询目录所属著录项
     */
    public List<Map<String, Object>> getDescriptionItemList(UInteger catalogueId) {
        return dslContext.select(
                archivesDescriptionItem.ID.as("id"),
                archivesDescriptionItem.METADATA_NAME.as("metadataName"),
                archivesDescriptionItem.DISPLAY_NAME.as("displayName"),
                archivesDescriptionItem.PROPERTY_TYPE.as("propertyType"),
                archivesDescriptionItem.DEFAULT_VALUE.as("defaultValue"),
                archivesDescriptionItem.DATA_TYPE.as("dataType"),
                archivesDescriptionItem.FIELD_WIDTH.as("fieldWidth"),
                archivesDescriptionItem.FIELD_PRECISION.as("fieldPrecision"),
                archivesDescriptionItem.FIELD_FORMAT.as("fieldFormat"),
                archivesDescriptionItem.IS_INCREMENT.as("isIncrement"),
                archivesDescriptionItem.IS_READ.as("isRead"),
                archivesDescriptionItem.IS_NULL.as("isNull"),
                archivesDescriptionItem.IS_DICTIONARY.as("isDictionary"),
                archivesDescriptionItem.DICTIONARY_TYPE.as("dictionaryType"),
                archivesDescriptionItem.DICTIONARY_NODE_ID.as("dictionaryNodeId"),
                archivesDescriptionItem.DICTIONARY_VALUE_TYPE.as("dictionaryValueType"),
                archivesDescriptionItem.DICTIONARY_ROOT_SELECT.as("dictionaryRootSelect"))
                .from(archivesDescriptionItem)
                .where(archivesDescriptionItem.CATALOGUE_ID.equal(catalogueId))
                .fetch().intoMaps();
    }

    /**
     * 档案库可以搜索的列
     */
    public Map<String, Object> getEntryColumns(Integer cid) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(archivesDescriptionItem.METADATA_ID.as("metadata_id"),
                archivesDescriptionItem.METADATA_NAME.as("metadata_name"),
                archivesDescriptionItem.DISPLAY_NAME.as("display_name"),
                archivesDescriptionItem.DATA_TYPE.as("data_type"))
                .from(archivesDescriptionItem)
                .where(archivesDescriptionItem.CATALOGUE_ID.equal(UInteger.valueOf(cid)))
                .fetch().intoMaps();
        resultMap.put("items", list);
        return resultMap;
    }

    /**
     * 库所属全宗
     */
    public int getFondsIdByArchiveId(UInteger catalogueId) {
        Result result = dslContext.select(archivesGroup.FONDS_ID).from(archivesGroup, archives, archivesCatalogue)
                .where(archivesCatalogue.ID.equal(catalogueId), archives.ID.equal(archivesCatalogue.ARCHIVES_ID), archives.ARCHIVES_GROUP_ID.equal(archivesGroup.ID)).fetch();
        if (result.size() == 0) {
            return 0;
        } else {
            return ((UInteger) result.getValue(0, 0)).intValue();
        }
    }

    /**
     * 库所属全宗号
     */
    public String getFondsCodeByArchiveId(UInteger archiveId) {
        Result result = dslContext.select(sysFonds.FONDS_CODE).from(sysFonds, archivesGroup, archives)
                .where(archives.ID.equal(archiveId)
                        , archives.ARCHIVES_GROUP_ID.equal(archivesGroup.ID)
                        , archivesGroup.FONDS_ID.equal(sysFonds.ID)).fetch();
        if (result.size() == 0) {
            return "";
        } else {
            return (String) result.getValue(0, 0);
        }
    }

    /**
     * 通过库ID获取目录ID及著录项
     */
    public Map<String, Object> getDescriptionItemListForPlaceOnFile(UInteger archivesId) {
        List<Map<String, Object>> catalogueList = dslContext.select(archivesCatalogue.ID.as("catalogueId"), archivesCatalogue.CATALOGUE_TYPE.as("type"))
                .from(archivesCatalogue).where(archivesCatalogue.ARCHIVES_ID.equal(archivesId)).fetch().intoMaps();
        HashMap resultMap = new HashMap<String, Object>();
        ArrayList resultList = new ArrayList<Map<String, Object>>();
        for (Map map : catalogueList) {

            List<Map<String, Object>> descriptionItemList = dslContext.select(
                    archivesDescriptionItem.ID.as("descriptionItemId"),
                    archivesDescriptionItem.METADATA_NAME.as("metadataName"),
                    archivesDescriptionItem.DISPLAY_NAME.as("displayName"))
                    .from(archivesDescriptionItem)
                    .where(archivesDescriptionItem.CATALOGUE_ID.equal((UInteger) map.get("catalogueId")))
                    .fetch().intoMaps();

            map.put("descriptionItem", descriptionItemList);

            CatalogueType catalogueType = CatalogueType.create((byte) map.get("type"));
            switch (catalogueType) {
                case File: {
                    map.put("type", "file");
                    break;
                }
                case Folder: {
                    map.put("type", "folder");
                    break;
                }
                case FolderFile: {
                    map.put("type", "folderFile");
                    break;
                }
                case Subject: {
                    map.put("type", "subject");
                    break;
                }
                default: {

                }
            }
            resultList.add(map);
        }
        resultMap.put("item", resultList);
        return resultMap;
    }

    /**
     * 通过库ID及结构获取目录ID
     */
    public UInteger getCatalogueIdByArchivesIdAndType(UInteger archivesId) {

        byte structure = (byte) dslContext.select(archives.STRUCTURE.as("structure"))
                .from(archives)
                .where(archives.ID.equal(archivesId))
                .fetch().getValue(0, 0);
        byte catalogueType = 0;
        switch (structure) {
            case 1: {
                catalogueType = 1;
                break;
            }
            case 2: {
                catalogueType = 2;
                break;
            }
            case 3: {
                catalogueType = 4;
                break;
            }
        }

        return (UInteger) dslContext.select(archivesCatalogue.ID.as("id"))
                .from(archivesCatalogue)
                .where(archivesCatalogue.ARCHIVES_ID.equal(archivesId), archivesCatalogue.CATALOGUE_TYPE.equal(catalogueType))
                .fetch().getValue(0, 0);
    }

    /**
     * 获取所属全宗的库分组树.
     */
    public Map<String, Object> getArchivesGroupTreeMap(UInteger fondsId, UInteger archivesGroupId) {
        Map<String, Object> resultMap = new HashMap<>();
        //伪造根节点，便于递归查询子档案库分组
        resultMap.put("id", UInteger.valueOf(1));
        resultMap.put("fondsId", fondsId);
        //查询
        resultMap = getSubArchivesGroupTreeMap(getArchivesGroups(fondsId, archivesGroupId), resultMap);
        //拼装返回数据信息
        if (null != resultMap.get("children")) {
            resultMap.put("items", resultMap.get("children"));
        } else {
            resultMap.put("items", new ArrayList<Map<String, Object>>());
        }
        //去除根节点数据
        resultMap.remove("id");
        resultMap.remove("children");
        return resultMap;
    }

    /**
     * 通过上级节点递归获取库分组树形列表
     */
    public Map<String, Object> getSubArchivesGroupTreeMap
    (List<Map<String, Object>> dataList, Map<String, Object> treeMap) {
        //创建一个空子档案库分组列表
        List<Map<String, Object>> subArchivesGroupList = new ArrayList<>();
        //遍历档案库分组数据，并递归添加子档案库分组的下级节点
        for (Map<String, Object> map : dataList) {
            if (treeMap.get("id").equals(map.get("parentId")) && treeMap.get("fondsId").equals(map.get("fondsId"))) {
                map.put("childrenType", "ArchivesGroup");
                map = getSubArchivesGroupTreeMap(dataList, map);
                //将递归添加后的子档案库分组放入子节点列表
                subArchivesGroupList.add(map);
            }
        }
        //将子节点列表加入根节点档案分类信息
        if (!subArchivesGroupList.isEmpty()) {
            treeMap.put("children", subArchivesGroupList);
        }
        return treeMap;
    }

    /**
     * 获取全宗下全部档案分类.
     */
    private List<Map<String, Object>> getArchivesGroups(UInteger fondsId, UInteger archivesGroupId) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(archivesGroup.GMT_DELETED.equal(0));
        if (null != fondsId) {
            conditions.add(archivesGroup.FONDS_ID.equal(fondsId));
        }
        if (null != archivesGroupId) {
            conditions.add(archivesGroup.ID.notEqual(archivesGroupId));
        }
        return dslContext.select(
                archivesGroup.ID.as("id"),
                archivesGroup.FONDS_ID.as("fondsId"),
                archivesGroup.NAME.as("name"),
                DSL.decode().when(archivesGroup.REMARK.isNull(), "").otherwise(archivesGroup.REMARK).as("remark"),
                archivesGroup.PARENT_ID.as("parentId"))
                .from(archivesGroup)
                .where(conditions)
                .fetch().intoMaps();
    }

    /**
     * 获取库详情.
     */
    public Map<String, Object> getArchives(UInteger id) {
        return dslContext.select(archives.ID.as("id"),
                archives.NAME.as("name"),
                archives.STRUCTURE.as("structure"),
                archives.ARCHIVES_GROUP_ID.as("archivesGroupId"),
                archives.CONTENT_TYPE_ID.as("contentTypeId"),
                archives.TYPE.as("type"),
                archives.REMARK.as("remark"))
                .from(archives).where(archives.ID.equal(id)).fetch().intoMaps().get(0);
    }

    /**
     * 获取库分组下库、目录树.
     */
    public Map<String, Object> getArchivesToCatalogueTree(UInteger archivesGroupId) {

        HashMap<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> resultList = new ArrayList<>();

        List<Map<String, Object>> dataArchivesList = getArchivesListByGroupId(archivesGroupId);
        List<Map<String, Object>> dataCatalogueList = getAllCatalogueList();
        //遍历库数据，获取下级库节点
        for (Map<String, Object> archive : dataArchivesList) {
            //递归获取下级节点
            archive.put("childrenType", "Archives");

            //创建一个空的下级目录节点列表
            List<Map<String, Object>> catalogueList = new ArrayList<>();
            //遍历目录数据，获取下级目录节点
            for (Map<String, Object> catalogue : dataCatalogueList) {
                if (archive.get("id").equals(catalogue.get("archivesId"))) {
                    //获取下级节点
                    catalogue.put("childrenType", "Catalogue");
                    catalogueList.add(catalogue);
                }
            }
            //添加查询后的下级全宗节点数据到本节点
            if (!catalogueList.isEmpty()) {
                archive.put("children", catalogueList);
                resultList.add(archive);
            }
        }
        resultMap.put("items", resultList);
        return resultMap;
    }

    /**
     * 获取库分组下库.
     */
    private List<Map<String, Object>> getArchivesListByGroupId(UInteger archivesGroupId) {
        return dslContext.select(
                archives.ID.as("id"),
                archives.STRUCTURE.as("structure"),
                archives.NAME.as("name"),
                archives.ARCHIVES_GROUP_ID.as("archivesGroupId"),
                archivesContentType.NAME.as("contentType"),
                archives.TYPE.as("type"))
                .from(archives, archivesContentType)
                .where(archives.ARCHIVES_GROUP_ID.equal(archivesGroupId), archives.GMT_DELETED.equal(0), archivesContentType.ID.equal(archives.CONTENT_TYPE_ID))
                .fetch().intoMaps();
    }

    public List<Map<String, Object>> getArchivalCodeRules(UInteger id) {
        return dslContext.select(archivesArchivalcodeRuler.ID.as("id"),
                archivesArchivalcodeRuler.TYPE.as("type"),
                archivesArchivalcodeRuler.VALUE.as("value"),
                archivesArchivalcodeRuler.DESCRIPTION_ITEM_ID.as("descriptionItemId"),
                archivesArchivalcodeRuler.INTERCEPTION_LENGTH.as("interceptionLength"),
                archivesArchivalcodeRuler.FLOW_NUMBER_LENGTH.as("flowNumberLength"),
                archivesArchivalcodeRuler.CATALOGUE_ID.as("catalogueId"),
                DSL.decode().when(archivesArchivalcodeRuler.REMARK.isNull(), "").otherwise(archivesArchivalcodeRuler.REMARK).as("remark"))
                .from(archivesArchivalcodeRuler)
                .where(archivesArchivalcodeRuler.CATALOGUE_ID.equal(id))
                .orderBy(archivesArchivalcodeRuler.ORDER_NUMBER)
                .fetch().intoMaps();
    }

    public Map<String, Object> getArchivalCodeRuler(UInteger id) {
        return dslContext.select(archivesArchivalcodeRuler.ID.as("id"),
                archivesArchivalcodeRuler.TYPE.as("type"),
                archivesArchivalcodeRuler.VALUE.as("value"),
                archivesArchivalcodeRuler.DESCRIPTION_ITEM_ID.as("descriptionItemId"),
                archivesArchivalcodeRuler.INTERCEPTION_LENGTH.as("interceptionLength"),
                archivesArchivalcodeRuler.CATALOGUE_ID.as("catalogueId"),
                DSL.decode().when(archivesArchivalcodeRuler.REMARK.isNull(), "").otherwise(archivesArchivalcodeRuler.REMARK).as("remark"))
                .from(archivesArchivalcodeRuler).where(archivesArchivalcodeRuler.ID.equal(id)).fetch().intoMaps().get(0);
    }

    /**
     * 获取全宗、库分组树.
     */
    public Map<String, Object> getFondsToArchivesGroupTree(Function<Integer, Boolean> hasFondsPermission) {
        Map<String, Object> resultMap = new HashMap<>();
        //伪造全宗根节点，便于递归查询
        resultMap.put("id", UInteger.valueOf(1));
        //查询
        resultMap = getSubFondsToArchivesGroupTreeMap(
                getAllFondsList()
                , getAllArchivesGroupList()
                , resultMap
                , hasFondsPermission);
        //拼装返回数据信息
        if (null != resultMap && null != resultMap.get("children")) {
            resultMap.put("items", resultMap.get("children"));
            //去除根节点数据
            resultMap.remove("id");
            resultMap.remove("children");
        } else {
            resultMap = new HashMap<>();
            resultMap.put("items", new ArrayList<Map<String, Object>>());
        }
        return resultMap;
    }

    /**
     * 通过全宗上级节点递归获取全宗、库分组树.
     */
    private Map<String, Object> getSubFondsToArchivesGroupTreeMap(
            List<Map<String, Object>> dataFondsList
            , List<Map<String, Object>> dataArchivesGroupList
            , Map<String, Object> treeMap
            , Function<Integer, Boolean> hasFondsPermission
    ) {

        //创建一个空的子列表
        List<Map<String, Object>> childrenList = new ArrayList<>();

        Map<String, Object> archivesGroupTreeMap = new HashMap<>();
        //伪造库分组根节点，便于递归查询
        archivesGroupTreeMap.put("id", UInteger.valueOf(1));
        archivesGroupTreeMap.put("fondsId", treeMap.get("id"));
        //递归查询库分组树
        archivesGroupTreeMap = getSubArchivesGroupTreeMap(
                dataArchivesGroupList
                , archivesGroupTreeMap
        );
        //添加查询后的下级库分组节点数据到本节点
        if (null != archivesGroupTreeMap.get("children")) {
            childrenList = (List) archivesGroupTreeMap.get("children");
        }

        //遍历全宗数据，获取下级全宗节点
        for (Map<String, Object> map : dataFondsList) {
            if (treeMap.get("id").equals(map.get("parentId"))) {
                //递归获取下级节点
                Map<String, Object> childrenMap = map;
                childrenMap.put("childrenType", "Fonds");
                childrenMap = getSubFondsToArchivesGroupTreeMap(
                        dataFondsList
                        , dataArchivesGroupList
                        , childrenMap
                        , hasFondsPermission
                );
                if (null != childrenMap) {
                    childrenList.add(childrenMap);
                }

            }
        }
        //添加查询后的下级全宗节点数据到本节点
        if (!childrenList.isEmpty()) {
            boolean hasChildFonds = childrenList.stream().anyMatch(
                    a -> "Fonds".equals(a.getOrDefault("childrenType", null))
            );
            if (hasChildFonds) {
                treeMap.put("children", childrenList);
            } else {
                int fondsId = ((UInteger) treeMap.get("id")).intValue();
                if (!hasFondsPermission.apply(fondsId)) {
                    return null;
                }
                treeMap.put("children", childrenList);
            }
        } else {
            int fondsId = ((UInteger) treeMap.get("id")).intValue();
            if (!hasFondsPermission.apply(fondsId)) {
                return null;
            }
        }
        return treeMap;
    }

    /**
     * 获取目录可关联的元数据规范.
     */
    public Map<String, Object> getMetadataStandardsRelationList(UInteger catalogueId) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Condition> conditions = new ArrayList<>();

        UInteger metadataStandardsId = (UInteger) dslContext.select(archivesCatalogue.METADATA_STANDARDS_ID)
                .from(archivesCatalogue).where(archivesCatalogue.ID.equal(catalogueId)).fetch().getValue(0, 0);

        if (null == metadataStandardsId) {
            conditions.add(businessMetadataStandards.ID.equal(metadataStandardsId));
        }
        List<Map<String, Object>> list = dslContext.select(businessMetadataStandards.ID.as("id"),
                businessMetadataStandards.METADATA_STANDARDS_CODE.as("code"),
                businessMetadataStandards.METADATA_STANDARDS_NAME.as("name"),
                DSL.decode().when(businessMetadataStandards.REMARK.isNull(), "").otherwise(businessMetadataStandards.REMARK).as("remark"))
                .from(businessMetadataStandards).where(conditions)
                .orderBy(businessMetadataStandards.ORDER_NUMBER).fetch().intoMaps();

        resultMap.put("items", list);
        return resultMap;
    }

    public Map<String, Object> getDescriptionItem(UInteger id) {
        return dslContext.select(
                archivesDescriptionItem.ID.as("id"),
                archivesDescriptionItem.METADATA_NAME.as("metadataName"),
                archivesDescriptionItem.DISPLAY_NAME.as("displayName"),
                archivesDescriptionItem.PROPERTY_TYPE.as("propertyType"),
                archivesDescriptionItem.DEFAULT_VALUE.as("defaultValue"),
                archivesDescriptionItem.DATA_TYPE.as("dataType"),
                archivesDescriptionItem.FIELD_WIDTH.as("fieldWidth"),
                archivesDescriptionItem.FIELD_PRECISION.as("fieldPrecision"),
                archivesDescriptionItem.FIELD_FORMAT.as("fieldFormat"),
                archivesDescriptionItem.IS_INCREMENT.as("isIncrement"),
                archivesDescriptionItem.IS_READ.as("isRead"),
                archivesDescriptionItem.IS_NULL.as("isNull"),
                archivesDescriptionItem.IS_DICTIONARY.as("isDictionary"),
                archivesDescriptionItem.DICTIONARY_TYPE.as("dictionaryType"),
                archivesDescriptionItem.DICTIONARY_NODE_ID.as("dictionaryNodeId"),
                archivesDescriptionItem.DICTIONARY_VALUE_TYPE.as("dictionaryValueType"),
                archivesDescriptionItem.DICTIONARY_ROOT_SELECT.as("dictionaryRootSelect"))
                .from(archivesDescriptionItem)
                .where(archivesDescriptionItem.ID.equal(id))
                .fetch().intoMaps().get(0);
    }
}
