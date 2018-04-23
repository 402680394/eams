package com.ztdx.eams.query;

import com.ztdx.eams.domain.system.model.User;
import com.ztdx.eams.query.jooq.Tables;
import com.ztdx.eams.query.jooq.tables.SysFonds;
import com.ztdx.eams.query.jooq.tables.SysOrganization;
import com.ztdx.eams.query.jooq.tables.SysUser;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by li on 2018/4/11.
 */
@Service
public class SystemQuery {

    private final DSLContext dslContext;

    private Map<String, Object> resultMap;

    private SysOrganization sysOrganization = Tables.SYS_ORGANIZATION;

    private SysUser sysUser = Tables.SYS_USER;

    private SysFonds sysFonds = Tables.SYS_FONDS;

    @Autowired
    public SystemQuery(DSLContext dslContext) {
        this.dslContext = dslContext;
        resultMap = new HashMap<String, Object>();
    }

    /**
     * 获取全部机构树形列表.
     */
    public Map<String, Object> getOrganizationTreeMap() {
        //伪造根机构，便于递归查询子机构
        resultMap.put("id", UInteger.valueOf(0));
        //查询
        resultMap = getSubOrganizationTreeMap(getAllOrganizationList(), resultMap);
        //拼装返回数据信息
        resultMap.put("items", resultMap.get("subOrg"));
        //去除根机构数据
        resultMap.remove("id");
        resultMap.remove("subOrg");
        return resultMap;
    }

    /**
     * 获取全部机构.
     */
    public List<Map<String, Object>> getAllOrganizationList() {
        return dslContext.select(
                sysOrganization.ID.as("id"),
                sysOrganization.ORG_CODE.as("code"),
                sysOrganization.ORG_NAME.as("name"),
                sysOrganization.PARENT_ID.as("parentId"),
                sysOrganization.ORDER_NUMBER.as("orderNumber"),
                sysOrganization.ORG_TYPE.as("type"))
                .from(sysOrganization)
                .orderBy(sysOrganization.ORG_TYPE,sysOrganization.ORDER_NUMBER)
                .fetch().intoMaps();
    }

