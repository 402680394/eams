package com.ztdx.eams.query;

import com.ztdx.eams.query.jooq.Tables;
import com.ztdx.eams.query.jooq.tables.SysFonds;
import com.ztdx.eams.query.jooq.tables.SysOrganization;
import com.ztdx.eams.query.jooq.tables.SysRole;
import com.ztdx.eams.query.jooq.tables.SysUser;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by li on 2018/4/11.
 */
@Service
public class SystemQuery {

    private final DSLContext dslContext;

    private SysOrganization sysOrganization = Tables.SYS_ORGANIZATION;

    private SysUser sysUser = Tables.SYS_USER;

    private SysFonds sysFonds = Tables.SYS_FONDS;

    private SysRole sysRole = Tables.SYS_ROLE;
    @Autowired
    public SystemQuery(DSLContext dslContext) {
        this.dslContext = dslContext;
    }


    /**
     * 获取全宗所属机构树.
     */
    public Map<String, Object> getOrganizationTreeMapByFondsId(UInteger fondsId) {
        Map<String, Object> resultMap = new HashMap<>();
        //伪造根机构，便于递归查询子机构
        resultMap.put("id", UInteger.valueOf(1));
        //查询
        resultMap = getSubOrganizationTreeMap(getAllOrganizationListByFondsId(fondsId), resultMap);
        //拼装返回数据信息
        if (null != resultMap.get("children")) {
            resultMap.put("items", resultMap.get("children"));
        } else {
            resultMap.put("items", new ArrayList<Map<String, Object>>());
        }
        //去除根机构数据
        resultMap.remove("id");
        resultMap.remove("children");
        return resultMap;
    }

    /**
     * 通过上级机构节点与机构类型获取机构树.
     */
    public Map<String, Object> getOrganizationTreeMap(int id, int type) {
        Map<String, Object> resultMap = new HashMap<>();
        //伪造上级机构，便于递归查询子机构
        resultMap.put("id", UInteger.valueOf(id));
        //查询
        if (type == 0) {
            resultMap = getSubOrganizationTreeMap(getAllOrganizationList(), resultMap);
        } else {
            resultMap = getSubOrganizationTreeMap(getAllOrganizationListByType(type), resultMap);

        }
        //拼装返回数据信息
        if (null != resultMap.get("children")) {
            resultMap.put("items", resultMap.get("children"));
        } else {
            resultMap.put("items", new ArrayList<Map<String, Object>>());
        }
        //去除根机构数据
        resultMap.remove("id");
        resultMap.remove("children");
        return resultMap;
    }

    /**
     * 获取全部机构.
     */
    private List<Map<String, Object>> getAllOrganizationList() {
        return dslContext.select(
                sysOrganization.ID.as("id"),
                sysOrganization.ORG_CODE.as("code"),
                sysOrganization.ORG_NAME.as("name"),
                sysOrganization.PARENT_ID.as("parentId"),
                sysOrganization.ORDER_NUMBER.as("orderNumber"),
                sysOrganization.FONDS_ID.as("fondsId"),
                sysOrganization.ORG_TYPE.as("type"))
                .from(sysOrganization)
                .orderBy(sysOrganization.ORG_TYPE, sysOrganization.ORDER_NUMBER)
                .fetch().intoMaps();
    }

    /**
     * 通过公司类型获取机构.
     */
    private List<Map<String, Object>> getAllOrganizationListByType(int type) {
        return dslContext.select(
                sysOrganization.ID.as("id"),
                sysOrganization.ORG_CODE.as("code"),
                sysOrganization.ORG_NAME.as("name"),
                sysOrganization.PARENT_ID.as("parentId"),
                sysOrganization.ORDER_NUMBER.as("orderNumber"),
                sysOrganization.FONDS_ID.as("fondsId"),
                sysOrganization.ORG_TYPE.as("type"))
                .from(sysOrganization)
                .where(sysOrganization.ORG_TYPE.equal(type))
                .orderBy(sysOrganization.ORG_TYPE, sysOrganization.ORDER_NUMBER)
                .fetch().intoMaps();
    }

