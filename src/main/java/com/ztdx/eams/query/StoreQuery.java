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

    @Autowired
    public StoreQuery(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    /**
     * 通过全宗id与关键字内容查询库房列表
     */
    public Map<String,Object> getStorageList(Integer fondsId,String keyWord){

        List<Condition> conditions =new ArrayList<>();
        conditions.add(storage.FONDS_ID.equal(UInteger.valueOf(fondsId)));
        conditions.add(storage.FONDS_ID.equal(sysFonds.ID));
        if(keyWord !=null && !keyWord.equals("")){
            conditions.add(storage.NAME.like("%" + keyWord +"%")
                    .or(storage.NUMBER.like("%" + keyWord +"%"))
                    .or(storage.DESCRIPTION.like("%" + keyWord +"%")));
        }

        Map<String,Object> resultMap = new HashMap<>();
        List<Map<String,Object>> list = dslContext.select(storage.ID.as("id"),
                storage.NAME.as("name"),
                storage.NUMBER.as("number"),
                storage.DESCRIPTION.as("description"),
                sysFonds.FONDS_NAME.as("fonds_name"))
                .from(storage,sysFonds)
                .where(conditions)
                .fetch().intoMaps();
        resultMap.put("items",list);

        return resultMap;
    }


    /**
     * 通过库房id与关键字内容查询监测点列表
     */
    public Map<String,Object> getMonitoringPointList(Integer storageId, String keyWord){

        List<Condition> conditions =new ArrayList<>();
        conditions.add(monitoringPoint.STORAGE_ID.equal(UInteger.valueOf(storageId)));
        conditions.add(monitoringPoint.STORAGE_ID.equal(storage.ID));
        if(keyWord !=null && !keyWord.equals("")){
            conditions.add(storage.NAME.like("%" + keyWord +"%")
                    .or(monitoringPoint.NUMBER.like("%" + keyWord +"%"))
                    .or(monitoringPoint.TYPE.like("%" + keyWord +"%")));
        }

        Map<String,Object> resultMap = new HashMap<>();
        List<Map<String,Object>> list = dslContext.select(monitoringPoint.ID.as("id"),
                monitoringPoint.STORAGE_ID.as("storage_id"),
                monitoringPoint.NUMBER.as("number"),
                monitoringPoint.TYPE.as("type"),
                monitoringPoint.STATUS.as("status"),
                monitoringPoint.REMARK.as("remark"),
                storage.NAME.as("name"))
                .from(monitoringPoint,storage)
                .where(conditions)
                .fetch().intoMaps();
        resultMap.put("items",list);

        return resultMap;
    }

    /**
     * 通过库房id与关键字内容查询监测记录列表
     */
    public Map<String,Object> getMonitoringRecordList(Integer storageId, String keyWord){


        List<Condition> conditions =new ArrayList<>();
        conditions.add(monitoringRecord.STORAGE_ID.equal(UInteger.valueOf(storageId)));
        conditions.add(monitoringRecord.MONITORING_POINT_ID.equal(monitoringPoint.ID));
        if(keyWord !=null && !keyWord.equals("")){
            conditions.add(monitoringRecord.TEMPERATURE_VALUE.like("%" + keyWord +"%")
                    .or(monitoringRecord.HUMIDITY_VALUE.like("%" + keyWord +"%"))
                    .or(monitoringRecord.TAKE_STEPS.like("%" + keyWord +"%")));
        }

        Map<String,Object> resultMap = new HashMap<>();
        List<Map<String,Object>> list = dslContext.select(monitoringRecord.ID.as("id"),
                monitoringPoint.NUMBER.as("number"),
                monitoringRecord.MONITORING_TIME.as("monitoring_time"),
                monitoringRecord.TEMPERATURE_VALUE.as("temperature_value"),
                monitoringRecord.HUMIDITY_VALUE.as("humidity_value"),
                monitoringRecord.TAKE_STEPS.as("take_steps"),
                monitoringRecord.STORAGE_ID.as("storage_id"))
                .from(monitoringRecord,monitoringPoint)
                .where(conditions)
                .fetch().intoMaps();
        resultMap.put("items",list);

        return resultMap;
    }


    /**
     * 查询监测点下拉列表
     */
    public Map<String, Object> getMonitoringPointType() {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = dslContext.select(monitoringPoint.ID.as("id"),
                monitoringPoint.NUMBER.as("number")
                ,monitoringPoint.TYPE.as("type"))
                .from(monitoringPoint)
                .fetch().intoMaps();
        resultMap.put("items", list);
        return resultMap;
    }



}
