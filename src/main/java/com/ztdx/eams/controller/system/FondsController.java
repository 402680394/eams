package com.ztdx.eams.controller.system;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.basic.exception.ForbiddenException;
import com.ztdx.eams.domain.system.application.FondsService;
import com.ztdx.eams.domain.system.application.PermissionService;
import com.ztdx.eams.domain.system.application.RoleService;
import com.ztdx.eams.domain.system.model.Fonds;
import com.ztdx.eams.query.SystemQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by li on 2018/4/15.
 */
@RestController
@RequestMapping(value = "/fonds")
public class FondsController {

    private final FondsService fondsService;

    private final SystemQuery systemQuery;

    private final RoleService roleService;

    private final PermissionService permissionService;

    @Autowired
    public FondsController(FondsService fondsService, SystemQuery systemQuery, RoleService roleService, PermissionService permissionService) {
        this.fondsService = fondsService;
        this.systemQuery = systemQuery;
        this.roleService = roleService;
        this.permissionService = permissionService;
    }

    /**
     * @api {get} /fonds/treeList 获取全宗树形列表
     * @apiName treeList
     * @apiGroup fonds
     * @apiSuccess (Success 200) {Number} id 全宗ID.
     * @apiSuccess (Success 200) {String} code 全宗号.
     * @apiSuccess (Success 200) {String} name 全宗名称.
     * @apiSuccess (Success 200) {Number} parentId 上级全宗ID.
     * @apiSuccess (Success 200) {Number} orderNumber 同级排序编号.
     * @apiSuccess (Success 200) {String} remark 备注
     * @apiSuccess (Success 200) {Object[]} children 子节点信息
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": 全宗ID,"code": "全宗号","name": "父全宗0","parentId": 上级全宗ID,"orderNumber": 同级排序编号},
     * {"id": 全宗ID,"code": "全宗号","name": "父全宗1","parentId": 上级全宗ID,"orderNumber": 同级排序编号,"children": [
     * {"id": 全宗ID,"code": "全宗号","name": "子全宗1","parentId": 上级全宗ID,"orderNumber": 同级排序编号}]}]}}.
     */
    @RequestMapping(value = "/treeList", method = RequestMethod.GET)
    public Map<String, Object> treeList(@SessionAttribute(required = false) UserCredential LOGIN_USER) {
        int userId;
        if (LOGIN_USER != null) {
            userId = LOGIN_USER.getUserId();
        } else {
            throw new ForbiddenException("拒绝访问");
        }
        //可以管理的全宗
        Set<Integer> fondsIds = roleService.findUserManageFonds(userId);

        //全宗树
        return systemQuery.getFondsTreeMap(a -> {
            if (permissionService.hasAnyAuthority("ROLE_ADMIN")) {
                return true;
            } else {
                return fondsIds.contains(a);
            }
        });
    }

    /**
     * @api {post} /fonds 新增全宗
     * @apiName save
     * @apiGroup fonds
     * @apiParam {Number} parentId 上级全宗ID（根节点传1）
     * @apiParam {String{20}} code 全宗号
     * @apiParam {String{20}} name 全宗名称
     * @apiParam {String{100}} remark 备注(非必传)
     * @apiParam {Number[]} association 关联机构ID
     * @apiError (Error 400) message 1.全宗号已存在 2.请设置关联机构 3.机构已被其它全宗关联.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_fonds_write')")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody HashMap<String, Object> map) {
        Fonds fonds = new Fonds();
        fonds.setParentId(1);
        fonds.setCode((String) map.get("code"));
        fonds.setName((String) map.get("name"));
        Object remark = map.getOrDefault("remark", null);
        fonds.setRemark(remark == null ? null : remark.toString());
        ArrayList<Integer> associationList = (ArrayList<Integer>) map.get("association");
        fondsService.save(fonds, associationList);
    }

    /**
     * @api {delete} /fonds/{id} 删除全宗
     * @apiName delete
     * @apiGroup fonds
     * @apiParam {Number} id 全宗ID（url占位符）
     * @apiError (Error 400) message 该全宗下存在子全宗.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_fonds_write')")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        fondsService.delete(id);
    }

    /**
     * @api {put} /fonds 修改全宗信息
     * @apiName update
     * @apiGroup fonds
     * @apiParam {Number} id 全宗ID
     * @apiParam {Number} parentId 上级全宗ID（根节点传1）
     * @apiParam {String{20}} code 全宗号
     * @apiParam {String{20}} name 全宗名称
     * @apiParam {String{100}} remark 备注（未输入传""值）
     * @apiParam {Number[]} association 关联机构ID
     * @apiError (Error 400) message 1.全宗号已存在 2.请设置关联机构 3.机构已被其它全宗关联
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_fonds_write')")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody HashMap<String, Object> map) {
        Fonds fonds = new Fonds();
        fonds.setId((int) map.get("id"));
        fonds.setCode((String) map.get("code"));
        fonds.setName((String) map.get("name"));
        Object remark = map.getOrDefault("remark", null);
        fonds.setRemark(remark == null ? null : remark.toString());
        ArrayList<Integer> associationList = (ArrayList<Integer>) map.get("association");
        fondsService.update(fonds, associationList);
    }

    /**
     * @api {get} /fonds/{id} 获取全宗详情
     * @apiName get
     * @apiGroup fonds
     * @apiParam {Number} id 全宗ID（url占位符）
     * @apiSuccess (Success 200) {Number} id 全宗ID.
     * @apiSuccess (Success 200) {String} code 全宗号.
     * @apiSuccess (Success 200) {String} name 全宗名称.
     * @apiSuccess (Success 200) {Number} parentId 上级全宗ID.
     * @apiSuccess (Success 200) {Number[]} association 关联机构ID.
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": 全宗ID,"code": "全宗编码","name": "全宗名称","parentId": 上级全宗ID","association":[1,2,3]}}.
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_fonds_read')")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable("id") int id) {
        return systemQuery.getFondsAndAssociationId(UInteger.valueOf(id));
    }

    /**
     * @api {patch} /fonds/{upId},{downId}/priority 修改全宗排序优先级
     * @apiName priority
     * @apiGroup fonds
     * @apiParam {Number} upId 上移全宗ID（url占位符）
     * @apiParam {Number} downId 下移全宗ID（url占位符）
     * @apiError (Error 400) message 上级全宗不一致.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_fonds_write')")
    @RequestMapping(value = "/{upId},{downId}/priority", method = RequestMethod.PATCH)
    public void priority(@PathVariable("upId") int upId, @PathVariable("downId") int downId) {
        fondsService.priority(upId, downId);
    }
}
