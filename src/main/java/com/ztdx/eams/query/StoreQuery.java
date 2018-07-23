package com.ztdx.eams.query;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.model.Entry;
import com.ztdx.eams.query.jooq.Tables;
import com.ztdx.eams.query.jooq.tables.*;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.*;


@Service
public class StoreQuery {

    private final DSLContext dslContext;

    private final SysFonds sysFonds = Tables.SYS_FONDS;

    private final StoreStorage storage = Tables.STORE_STORAGE;

    private final StoreMonitoringPoint monitoringPoint = Tables.STORE_MONITORING_POINT;

    private final StoreMonitoringRecord monitoringRecord = Tables.STORE_MONITORING_RECORD;

    private final StoreBox storeBox = Tables.STORE_BOX;

    private final StoreBoxCodeRule storeBoxCodeRule = Tables.STORE_BOX_CODE_RULE;

    private final StoreShelf storeShelf = Tables.STORE_SHELF;

    private final StoreShelfCell storeShelfCell = Tables.STORE_SHELF_CELL;

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
                .where(storage.FONDS_ID.equal(sysFonds.ID), sysFonds.GMT_DELETED.equal((byte) 0))
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
                        sysFonds.GMT_DELETED.equal((byte) 0),
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
                monitoringRecord.STORAGE_ID.as("storage_id"),
                monitoringPoint.TYPE.as("type"))
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
                monitoringRecord.STORAGE_ID.as("storage_id"),
                monitoringPoint.TYPE.as("type"))
                .from(monitoringRecord, monitoringPoint)
                .where(conditions)
                .fetch().intoMaps();
        resultMap.put("items", list);

        return resultMap;
    }


    /**
     * 查询监测点下拉列表
     */
    public Map<String, Object> getMonitoringPointType(Integer storageId) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(monitoringPoint.ID.as("id"),
                monitoringPoint.NUMBER.as("number"),
                monitoringPoint.TYPE.as("type"))
                .from(monitoringPoint)
                .where(monitoringPoint.STORAGE_ID.equal(UInteger.valueOf(storageId)))
                .fetch().intoMaps();
        resultMap.put("items", list);
        return resultMap;
    }

    /**
     * 根据条件获取档案盒列表
     */
    public Map<String, Object> getBoxList(int pageNum, int size, int archivesId, String code, int status, byte onFrame) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = null;
        int total = 0;
        if (status == 1 && onFrame == 0) {
            //容纳状况已满
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
                            storeBox.PAGES_TOTAL.greaterThan(storeBox.MAX_PAGES_TOTAL))
                    .limit((pageNum - 1) * size, size)
                    .fetch().intoMaps();

            total = (int) dslContext.select(storeBox.ID.as("id").count()
            ).from(storeBox)
                    .where(storeBox.ARCHIVES_ID.equal(UInteger.valueOf(archivesId)),
                            storeBox.CODE.like("%" + code + "%"),
                            storeBox.PAGES_TOTAL.greaterThan(storeBox.MAX_PAGES_TOTAL))
                    .fetch().getValue(0, 0);
        } else if (status == 2 && onFrame == 0) {
            //容纳状况未满
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
                            storeBox.PAGES_TOTAL.lessThan(storeBox.MAX_PAGES_TOTAL))
                    .limit((pageNum - 1) * size, size)
                    .fetch().intoMaps();

            total = (int) dslContext.select(storeBox.ID.as("id").count()
            ).from(storeBox)
                    .where(storeBox.ARCHIVES_ID.equal(UInteger.valueOf(archivesId)),
                            storeBox.CODE.like("%" + code + "%"),
                            storeBox.PAGES_TOTAL.lessThan(storeBox.MAX_PAGES_TOTAL))
                    .fetch().getValue(0, 0);
        } else if (status == 0 && onFrame != 0) {
            //已上架或未上架
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
                            storeBox.ON_FRAME.equal(onFrame))
                    .limit((pageNum - 1) * size, size)
                    .fetch().intoMaps();

            total = (int) dslContext.select(storeBox.ID.as("id").count()
            ).from(storeBox)
                    .where(storeBox.ARCHIVES_ID.equal(UInteger.valueOf(archivesId)),
                            storeBox.CODE.like("%" + code + "%"),
                            storeBox.ON_FRAME.equal(onFrame))
                    .fetch().getValue(0, 0);
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
                            storeBox.PAGES_TOTAL.greaterThan(storeBox.MAX_PAGES_TOTAL),
                            storeBox.ON_FRAME.equal(onFrame))
                    .limit((pageNum - 1) * size, size)
                    .fetch().intoMaps();

            total = (int) dslContext.select(storeBox.ID.as("id").count()
            ).from(storeBox)
                    .where(storeBox.ARCHIVES_ID.equal(UInteger.valueOf(archivesId)),
                            storeBox.CODE.like("%" + code + "%"),
                            storeBox.PAGES_TOTAL.greaterThan(storeBox.MAX_PAGES_TOTAL),
                            storeBox.ON_FRAME.equal(onFrame))
                    .fetch().getValue(0, 0);
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
                            storeBox.PAGES_TOTAL.lessThan(storeBox.MAX_PAGES_TOTAL),
                            storeBox.ON_FRAME.equal(onFrame))
                    .limit((pageNum - 1) * size, size)
                    .fetch().intoMaps();

            total = (int) dslContext.select(storeBox.ID.as("id").count()
            ).from(storeBox)
                    .where(storeBox.ARCHIVES_ID.equal(UInteger.valueOf(archivesId)),
                            storeBox.CODE.like("%" + code + "%"),
                            storeBox.PAGES_TOTAL.lessThan(storeBox.MAX_PAGES_TOTAL),
                            storeBox.ON_FRAME.equal(onFrame))
                    .fetch().getValue(0, 0);
        } else {
            //全部
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
                    .limit((pageNum - 1) * size, size)
                    .fetch().intoMaps();

            total = (int) dslContext.select(storeBox.ID.as("id").count()
            ).from(storeBox)
                    .where(storeBox.ARCHIVES_ID.equal(UInteger.valueOf(archivesId)),
                            storeBox.CODE.like("%" + code + "%"))
                    .fetch().getValue(0, 0);
        }
        resultMap.put("items", list);
        resultMap.put("total", total);
        return resultMap;
    }

    /**
     * 根据id获取档案盒信息
     */
    public Map<String, Object> getBox(UInteger id) {
        return dslContext.select(storeBox.ID.as("id"),
                storeBox.CODE_RULE.as("codeRule"),
                storeBox.FLOW_NUMBER.as("flowNumber"),
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
    public Map<String, Object> ruleApply(String fondsCode, UInteger archivesId, List<Map<String, Object>> descriptionItems, Entry entry) {
        List<Map<String, Object>> rules = dslContext.select(storeBoxCodeRule.TYPE.as("type"),
                storeBoxCodeRule.NAME.as("name"),
                storeBoxCodeRule.VALUE.as("value"),
                storeBoxCodeRule.DESCRIPTION_ITEM_ID.as("descriptionItemId"),
                storeBoxCodeRule.INTERCEPTION_LENGTH.as("interceptionLength"),
                storeBoxCodeRule.FLOW_NUMBER_LENGTH.as("flowNumberLength"),
                storeBoxCodeRule.ORDER_NUMBER.as("orderNumber"))
                .from(storeBoxCodeRule)
                .where(storeBoxCodeRule.ARCHIVES_ID.equal(archivesId))
                .orderBy(storeBoxCodeRule.ORDER_NUMBER)
                .fetch().intoMaps();

        for (Map rule : rules) {
            byte type = (byte) rule.get("type");
            //1-著录项值
            if (type == 1) {
                UInteger descriptionItemId = (UInteger) rule.get("descriptionItemId");
                for (Map descriptionItem : descriptionItems) {
                    if (descriptionItemId.equals(descriptionItem.get("id"))) {
                        rule.put("isDictionary", descriptionItem.get("isDictionary"));
                        rule.put("dictionaryType", descriptionItem.get("dictionaryType"));
                        rule.put("dictionaryNodeId", descriptionItem.get("dictionaryNodeId"));
                        rule.put("dictionaryValueType", descriptionItem.get("dictionaryValueType"));
                        rule.put("dictionaryRootSelect", descriptionItem.get("dictionaryRootSelect"));
                        if (null != entry) {
                            String metadataName = (String) descriptionItem.get("metadataName");
                            rule.put("value", entry.getItems().get(metadataName));
                        } else {
                            rule.put("value", "");
                        }
                    }
                }
                //3-档案库所属全宗号
            } else if (type == 2) {
                rule.put("value", fondsCode);
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("items", rules);
        return resultMap;
    }

    public Map<String, Object> listShelf(String key, int storageId, Pageable pageable) {
        Integer total = dslContext
                .selectCount()
                .from(storeShelf)
                .where(storeShelf.STORAGE_ID.eq(storageId)
                        , storeShelf.NAME.like("%" + key.trim() + "%")
                                .or(storeShelf.CODE.like("%" + key.trim() + "%"))
                                .or(storeShelf.REMARK.like("%" + key.trim() + "%")))
                .fetchOne().value1();

        List<Map<String, Object>> list = dslContext
                .select(
                        storeShelf.ID
                        , storeShelf.NAME
                        , storeShelf.NAME.as("title")
                        , storeShelf.CODE
                        , storeShelf.STORAGE_ID.as("storageId")
                        , storeShelf.REMARK
                        , storeShelf.SHELF_TYPE.as("shelfType")
                        , storeShelf.SECTION_NUM.as("sectionNum")
                )
                .from(storeShelf)
                .where(storeShelf.STORAGE_ID.eq(storageId)
                        , storeShelf.NAME.like("%" + key.trim() + "%")
                                .or(storeShelf.CODE.like("%" + key.trim() + "%"))
                                .or(storeShelf.REMARK.like("%" + key.trim() + "%")))
                .orderBy(storeShelf.GMT_CREATE.desc())
                .limit((int) pageable.getOffset(), pageable.getPageSize()).fetch().intoMaps();

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("items", list);
        resultMap.put("total", total);
        return resultMap;
    }

    public Map<String, Object> maxFlowNumber(int archivesId, String codeRule) {

        int maxFlowNumber = 0;
        String max = (String) dslContext
                .select(storeBox.FLOW_NUMBER.add(0).max().as("maxFlowNumber"))
                .from(storeBox)
                .where(storeBox.CODE_RULE.equal(codeRule), storeBox.ARCHIVES_ID.equal(UInteger.valueOf(archivesId)))
                .fetch().intoMaps().get(0).get("maxFlowNumber");
        if (null != max) {
            maxFlowNumber = Integer.parseInt(max);
        }
        maxFlowNumber = maxFlowNumber + 1;

        List<Map<String, Object>> flowNumberLengthResult = dslContext.select(storeBoxCodeRule.FLOW_NUMBER_LENGTH.as("flowNumberLength"))
                .from(storeBoxCodeRule)
                .where(storeBoxCodeRule.ARCHIVES_ID.equal(UInteger.valueOf(archivesId)), storeBoxCodeRule.TYPE.equal((byte) 4))
                .fetch().intoMaps();
        if (flowNumberLengthResult.size() != 1) {
            throw new InvalidArgumentException("盒号生成规则配置错误，请检查数据");
        }
        byte flowNumberLength = (byte) flowNumberLengthResult.get(0).get("flowNumberLength");
        //位数
        int count = 0;
        int number = maxFlowNumber;
        while (number > 0) {
            number = number / 10;
            count++;
        }
        if (count > flowNumberLength) {
            throw new InvalidArgumentException("流水号超出最大限制");
        }

        //格式化
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setMinimumIntegerDigits(flowNumberLength);
        formatter.setGroupingUsed(false);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("maxFlowNumber", formatter.format(maxFlowNumber));
        return resultMap;
    }

    public Collection<String> getCellIdsByShelfSectionIdIn(Collection<Integer> shelfSectionIds) {
        return dslContext.select(storeShelfCell.POINT_CODE)
                .from(storeShelfCell)
                .where(storeShelfCell.SHELF_SECTION_ID.in(shelfSectionIds))
                .fetch().intoSet(storeShelfCell.POINT_CODE);
    }

    public Collection<String> getCellIdsByShelfIdIn(Collection<Integer> shelfIds) {
        return dslContext.select(storeShelfCell.POINT_CODE)
                .from(storeShelfCell)
                .where(storeShelfCell.SHELF_ID.in(shelfIds))
                .fetch().intoSet(storeShelfCell.POINT_CODE);
    }
}
