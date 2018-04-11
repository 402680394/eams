package com.ztdx.eams.query;

import com.ztdx.eams.query.jooq.Tables;
import com.ztdx.eams.query.jooq.tables.SysOrganization;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * Created by li on 2018/4/11.
 */
@Service
public class SystemQuery {

    private final DSLContext dslContext;

    private HashMap<String,Object> resultMap;

    private SysOrganization sysOrganization = Tables.SYS_ORGANIZATION;

    @Autowired
    public SystemQuery(DSLContext dslContext) {
        this.dslContext = dslContext;
        resultMap =new HashMap<String,Object>();
    }

    /**
     * 获取下级机构列表.
     */
    public HashMap<String,Object> getLowerList(int orgId){

        List list=dslContext.select(
                sysOrganization.ID.as("id"),
                sysOrganization.ORG_CODE.as("code"),
                sysOrganization.ORG_NAME.as("name"),
                sysOrganization.ORG_PARENT_ID.as("parentId"),
                sysOrganization.ORG_ORDER_NUMBER.as("orderNumber"),
                sysOrganization.ORG_TYPE.as("type"))
                .from(sysOrganization)
                .where(sysOrganization.ORG_PARENT_ID.equal(orgId))
                .orderBy(sysOrganization.ORG_ORDER_NUMBER)
                .fetch().intoMaps();

        resultMap.put("items",list);
        return resultMap;
    }
}
