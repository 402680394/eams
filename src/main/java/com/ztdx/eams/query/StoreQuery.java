package com.ztdx.eams.query;

import com.ztdx.eams.query.jooq.Tables;
import com.ztdx.eams.query.jooq.tables.*;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class StoreQuery {

    private final DSLContext dslContext;

    private final SysFonds sysFonds = Tables.SYS_FONDS;

    private final StoreStorage storage = Tables.STORE_STORAGE;

    private final StoreMonitoringPoint monitoringPoint = Tables.STORE_MONITORING_POINT;

    private final StoreMonitoringRecord monitoringRecord = Tables.STORE_MONITORING_RECORD;

    private final StoreBox storeBox = Tables.STORE_BOX;

    private final StoreBoxCodeRule storeBoxCodeRule = Tables.STORE_BOX_CODE_RULE;

    @Autowired
    public StoreQuery(DSLContext dslContext) {
        this.dslContext = dslContext;
    }


    /**
     * 获取全部库房列表
     */
    public Map<String, Object> getStorageList() {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(storage.ID.as("id"),
                storage.NAME.as("name"),
                storage.NUMBER.as("number"),
                storage.DESCRIPTION.as("description"),
                storage.FONDS_ID.as("fonds_id"),
                sysFonds.FONDS_NAME.as("fonds_name"))
                .from(storage, sysFonds)
                .where(storage.FONDS_ID.equal(sysFonds.ID))
                .fetch().intoMaps();
        resultMap.put("items", list);
        return resultMap;
    }

    /**
     * 通过关键字内容查询库房列表
     */
    public Map<String, Object> getStorageListByFondsIdAndKeyWord(String keyWord) {

        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(storage.ID.as("id"),
                storage.NAME.as("name"),
                storage.NUMBER.as("number"),
                storage.DESCRIPTION.as("description"),
                storage.FONDS_ID.as("fonds_id"),
                sysFonds.FONDS_NAME.as("fonds_name"))
                .from(storage, sysFonds)
                .where(storage.FONDS_ID.equal(sysFonds.ID),
                        storage.NAME.like("%" + keyWord.trim() + "%")
                                .or(storage.NUMBER.like("%" + keyWord.trim() + "%"))
                                .or(storage.DESCRIPTION.like("%" + keyWord.trim() + "%")))
                .fetch().intoMaps();
        resultMap.put("items", list);

        return resultMap;
    }

    /**
     * 通过库房id与关键字内容查询监测点列表
     */
    public Map<String, Object> getMonitoringPointList(Integer storageId, String keyWord) {

        List<Condition> conditions = new ArrayList<>();
        conditions.add(monitoringPoint.STORAGE_ID.equal(UInteger.valueOf(storageId)));
        conditions.add(monitoringPoint.STORAGE_ID.equal(storage.ID));
        if (keyWord != null && !keyWord.trim().equals("")) {
            conditions.add(storage.NAME.like("%" + keyWord.trim() + "%")
                    .or(monitoringPoint.NUMBER.like("%" + keyWord.trim() + "%"))
                    .or(monitoringPoint.TYPE.like("%" + keyWord.trim() + "%")));
        }

        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(monitoringPoint.ID.as("id"),
                monitoringPoint.STORAGE_ID.as("storage_id"),
                monitoringPoint.NUMBER.as("number"),
                monitoringPoint.TYPE.as("type"),
                monitoringPoint.STATUS.as("status"),
                monitoringPoint.REMARK.as("remark"),
                storage.NAME.as("name"))
                .from(monitoringPoint, storage)
                .where(conditions)
                .fetch().intoMaps();
        resultMap.put("items", list);

        return resultMap;
    }

    /**
     * 获取全部监测记录列表
     */
    public Map<String, Object> getMonitoringRecordList() {

        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(monitoringRecord.ID.as("id"),
                monitoringPoint.NUMBER.as("number"),
                monitoringRecord.MONITORING_TIME.as("monitoring_time"),
                monitoringRecord.TEMPERATURE_VALUE.as("temperature_value"),
                monitoringRecord.HUMIDITY_VALUE.as("humidity_value"),
                monitoringRecord.TAKE_STEPS.as("take_steps"),
                monitoringRecord.STORAGE_ID.as("storage_id"))
                .from(monitoringRecord, monitoringPoint)
                .where(monitoringRecord.MONITORING_POINT_ID.equal(monitoringPoint.ID))
                .fetch().intoMaps();
        resultMap.put("items", list);

        return resultMap;
    }

    /**
     * 通过库房id与关键字内容查询监测记录列表
     */
    public Map<String, Object> getMonitoringPointListByStorageIdAndKeyWord(Integer storageId, String keyWord) {

        List<Condition> conditions = new ArrayList<>();
        conditions.add(monitoringRecord.STORAGE_ID.equal(UInteger.valueOf(storageId)));
        conditions.add(monitoringRecord.MONITORING_POINT_ID.equal(monitoringPoint.ID));
        if (keyWord != null && !keyWord.trim().equals("")) {
            conditions.add(monitoringRecord.TEMPERATURE_VALUE.like("%" + keyWord.trim() + "%")
                    .or(monitoringRecord.HUMIDITY_VALUE.like("%" + keyWord.trim() + "%"))
                    .or(monitoringRecord.TAKE_STEPS.like("%" + keyWord.trim() + "%")));
        }

        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(monitoringRecord.ID.as("id"),
                monitoringPoint.NUMBER.as("number"),
                monitoringRecord.MONITORING_TIME.as("monitoring_time"),
                monitoringRecord.TEMPERATURE_VALUE.as("temperature_value"),
                monitoringRecord.HUMIDITY_VALUE.as("humidity_value"),
                monitoringRecord.TAKE_STEPS.as("take_steps"),
                monitoringRecord.STORAGE_ID.as("storage_id"))
                .from(monitoringRecord, monitoringPoint)
                .where(conditions)
                .fetch().intoMaps();
        resultMap.put("items", list);

        return resultMap;
    }


    /**
     * 查询监测点下拉列表
     */
    public Map<String, Object> getMonitoringPointType() {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(monitoringPoint.ID.as("id"),
                monitoringPoint.NUMBER.as("number")
                , monitoringPoint.TYPE.as("type"))
                .from(monitoringPoint)
                .fetch().intoMaps();
        resultMap.put("items", list);
        return resultMap;
    }

    /**
     * 根据条件获取档案盒列表
     */
    public Map<String, Object> getBoxList(int archivesId, String code, int status, int onFrame) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list;
        //容纳状况已满
        if (status == 1 && onFrame == 0) {
            list = dslContext.select(storeBox.ID.as("id"),
                    storeBox.CODE.as("code"),
                    storeBox.FILES_TOTAL.as("filesTotal"),
                    storeBox.PAGES_TOTAL.as("pagesTotal"),
                    storeBox.MAX_PAGES_TOTAL.as("maxPagesTotal"),
                    storeBox.WIDTH.as("width"),
                    storeBox.ON_FRAME.as("onFrame"),
                    storeBox.POINT.as("point"),
                    storeBox.REMARK.as("remark")
            ).from(storeBox)
                    .where(storeBox.ARCHIVES_ID.equal(UInteger.valueOf(archivesId)),
                            storeBox.CODE.like("%" + code + "%"),
                            storeBox.PAGES_TOTAL.equal(storeBox.MAX_PAGES_TOTAL))
                    .fetch().intoMaps();
            //容纳状况未满
        } else if (status == 2 && onFrame == 0) {
            list = dslContext.select(storeBox.ID.as("id"),
                    storeBox.CODE.as("code"),
                    storeBox.FILES_TOTAL.as("filesTotal"),
                    storeBox.PAGES_TOTAL.as("pagesTotal"),
                    storeBox.MAX_PAGES_TOTAL.as("maxPagesTotal"),
                    storeBox.WIDTH.as("width"),
                    storeBox.ON_FRAME.as("onFrame"),
                    storeBox.POINT.as("point"),
                    storeBox.REMARK.as("remark")
            ).from(storeBox)
                    .where(storeBox.ARCHIVES_ID.equal(UInteger.valueOf(archivesId)),
                            storeBox.CODE.like("%" + code + "%"),
                            storeBox.PAGES_TOTAL.notEqual(storeBox.MAX_PAGES_TOTAL))
                    .fetch().intoMaps();
            //已上架或未上架
        } else if (status == 0 && onFrame != 0) {
            list = dslContext.select(storeBox.ID.as("id"),
                    storeBox.CODE.as("code"),
                    storeBox.FILES_TOTAL.as("filesTotal"),
                    storeBox.PAGES_TOTAL.as("pagesTotal"),
                    storeBox.MAX_PAGES_TOTAL.as("maxPagesTotal"),
                    storeBox.WIDTH.as("width"),
                    storeBox.ON_FRAME.as("onFrame"),
                    storeBox.POINT.as("point"),
                    storeBox.REMARK.as("remark")
            ).from(storeBox)
                    .where(storeBox.ARCHIVES_ID.equal(UInteger.valueOf(archivesId)),
                            storeBox.CODE.like("%" + code + "%"),
                            storeBox.ON_FRAME.equal((byte) onFrame))
                    .fetch().intoMaps();
        } else if (status == 1 && onFrame != 0) {
            list = dslContext.select(storeBox.ID.as("id"),
                    storeBox.CODE.as("code"),
                    storeBox.FILES_TOTAL.as("filesTotal"),
                    storeBox.PAGES_TOTAL.as("pagesTotal"),
                    storeBox.MAX_PAGES_TOTAL.as("maxPagesTotal"),
                    storeBox.WIDTH.as("width"),
                    storeBox.ON_FRAME.as("onFrame"),
                    storeBox.POINT.as("point"),
                    storeBox.REMARK.as("remark")
            ).from(storeBox)
                    .where(storeBox.ARCHIVES_ID.equal(UInteger.valueOf(archivesId)),
                            storeBox.CODE.like("%" + code + "%"),
                            storeBox.PAGES_TOTAL.equal(storeBox.MAX_PAGES_TOTAL),
                            storeBox.ON_FRAME.equal((byte) onFrame))
                    .fetch().intoMaps();
        } else if (status == 2 && onFrame != 0) {
            list = dslContext.select(storeBox.ID.as("id"),
                    storeBox.CODE.as("code"),
                    storeBox.FILES_TOTAL.as("filesTotal"),
                    storeBox.PAGES_TOTAL.as("pagesTotal"),
                    storeBox.MAX_PAGES_TOTAL.as("maxPagesTotal"),
                    storeBox.WIDTH.as("width"),
                    storeBox.ON_FRAME.as("onFrame"),
                    storeBox.POINT.as("point"),
                    storeBox.REMARK.as("remark")
            ).from(storeBox)
                    .where(storeBox.ARCHIVES_ID.equal(UInteger.valueOf(archivesId)),
                            storeBox.CODE.like("%" + code + "%"),
                            storeBox.PAGES_TOTAL.notEqual(storeBox.MAX_PAGES_TOTAL),
                            storeBox.ON_FRAME.equal((byte) onFrame))
                    .fetch().intoMaps();
        } else {
            list = dslContext.select(storeBox.ID.as("id"),
                    storeBox.CODE.as("code"),
                    storeBox.FILES_TOTAL.as("filesTotal"),
                    storeBox.PAGES_TOTAL.as("pagesTotal"),
                    storeBox.MAX_PAGES_TOTAL.as("maxPagesTotal"),
                    storeBox.WIDTH.as("width"),
                    storeBox.ON_FRAME.as("onFrame"),
                    storeBox.POINT.as("point"),
                    storeBox.REMARK.as("remark")
            ).from(storeBox)
                    .where(storeBox.ARCHIVES_ID.equal(UInteger.valueOf(archivesId)),
                            storeBox.CODE.like("%" + code + "%"))
                    .fetch().intoMaps();
            //容纳状况已满
        }
        resultMap.put("items", list);
        return resultMap;
    }

    /**
     * 根据id获取档案盒信息
     */
    public Map<String, Object> getBox(UInteger id) {
        return dslContext.select(storeBox.ID.as("id"),
                storeBox.CODE.as("code"),
                storeBox.MAX_PAGES_TOTAL.as("maxPagesTotal"),
                storeBox.WIDTH.as("width"),
                storeBox.REMARK.as("remark")
        ).from(storeBox)
                .where(storeBox.ID.equal(id))
                .fetch().intoMaps().get(0);
    }

    /**
     * 根据档案库id获取档案盒号规则应用信息
     */
    public Map<String, Object> ruleApply(UInteger archivesId, List<Map<String, Object>> descriptionItems) {
        List<Map<String, Object>> rules = dslContext.select(storeBoxCodeRule.TYPE.as("type"),
                storeBoxCodeRule.NAME.as("type"),
                storeBoxCodeRule.VALUE.as("value"),
                storeBoxCodeRule.DESCRIPTION_ITEM_ID.as("descriptionItemId"),
                storeBoxCodeRule.INTERCEPTION_LENGTH.as("interceptionLength"),
                storeBoxCodeRule.ORDER_NUMBER.as("orderNumber"))
                .from(storeBoxCodeRule)
                .where(storeBoxCodeRule.ARCHIVES_ID.equal(archivesId))
                .fetch().intoMaps();

        for (Map rule : rules) {
            int type = (int) rule.get("type");
            //1-著录项值 2-著录项值对应编码
            if (type == 1 || type == 2) {
                int descriptionItemId = (int) rule.get("descriptionItemId");
                for (Map descriptionItem : descriptionItems) {
                    if (descriptionItemId == (int) descriptionItem.get("id")) {
                        rule.put("isDictionary", descriptionItem.get("isDictionary"));
                        rule.put("dictionaryType", descriptionItem.get("dictionaryType"));
                        rule.put("dictionaryNodeId", descriptionItem.get("dictionaryNodeId"));
                        rule.put("dictionaryRootSelect", descriptionItem.get("dictionaryRootSelect"));
                    }
                }
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("items", rules);
        return resultMap;
    }
}