    /**
     * 通过公司类型获取机构.
     */
    private List<Map<String, Object>> getAllOrganizationListByFondsId(UInteger fondsId) {
        return dslContext.select(
                sysOrganization.ID.as("id"),
                sysOrganization.ORG_CODE.as("code"),
                sysOrganization.ORG_NAME.as("name"),
                sysOrganization.PARENT_ID.as("parentId"),
                sysOrganization.ORDER_NUMBER.as("orderNumber"),
                sysOrganization.FONDS_ID.as("fondsId"),
                sysOrganization.ORG_TYPE.as("type"))
                .from(sysOrganization)
                .where(sysOrganization.FONDS_ID.equal(fondsId))
                .orderBy(sysOrganization.ORG_TYPE, sysOrganization.ORDER_NUMBER)
                .fetch().intoMaps();
    }

    /**
     * 通过节点递归获取机构子列表.
     */
    public Map<String, Object> getSubOrganizationTreeMap(List<Map<String, Object>> dataList, Map<String, Object> treeMap) {
        //创建一个空的子列表
        List<Map<String, Object>> childrenList = new ArrayList<>();
        //遍历机构数据，并递归添加子机构的下级机构
        for (Map<String, Object> map : dataList) {
            if (treeMap.get("id").equals(map.get("parentId"))) {
                map = getSubOrganizationTreeMap(dataList, map);
                //将递归添加后的子机构放入子列表
                childrenList.add(map);
            }
        }
        //将子列表加入根节点
        if (!childrenList.isEmpty()) {
            treeMap.put("children", childrenList);

        }
        return treeMap;
    }

    /**
     * 获取机构详情.
     */
    public Map<String, Object> getOrganization(UInteger id) {
        return dslContext.select(sysOrganization.ID.as("id"),
                sysOrganization.ORG_CODE.as("code"),
                sysOrganization.ORG_NAME.as("name"),
                sysOrganization.PARENT_ID.as("parentId"),
                sysOrganization.REMARK.as("remark"),
                sysOrganization.ORG_TYPE.as("type"))
                .from(sysOrganization).where(sysOrganization.ID.equal(id)).fetch().intoMaps().get(0);
    }

    /**
     * 根据条件查询机构下属用户.
     */
    public Map<String, Object> getUserListByOrg(int organizationId, String key, int pageNum,int pageSize) {

        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();

        int total = getUserListTotalByOrg(organizationId, key);
        if (total != 0) {
            list = dslContext.select(
                    sysUser.ID.as("id"),
                    sysUser.REAL_NAME.as("name"),
                    sysUser.WORKERS.as("workers"),
                    sysUser.ORGANIZATION_ID.as("organiztionId"),
                    sysUser.USERNAME.as("username"),
                    sysUser.PHONE.as("phone"),
                    sysUser.EMAIL.as("email"),
                    sysUser.JOB.as("job"),
                    sysUser.REMARK.as("remark"),
                    sysUser.FLAG.as("flag"))
                    .from(sysUser)
                    .where(sysUser.ORGANIZATION_ID.equal(UInteger.valueOf(organizationId)),
                            sysUser.REAL_NAME.like("%" + key + "%")
                                    .or(sysUser.WORKERS.like("%" + key + "%"))
                                    .or(sysUser.USERNAME.like("%" + key + "%"))
                                    .or(sysUser.PHONE.like("%" + key + "%"))
                                    .or(sysUser.EMAIL.like("%" + key + "%"))
                                    .or(sysUser.JOB.like("%" + key + "%")))
                    .limit((pageNum - 1) * pageSize, pageSize)
                    .fetch().intoMaps();
        }

        resultMap.put("items", list);
        resultMap.put("total", total);
        return resultMap;
    }

