package com.ztdx.eams.query;

import com.ztdx.eams.query.jooq.Tables;
import com.ztdx.eams.query.jooq.tables.BusinessClassification;
import com.ztdx.eams.query.jooq.tables.BusinessDictionary;
import com.ztdx.eams.query.jooq.tables.BusinessDictionaryClassification;
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

    private Map<String, Object> resultMap;

    private BusinessClassification businessClassification = Tables.BUSINESS_CLASSIFICATION;

    private BusinessDictionaryClassification businessDictionaryClassification = Tables.BUSINESS_DICTIONARY_CLASSIFICATION;

    private BusinessDictionary businessDictionary = Tables.BUSINESS_DICTIONARY;

    @Autowired
    public BusinessQuery(DSLContext dslContext) {
        this.dslContext = dslContext;
        resultMap = new HashMap<String, Object>();
    }

    /**
     * 获取全宗下全部档案分类树形列表.
     */
    public Map<String, Object> getAllClassificationTreeMap(UInteger fondsId) {
        //伪造根档案分类，便于递归查询子档案分类
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
    public List<Map<String, Object>> getAllClassificationList(UInteger fondsId) {
        List<Map<String, Object>> dataList = dslContext.select(
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
        return dataList;
    }

    /**
     * 递归获取档案分类下的子档案分类列表.
     */
    public Map<String, Object> getSubClassificationTreeMap(List<Map<String, Object>> dataList, Map<String, Object> classificationMap) {
        //创建一个空的子档案分类列表
        List<Map<String, Object>> subClassificationList = new ArrayList<Map<String, Object>>();
        //遍历档案分类数据，若档案分类有子档案分类，将添加子档案分类后的子档案分类加入子档案分类列表
        for (Map<String, Object> map : dataList) {
            if (map.get("parentId").equals(classificationMap.get("id"))) {
                //递归添加子档案分类的子档案分类
                map = getSubClassificationTreeMap(dataList, map);
                //将添加子档案分类后的子档案分类放入子档案分类列表
                subClassificationList.add(map);
            }
        }
        //将子档案分类列表加入档案分类信息
        classificationMap.put("subClassification", subClassificationList);
        return classificationMap;
    }

    /**
     * 获取档案分类详情.
     */
    public Map<String, Object> getClassification(UInteger id) {
        resultMap = dslContext.select(businessClassification.ID.as("id"),
                businessClassification.CLASSIFICATION_CODE.as("code"),
                businessClassification.CLASSIFICATION_NAME.as("name"),
                businessClassification.REMARK.as("remark"),
                businessClassification.RETENTION_PERIOD.as("retentionPeriod"),
                businessClassification.PARENT_ID.as("parentId"))
                .from(businessClassification).where(businessClassification.ID.equal(id)).fetch().intoMaps().get(0);
        return resultMap;
    }
}
