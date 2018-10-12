package com.ztdx.eams.controller.system;

import com.ztdx.eams.domain.system.application.OrganizationService;
import com.ztdx.eams.domain.system.model.Organization;
import com.ztdx.eams.query.SystemQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by li on 2018/4/11.
 */
@RestController
@RequestMapping(value = "/organization")
public class OrganizationController {

    private final OrganizationService organizationService;

    private final SystemQuery systemQuery;

    @Autowired
    public OrganizationController(OrganizationService organizationService, SystemQuery systemQuery) {
        this.organizationService = organizationService;
        this.systemQuery = systemQuery;
    }

    /**
     * @api {get} /organization/treeList 通过上级机构节点与机构类型获取下级机构树
     * @apiName treeList
     * @apiGroup organization
     * @apiParam {Number} id 上级机构ID（url参数）（非必需，默认为1根机构节点）
     * @apiParam {Number} type 机构类型（url参数）（非必需，默认为0）（可选值：0-获取全部 1-获取类型为公司的机构）
     * @apiSuccess (Success 200) {Number} id 机构ID.
     * @apiSuccess (Success 200) {String} code 机构编码.
     * @apiSuccess (Success 200) {String} name 机构名称.
     * @apiSuccess (Success 200) {Number} parentId 上级机构ID.
     * @apiSuccess (Success 200) {Number} orderNumber 同级排序编号.
     * @apiSuccess (Success 200) {Number} fondsId 关联全宗ID(未关联为null).
     * @apiSuccess (Success 200) {Number} type 机构类型
     * @apiSuccess (Success 200) {Object[]} children 子节点信息
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": 机构ID,"code": "机构编码","name": "父机构0","parentId": 上级机构ID,"orderNumber": 同级排序编号,"fondsId": 关联全宗ID,"type": 机构类型},
     * {"id": 机构ID,"code": "机构编码","name": "父机构1","parentId": 上级机构ID,"orderNumber": 同级排序编号,"fondsId": 关联全宗ID,"type": 机构类型,"children": [
     * {"id": 机构ID,"code": "机构编码","name": "子机构1","parentId": 上级机构ID,"orderNumber": 同级排序编号,"fondsId": 关联全宗ID,"type": 机构类型}]}]}}.
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_organization_read', 'global_role_user_set')")
    @RequestMapping(value = "/treeList", method = RequestMethod.GET)
    public Map<String, Object> treeList(@RequestParam(required = false, defaultValue = "1", name = "id") int id, @RequestParam(name = "type", required = false, defaultValue = "0") int type) {
        return systemQuery.getOrganizationTreeMap(id, type);
    }

    /**
     * @api {get} /organization/treeListByFonds 获取全宗关联的组织机构树
     * @apiName treeListByFonds
     * @apiGroup organization
     * @apiParam {Number} fondsId 关联全宗ID（url参数）
     * @apiSuccess (Success 200) {Number} id 机构ID.
     * @apiSuccess (Success 200) {String} code 机构编码.
     * @apiSuccess (Success 200) {String} name 机构名称.
     * @apiSuccess (Success 200) {Number} parentId 上级机构ID.
     * @apiSuccess (Success 200) {Number} orderNumber 同级排序编号.
     * @apiSuccess (Success 200) {Number} fondsId 关联全宗ID(未关联为null).
     * @apiSuccess (Success 200) {Number} type 机构类型
     * @apiSuccess (Success 200) {Object[]} children 子节点信息
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": 机构ID,"code": "机构编码","name": "父机构0","parentId": 上级机构ID,"orderNumber": 同级排序编号,"fondsId": 关联全宗ID,"type": 机构类型},
     * {"id": 机构ID,"code": "机构编码","name": "父机构1","parentId": 上级机构ID,"orderNumber": 同级排序编号,"fondsId": 关联全宗ID,"type": 机构类型,"children": [
     * {"id": 机构ID,"code": "机构编码","name": "子机构1","parentId": 上级机构ID,"orderNumber": 同级排序编号,"fondsId": 关联全宗ID,"type": 机构类型}]}]}}.
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_organization_read', 'fonds_role_user_set_' + #fondsId)")
    @RequestMapping(value = "/treeListByFonds", method = RequestMethod.GET)
    public Map<String, Object> treeListByFonds(@RequestParam("fondsId") int fondsId) {
        return systemQuery.getOrganizationTreeMapByFondsId(UInteger.valueOf(fondsId));
    }

    /**
     * @api {post} /organization 新增机构
     * @apiName save
     * @apiGroup organization
     * @apiParam {Number} parentId 上级机构ID（根机构传1）
     * @apiParam {String{20}} code 机构编码
     * @apiParam {String{50}} name 机构名称
     * @apiParam {String{100}} remark 备注（未输入传""值）
     * @apiParam {Number} type 机构类型（可选值：1-公司；2-部门；3-科室）
     * @apiError (Error 400) message 1.机构编码已存在；2.部门与科室下无法创建公司；3.根节点无法创建部门与科室；4.科室下无法创建部门.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_organization_write')")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody Organization organization) {
        organizationService.save(organization);
    }

    /**
     * @api {delete} /organization/{id} 删除机构
     * @apiName delete
     * @apiGroup organization
     * @apiParam {Number} id 机构ID（url占位符）
     * @apiError (Error 400) message 1.该机构或子机构下存在用户；2.该机构下存在子机构.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_organization_write')")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        organizationService.delete(id);
    }

    /**
     * @api {put} /organization 修改机构信息
     * @apiName update
     * @apiGroup organization
     * @apiParam {Number} id 机构ID
     * @apiParam {Number} parentId 上级机构ID（根机构传1）
     * @apiParam {String{20}} code 机构编码
     * @apiParam {String{50}} name 机构名称
     * @apiParam {String{100}} remark 备注（未输入传""值）
     * @apiParam {Number} type 机构类型（可选值：1-公司；2-部门；3-科室）
     * @apiError (Error 400) message 1.机构编码已存在；2.部门与科室下无法创建公司；3.根节点无法创建部门与科室 4.机构不存在或已被删除.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_organization_write')")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody Organization organization) {
        organizationService.update(organization);
    }

    /**
     * @api {get} /organization/{id} 获取机构详情
     * @apiName get
     * @apiGroup organization
     * @apiParam {Number} id 机构ID（url占位符）
     * @apiSuccess (Success 200) {Number} id 机构ID.
     * @apiSuccess (Success 200) {String} code 机构编码.
     * @apiSuccess (Success 200) {String} name 机构名称.
     * @apiSuccess (Success 200) {Number} parentId 上级机构ID.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccess (Success 200) {Number} type 机构类型（可选值：1-公司；2-部门；3-科室）
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": 机构ID,"code": "机构编码","name": "机构名称","parentId": 上级机构ID,"type": 机构类型,"remark": "备注"}}.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable("id") int id) {
        return systemQuery.getOrganization(UInteger.valueOf(id));
    }

    /**
     * @api {put} /organization/{upId},{downId}/priority 修改机构排序优先级
     * @apiName priority
     * @apiGroup organization
     * @apiParam {Number} upId 上移机构ID（url占位符）
     * @apiParam {Number} downId 下移机构ID（url占位符）
     * @apiError (Error 400) message 1.机构类型或上级机构不一致 2.机构不存在或已被删除.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_organization_write')")
    @RequestMapping(value = "/{upId},{downId}/priority", method = RequestMethod.PUT)
    public void priority(@PathVariable("upId") int upId, @PathVariable("downId") int downId) {
        organizationService.priority(upId, downId);
    }
}