    /**
     * 根据条件查询机构下属用户总数.
     */
    public int getUserListTotalByOrg(int organizationId, String key) {
        return (int) dslContext.select(sysUser.ID.count()).from(sysUser)
                .where(sysUser.ORGANIZATION_ID.equal(UInteger.valueOf(organizationId)),
                        sysUser.REAL_NAME.like("%" + key + "%")
                                .or(sysUser.WORKERS.like("%" + key + "%"))
                                .or(sysUser.USERNAME.like("%" + key + "%"))
                                .or(sysUser.PHONE.like("%" + key + "%"))
                                .or(sysUser.EMAIL.like("%" + key + "%"))
                                .or(sysUser.JOB.like("%" + key + "%"))).fetch().getValue(0, 0);
    }

    /**
     * 根据条件查询所有用户.
     */
    public Map<String, Object> getUserList(String key, int pageNum,int pageSize) {

        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();

        int total = getUserListTotal(key);
        if (total != 0) {
            int index = (pageNum - 1) * pageSize;

            list = dslContext.select(
                    sysUser.ID.as("id"),
                    sysUser.REAL_NAME.as("name"),
                    sysUser.WORKERS.as("workers"),
                    sysUser.ORGANIZATION_ID.as("organiztionId"),
                    sysUser.USERNAME.as("username"),
                    sysUser.PHONE.as("phone"),
                    sysUser.EMAIL.as("email"),
                    sysUser.JOB.as("job"),
                    sysUser.REMARK.as("remark"),
                    sysUser.FLAG.as("flag"))
                    .from(sysUser)
                    .where(sysUser.REAL_NAME.like("%" + key + "%")
                            .or(sysUser.WORKERS.like("%" + key + "%"))
                            .or(sysUser.USERNAME.like("%" + key + "%"))
                            .or(sysUser.PHONE.like("%" + key + "%"))
                            .or(sysUser.EMAIL.like("%" + key + "%"))
                            .or(sysUser.JOB.like("%" + key + "%")))
                    .limit(index, pageSize)
                    .fetch().intoMaps();
        }

        resultMap.put("items", list);
        resultMap.put("total", total);
        return resultMap;
    }

    /**
     * 根据条件查询所有用户总数.
     */
    public int getUserListTotal(String key) {
        return (int) dslContext.select(sysUser.ID.count()).from(sysUser)
                .where(sysUser.REAL_NAME.like("%" + key + "%")
                        .or(sysUser.WORKERS.like("%" + key + "%"))
                        .or(sysUser.USERNAME.like("%" + key + "%"))
                        .or(sysUser.PHONE.like("%" + key + "%"))
                        .or(sysUser.EMAIL.like("%" + key + "%"))
                        .or(sysUser.JOB.like("%" + key + "%"))).fetch().getValue(0, 0);
    }

    /**
     * 获取用户详情.
     */
    public Map<String, Object> getUser(UInteger id) {
        return dslContext.select(sysUser.ID.as("id"),
                sysUser.REAL_NAME.as("name"),
                sysUser.WORKERS.as("workers"),
                sysUser.USERNAME.as("username"),
                sysUser.ORGANIZATION_ID.as("organizationId"),
                sysOrganization.ORG_NAME.as("organizationName"),
                sysUser.PHONE.as("phone"),
                sysUser.EMAIL.as("email"),
                sysUser.JOB.as("job"),
                sysUser.REMARK.as("remark"))
                .from(sysUser,sysOrganization)
                .where(sysUser.ID.equal(id), sysUser.ORGANIZATION_ID.equal(sysOrganization.ID))
                .fetch().intoMaps().get(0);
    }