    /**
     * 通过节点递归获取机构子列表.
     */
    public Map<String, Object> getSubOrganizationTreeMap(List<Map<String, Object>> dataList, Map<String, Object> treeMap) {
        //创建一个空的子机构列表
        List<Map<String, Object>> subOrgList = new ArrayList<Map<String, Object>>();
        //遍历机构数据，并递归添加子机构的下级机构
        for (Map<String, Object> map : dataList) {
            if (map.get("parentId").equals(treeMap.get("id"))) {
                map = getSubOrganizationTreeMap(dataList, map);
                //将递归添加后的子机构放入子机构列表
                subOrgList.add(map);
            }
        }
        //将子机构列表加入根节点
        treeMap.put("subOrg", subOrgList);
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
                sysOrganization.ORDER_NUMBER.as("orderNumber"),
                sysOrganization.ORG_TYPE.as("type"))
                .from(sysOrganization).where(sysOrganization.ID.equal(id)).fetch().intoMaps().get(0);
    }

    /**
     * 根据条件查询机构下属用户.
     */
    public Map<String, Object> getUserListByOrg(User user, int pageNum) {

        int index = (pageNum - 1) * 10;

        List<Map<String, Object>> list =
                dslContext.select(
                        sysUser.ID.as("id"),
                        sysUser.REAL_NAME.as("name"),
                        sysUser.WORKERS.as("workers"),
                        sysUser.USERNAME.as("username"),
                        sysUser.PHONE.as("phone"),
                        sysUser.EMAIL.as("email"),
                        sysUser.JOB.as("job"),
                        sysUser.REMARK.as("remark"),
                        sysUser.FLAG.as("flag"))
                        .from(sysUser)
                        .where(sysUser.ORGANIZATION_ID.equal(UInteger.valueOf(user.getOrganizationId())),
                                sysUser.REAL_NAME.like("%" + user.getName() + "%"),
                                sysUser.WORKERS.like("%" + user.getWorkers() + "%"),
                                sysUser.USERNAME.like("%" + user.getUsername() + "%"),
                                sysUser.PHONE.like("%" + user.getPhone() + "%"),
                                sysUser.EMAIL.like("%" + user.getEmail() + "%"),
                                sysUser.JOB.like("%" + user.getJob() + "%"))
                        .limit(index, 10)
                        .fetch().intoMaps();

        int total = getUserListTotalByOrg(user);

        resultMap.put("items", list);
        resultMap.put("total", total);
        return resultMap;
    }

    /**
     * 根据条件查询机构下属用户总数.
     */
    public int getUserListTotalByOrg(User user) {
        return (int) dslContext.select(sysUser.ID.count()).from(sysUser)
                .where(sysUser.ORGANIZATION_ID.equal(UInteger.valueOf(user.getOrganizationId())),
                        sysUser.REAL_NAME.like("%" + user.getName() + "%"),
                        sysUser.WORKERS.like("%" + user.getWorkers() + "%"),
                        sysUser.USERNAME.like("%" + user.getUsername() + "%"),
                        sysUser.PHONE.like("%" + user.getPhone() + "%"),
                        sysUser.EMAIL.like("%" + user.getEmail() + "%"),
                        sysUser.JOB.like("%" + user.getJob() + "%")).fetch().getValue(0, 0);
    }

    /**
     * 根据条件查询所有用户.
     */
    public Map<String, Object> getUserList(User user, int pageNum) {

        int index = (pageNum - 1) * 10;

        List<Map<String, Object>> list =
                dslContext.select(
                        sysUser.ID.as("id"),
                        sysUser.REAL_NAME.as("name"),
                        sysUser.WORKERS.as("workers"),
                        sysUser.USERNAME.as("username"),
                        sysUser.PHONE.as("phone"),
                        sysUser.EMAIL.as("email"),
                        sysUser.JOB.as("job"),
                        sysUser.REMARK.as("remark"),
                        sysUser.FLAG.as("flag"))
                        .from(sysUser)
                        .where(sysUser.REAL_NAME.like("%" + user.getName() + "%"),
                                sysUser.WORKERS.like("%" + user.getWorkers() + "%"),
                                sysUser.USERNAME.like("%" + user.getUsername() + "%"),
                                sysUser.PHONE.like("%" + user.getPhone() + "%"),
                                sysUser.EMAIL.like("%" + user.getEmail() + "%"),
                                sysUser.JOB.like("%" + user.getJob() + "%"))
                        .limit(index, 10)
                        .fetch().intoMaps();

        int total = getUserListTotal(user);

        resultMap.put("items", list);
        resultMap.put("total", total);
        return resultMap;
    }

    /**
     * 根据条件查询所有用户总数.
     */
    public int getUserListTotal(User user) {
        return (int) dslContext.select(sysUser.ID.count()).from(sysUser)
                .where(sysUser.REAL_NAME.like("%" + user.getName() + "%"),
                        sysUser.WORKERS.like("%" + user.getWorkers() + "%"),
                        sysUser.USERNAME.like("%" + user.getUsername() + "%"),
                        sysUser.PHONE.like("%" + user.getPhone() + "%"),
                        sysUser.EMAIL.like("%" + user.getEmail() + "%"),
                        sysUser.JOB.like("%" + user.getJob() + "%")).fetch().getValue(0, 0);
    }

    /**
     * 获取用户详情.
     */
    public Map<String, Object> getUser(UInteger id) {
        resultMap = dslContext.select(sysUser.ID.as("id"),
                sysUser.REAL_NAME.as("name"),
                sysUser.WORKERS.as("workers"),
                sysUser.USERNAME.as("username"),
                sysUser.ORGANIZATION_ID.as("organizationId"),
                sysUser.PHONE.as("phone"),
                sysUser.EMAIL.as("email"),
                sysUser.JOB.as("job"),
                sysUser.REMARK.as("remark"))
                .from(sysUser).where(sysUser.ID.equal(id)).fetch().intoMaps().get(0);
        return resultMap;
    }

    /**
     * 获取全部全宗树形列表.
     */
    public Map<String, Object> getFondsTreeMap() {
        //伪造根全宗，便于递归查询子全宗
        resultMap.put("id", UInteger.valueOf(0));
        //查询
        resultMap = getSubFondsTreeMap(getAllFondsList(), resultMap);
        //拼装返回数据信息
        resultMap.put("items", resultMap.get("subFonds"));
        //去除根全宗数据
        resultMap.remove("id");
        resultMap.remove("subFonds");
        return resultMap;
    }

    /**
     * 获取全部全宗.
     */
    public List<Map<String, Object>> getAllFondsList() {
        List<Map<String, Object>> dataList = dslContext.select(
                sysFonds.ID.as("id"),
                sysFonds.FONDS_CODE.as("code"),
                sysFonds.FONDS_NAME.as("name"),
                sysFonds.PARENT_ID.as("parentId"),
                sysFonds.ORDER_NUMBER.as("orderNumber"),
                sysFonds.FONDS_TYPE.as("type"))
                .from(sysFonds)
                .orderBy(sysFonds.FONDS_TYPE,sysFonds.ORDER_NUMBER)
                .fetch().intoMaps();
        return dataList;
    }

    /**
     * 递归获取全宗子列表.
     */
    public Map<String, Object> getSubFondsTreeMap(List<Map<String, Object>> dataList, Map<String, Object> treeMap) {
        //创建一个空的子全宗列表
        List<Map<String, Object>> subFondsList = new ArrayList<Map<String, Object>>();
        //遍历全宗数据，并递归添加子全宗下级全宗
        for (Map<String, Object> map : dataList) {
            if (map.get("parentId").equals(treeMap.get("id"))) {
                map = getSubFondsTreeMap(dataList, map);
                //将递归添加后的子全宗放入子全宗列表
                subFondsList.add(map);
            }
        }
        //将子全宗列表加入全宗信息
        treeMap.put("subFonds", subFondsList);
        return treeMap;
    }

    /**
     * 获取全宗详情.
     */
    public Map<String, Object> getFonds(UInteger id) {
        return dslContext.select(sysFonds.ID.as("id"),
                sysFonds.FONDS_NAME.as("name"),
                sysFonds.FONDS_CODE.as("workers"),
                sysFonds.PARENT_ID.as("username"),
                sysFonds.FONDS_TYPE.as("organizationId"),
                sysFonds.REMARK.as("remark"))
                .from(sysFonds).where(sysFonds.ID.equal(id)).fetch().intoMaps().get(0);
    }
}
