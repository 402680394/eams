package com.ztdx.eams.query;

import com.ztdx.eams.query.jooq.Tables;
import com.ztdx.eams.query.jooq.tables.Archives;
import com.ztdx.eams.query.jooq.tables.ArchivesCatalogue;
import com.ztdx.eams.query.jooq.tables.ArchivesGroup;
import com.ztdx.eams.query.jooq.tables.SysFonds;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    public ArchivesQuery(DSLContext dslContext) {
        this.dslContext = dslContext;
    }


    public Map<String, Object> getArchivesGroupTreeMap() {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        //伪造全宗根节点，便于递归查询
        resultMap.put("id", UInteger.valueOf(0));
        //查询
        resultMap = getSubFondsAndArchivesGroupTreeMap(getAllFondsList(), getAllArchivesGroupList(), resultMap);
        //拼装返回数据信息
        resultMap.put("items", resultMap.get("subFonds"));
        //去除根档案分类数据
        resultMap.remove("id");
        resultMap.remove("subFonds");
        return resultMap;
    }

    /**
     * 获取全部档案库分组.
     */
    private List<Map<String, Object>> getAllArchivesGroupList() {
        return dslContext.select(
                archivesGroup.ID.as("id"),
                archivesGroup.NAME.as("name"),
                archivesGroup.FONDS_ID.as("fondsId"),
                archivesGroup.PARENT_ID.as("parentId"),
                archivesGroup.REMARK.as("remark"))
                .from(archivesGroup)
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
     * 通过根节点递归获取全宗档案库分组树形列表.
     */
    public Map<String, Object> getSubFondsAndArchivesGroupTreeMap(List<Map<String, Object>> dataFondsList, List<Map<String, Object>> dataArchivesGroupList, Map<String, Object> treeMap) {

        Map<String, Object> archivesGroupTreeMap = new HashMap<String, Object>();
        //伪造档案分组根节点，便于递归查询
        archivesGroupTreeMap.put("id", UInteger.valueOf(0));
        archivesGroupTreeMap.put("fondsId", treeMap.get("id"));
        //递归查询
        archivesGroupTreeMap = getSubArchivesGroupTreeMap(dataArchivesGroupList, archivesGroupTreeMap);
        //获取全宗下档案分组数据
        if (archivesGroupTreeMap.get("subArchivesGroup") != null) {
            treeMap.put("subArchivesGroup", archivesGroupTreeMap.get("subArchivesGroup"));
        }

        //创建一个空的子全宗列表
        List<Map<String, Object>> subFondsList = new ArrayList<>();
        //遍历全宗数据，并将全宗加入全宗列表
        for (Map<String, Object> map : dataFondsList) {
            if (map.get("parentId").equals(treeMap.get("id"))) {
                //递归添加子全宗所属词典分类与下级全宗
                map = getSubFondsAndArchivesGroupTreeMap(dataFondsList, dataArchivesGroupList, map);
                subFondsList.add(map);
            }
        }
        //将子全宗列表加入根节点
        if (!subFondsList.isEmpty()) {
            treeMap.put("subFonds", subFondsList);
        }
        return treeMap;
    }

    /**
     * 通过根节点递归获取档案库分组树形列表.
     */
    private Map<String, Object> getSubArchivesGroupTreeMap(List<Map<String, Object>> dataArchivesGroupList, Map<String, Object> treeMap) {
        //创建一个空的档案库分组列表
        List<Map<String, Object>> subArchivesGroupList = new ArrayList<>();

        //遍历档案库分组数据，并将所属档案库分组加入档案库分组列表
        for (Map<String, Object> map : dataArchivesGroupList) {
            if (map.get("fondsId").equals(treeMap.get("fondsId")) && map.get("parentId").equals(treeMap.get("id"))) {
                map = getSubArchivesGroupTreeMap(dataArchivesGroupList, map);
                subArchivesGroupList.add(map);
            }
        }
        //将所属档案库分组列表加入上级节点
        if (!subArchivesGroupList.isEmpty()) {
            treeMap.put("subArchivesGroup", subArchivesGroupList);
        }
        return treeMap;
    }
}