    /**
     * 获取全部全宗树形列表.
     */
    public Map<String, Object> getFondsTreeMap(Function<Integer, Boolean> hasPermission) {
        Map<String, Object> resultMap = new HashMap<>();
        //伪造根全宗，便于递归查询子全宗
        resultMap.put("id", UInteger.valueOf(1));
        //查询
        resultMap = getSubFondsTreeMap(getAllFondsList(), resultMap, hasPermission);
        //拼装返回数据信息
        if (null != resultMap && null != resultMap.get("children")) {
            resultMap.put("items", resultMap.get("children"));
        } else {
            resultMap = new HashMap<>();
            resultMap.put("items", new ArrayList<Map<String, Object>>());
            return resultMap;
        }
        //去除根全宗数据
        resultMap.remove("id");
        resultMap.remove("children");
        return resultMap;
    }

    /**
     * 获取全部全宗.
     */
    private List<Map<String, Object>> getAllFondsList() {
        List<Map<String, Object>> dataList = dslContext.select(
                sysFonds.ID.as("id"),
                sysFonds.FONDS_CODE.as("code"),
                sysFonds.FONDS_NAME.as("name"),
                sysFonds.PARENT_ID.as("parentId"),
                sysFonds.ORDER_NUMBER.as("orderNumber"),
                sysFonds.REMARK.as("remark"))
                .from(sysFonds)
                .where(sysFonds.GMT_DELETED.equal(0))
                .orderBy(sysFonds.ORDER_NUMBER)
                .fetch().intoMaps();
        return dataList;
    }

    /**
     * 递归获取全宗子列表.
     */
    public Map<String, Object> getSubFondsTreeMap(List<Map<String, Object>> dataList, Map<String, Object> treeMap, Function<Integer, Boolean> hasPermission) {
        //创建一个空的子列表
        List<Map<String, Object>> childrenList = new ArrayList<Map<String, Object>>();
        //遍历全宗数据，并递归添加子全宗下级全宗
        for (Map<String, Object> map : dataList) {
            if (treeMap.get("id").equals(map.get("parentId"))) {
                map = getSubFondsTreeMap(dataList, map, hasPermission);
                if (map != null) {
                    //将递归添加后的子全宗放入子列表
                    childrenList.add(map);
                }
            }
        }
        //将子列表加入
        if (!childrenList.isEmpty()) {
            treeMap.put("children", childrenList);
        } else {
            int fondsId = ((UInteger) treeMap.get("id")).intValue();
            if (!hasPermission.apply(fondsId)) {
                return null;
            }
        }
        return treeMap;
    }

    /**
     * 通过ID获取获取全宗.
     */
    public Map<String, Object> getFonds(UInteger id) {
        return dslContext.select(sysFonds.ID.as("id"),
                sysFonds.FONDS_NAME.as("name"),
                sysFonds.FONDS_CODE.as("code"),
                sysFonds.PARENT_ID.as("parentId"),
                sysFonds.REMARK.as("remark"))
                .from(sysFonds).where(sysFonds.ID.equal(id)).fetch().intoMaps().get(0);
    }

    /**
     * 获取全宗关联机构ID.
     */
    public List<Map<String, Object>> getAssociationId(UInteger fondsId) {
        return (List<Map<String, Object>>) dslContext.select(sysOrganization.ID.as("id")).from(sysOrganization).where(sysOrganization.FONDS_ID.equal(fondsId)).fetch().getValues("id");
    }

    /**
     * 通过ID获取获取全宗并获取全宗关联机构ID.
     */
    public Map<String, Object> getFondsAndAssociationId(UInteger id) {
        Map<String, Object> resultMap = getFonds(id);
        List<Map<String, Object>> list = getAssociationId(id);
        resultMap.put("association", list);
        return resultMap;
    }
    /**
     * 通过全宗ID获取获取全宗下角色.
     */
    public List<Map<String, Object>> getRoleListByFonds(UInteger fondsId) {
        Condition condition=sysRole.FONDS_ID.eq(fondsId);
        if(null==fondsId){
            condition=sysRole.FONDS_ID.isNull();
        }
        return dslContext.select(sysRole.ID, sysRole.ROLE_NAME, sysRole.REMARK).from(sysRole).where(condition).fetch().intoMaps();
    }
}
