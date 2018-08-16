package com.ztdx.eams.query;

import com.ztdx.eams.query.jooq.Tables;
import com.ztdx.eams.query.jooq.tables.SysPermission;
import com.ztdx.eams.query.jooq.tables.SysRoleUser;
import com.ztdx.eams.query.jooq.tables.SysUser;
import com.ztdx.eams.query.jooq.tables.records.SysUserRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class SystemSecurityQuery {

    private final DSLContext dslContext;

    private SysPermission sysPermission = Tables.SYS_PERMISSION;

    private SysRoleUser sysRoleUser = Tables.SYS_ROLE_USER;

    private SysUser sysUser = Tables.SYS_USER;

    @Autowired
    public SystemSecurityQuery(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Set<String> getUserPermissions(String userName) {

         return dslContext.select(sysPermission.RESOURCE_URL)
                .from(sysPermission)
                .innerJoin(sysRoleUser)
                    .on(sysPermission.ROLE_ID.equal(sysRoleUser.ROLE_ID))
                .innerJoin(sysUser)
                    .on(sysRoleUser.USER_ID.equal(sysUser.ID))
                .where(sysUser.USERNAME.equal(userName)).fetch().intoSet(sysPermission.RESOURCE_URL);
    }

    public Map<String,Object> getUser(String userName) {
        String sql = dslContext.select(sysUser.PWD,sysUser.FLAG,sysUser.ID)
                .from(sysUser)
                .where(sysUser.USERNAME.equal(userName)).getSQL();

        SysUserRecord result = dslContext.select(sysUser.PWD,sysUser.FLAG,sysUser.ID)
                .from(sysUser)
                .where(sysUser.USERNAME.equal(userName)).fetchOne().into(sysUser);

        Map<String, Object> map = new HashMap<>();
        map.put("pwd", result.getPwd());
        map.put("flag", result.getFlag());
        map.put("id", result.getId().intValue());
        return map;
    }
}
