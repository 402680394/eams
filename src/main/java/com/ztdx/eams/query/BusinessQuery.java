package com.ztdx.eams.query;

import com.ztdx.eams.query.jooq.Tables;
import com.ztdx.eams.query.jooq.tables.BusinessClassification;
import com.ztdx.eams.query.jooq.tables.BusinessDictionary;
import com.ztdx.eams.query.jooq.tables.BusinessDictionaryClassification;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
     * 通过ID获取档案分类及下级档案分类列表.
     */
    public Map<String, Object> getClassificationListMap(UInteger id) {
        resultMap.put("items", getClassificationList(id));
        return resultMap;
    }

    /**
     * 通过ID获取档案分类及下级档案分类列表.
     */
    public List<Map<String, Object>> getClassificationList(UInteger id) {
        //获取档案分类列表
        List<Map<String, Object>> classificationList = dslContext.select(
                businessClassification.ID.as("id"),
                businessClassification.CLASSIFICATION_CODE.as("code"),
                businessClassification.CLASSIFICATION_NAME.as("name"),
                businessClassification.REMARK.as("remark"),
                businessClassification.RETENTION_PERIOD.as("retentionPeriod"),
                businessClassification.PARENT_ID.as("parentId"),
                businessClassification.ORDER_NUMBER.as("orderNumber"))
                .from(businessClassification)
                .where(businessClassification.PARENT_ID.equal(id))
                .orderBy(businessClassification.ORDER_NUMBER)
                .fetch().intoMaps();
        //判断档案分类下是否有子档案分类
        for (Map<String, Object> map : classificationList) {
            //如果有，递归查询子档案分类，并放入父档案分类数据中
            if (hasSub((UInteger) map.get("id"))) {
                List<Map<String, Object>> subClassificationList = getClassificationList((UInteger) map.get("id"));

                map.put("subClassification", subClassificationList);
            }
        }
        return classificationList;
    }

    /**
     * 通过ID判断是否有子档案分类.
     */
    public boolean hasSub(UInteger id) {
        int total = (int) dslContext.select(businessClassification.ID.count()).from(businessClassification).where(businessClassification.PARENT_ID.equal(id)).fetch().getValue(0, 0);
        if (total != 0) {
            return true;
        }
        return false;
    }

}
