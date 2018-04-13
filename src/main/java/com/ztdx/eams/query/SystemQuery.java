package com.ztdx.eams.query;

import com.ztdx.eams.domain.system.model.User;
import com.ztdx.eams.query.jooq.Tables;
import com.ztdx.eams.query.jooq.tables.SysOrganization;
import com.ztdx.eams.query.jooq.tables.SysUser;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    public SystemQuery(DSLContext dslContext) {
        this.dslContext = dslContext;
        resultMap = new HashMap<String, Object>();
    }

    /**
     * 获取下级机构列表.
     */
    public Map<String, Object> getOrganizationLowerList(int id) {

        List<Map<String, Object>> list = dslContext.select(
                sysOrganization.ID.as("id"),
                sysOrganization.ORG_CODE.as("code"),
                sysOrganization.ORG_NAME.as("name"),
                sysOrganization.PARENT_ID.as("parentId"),
                sysOrganization.ORDER_NUMBER.as("orderNumber"),
                sysOrganization.ORG_TYPE.as("type"))
                .from(sysOrganization)
                .where(sysOrganization.PARENT_ID.equal(id))
                .orderBy(sysOrganization.ORDER_NUMBER, sysOrganization.ORG_TYPE)
                .fetch().intoMaps();

        resultMap.put("items", list);
        return resultMap;
    }

    /**
     * 获取机构详情.
     */
    public Map<String, Object> getOrganization(int id) {
        resultMap = dslContext.select(sysOrganization.ID.as("id"),
                sysOrganization.ORG_CODE.as("code"),
                sysOrganization.ORG_NAME.as("name"),
                sysOrganization.PARENT_ID.as("parentId"),
                sysOrganization.ORDER_NUMBER.as("orderNumber"),
                sysOrganization.ORG_TYPE.as("type"))
                .from(sysOrganization).where(sysOrganization.ID.equal(id)).fetch().intoMaps().get(0);
        return resultMap;
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
                        .where(sysUser.ORGANIZATION_ID.equal(user.getOrganizationId()),
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
                .where(sysUser.ORGANIZATION_ID.equal(user.getOrganizationId()),
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

        int total = getUserListTotalByOrg(user);

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
    public Map<String, Object> getUser(int id) {
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
}
