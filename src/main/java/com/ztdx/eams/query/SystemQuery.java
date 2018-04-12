package com.ztdx.eams.query;

import com.ztdx.eams.query.jooq.Tables;
import com.ztdx.eams.query.jooq.tables.SysOrganization;
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

    @Autowired
    public SystemQuery(DSLContext dslContext) {
        this.dslContext = dslContext;
        resultMap = new HashMap<String, Object>();
    }

    /**
     * 获取下级机构列表.
     */
    public Map<String, Object> getLowerList(int id) {

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
    public Map<String, Object> get(int id) {
        resultMap = dslContext.select(sysOrganization.ID.as("id"),
                sysOrganization.ORG_CODE.as("code"),
                sysOrganization.ORG_NAME.as("name"),
                sysOrganization.PARENT_ID.as("parentId"),
                sysOrganization.ORDER_NUMBER.as("orderNumber"),
                sysOrganization.ORG_TYPE.as("type"))
                .from(sysOrganization).where(sysOrganization.ID.equal(id)).fetch().intoMaps().get(0);
        return resultMap;
    }
}
