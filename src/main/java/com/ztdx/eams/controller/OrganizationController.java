package com.ztdx.eams.controller;

import com.ztdx.eams.domain.system.application.OrganizationService;
import com.ztdx.eams.domain.system.model.Organization;
import com.ztdx.eams.query.SystemQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by li on 2018/4/11.
 */
@RestController
@RequestMapping(value = "/oganization")
public class OrganizationController {

    private final OrganizationService organizationService;

    private final SystemQuery systemQuery;

    @Autowired
    public OrganizationController(OrganizationService organizationService, SystemQuery systemQuery) {
        this.organizationService = organizationService;
        this.systemQuery = systemQuery;
    }

    /**
     * @api {get} /organization/treeList 获取机构树形列表
     * @apiName treeList
     * @apiGroup organization
     * @apiSuccess (Success 200) {int} id 机构ID.
     * @apiSuccess (Success 200) {String} code 机构编码.
     * @apiSuccess (Success 200) {String} name 机构名称.
     * @apiSuccess (Success 200) {int} parentId 上级机构ID.
     * @apiSuccess (Success 200) {int} orderNumber 同级排序编号.
     * @apiSuccess (Success 200) {int} type 机构类型
     * @apiSuccess (Success 200) {arr} subOrg 子机构信息
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": 机构ID,"code": "机构编码","name": "父机构0","parentId": 上级机构ID,"orderNumber": 同级排序编号,"type": 机构类型},
     * {"id": 机构ID,"code": "机构编码","name": "父机构1","parentId": 上级机构ID,"orderNumber": 同级排序编号,"type": 机构类型,"subOrg": [
     * {"id": 机构ID,"code": "机构编码","name": "子机构1","parentId": 上级机构ID,"orderNumber": 同级排序编号,"type": 机构类型}]}]}}.
     */
    @RequestMapping(value = "/treeList", method = RequestMethod.GET)
    public Map<String, Object> treeList() {
        return systemQuery.getOrganizationTreeMap();
    }

    /**
     * @api {post} /organization 新增机构
     * @apiName save
     * @apiGroup organization
     * @apiParam {int} parentId 上级机构ID（根机构传0）
     * @apiParam {String} code 机构编码
     * @apiParam {String} name 机构名称
     * @apiParam {String} describe 机构描述（未输入传""值）
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiParam {int} type 机构类型（1-公司；2-部门；3-科室）
     * @apiError (Error 400) message 1.机构编码已存在；2.部门与科室下无法创建公司；3.根节点无法创建部门与科室；4.科室下无法创建部门.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody Organization organization) {
        organizationService.save(organization);
    }

    /**
     * @api {delete} /organization/{id} 删除机构
     * @apiName delete
     * @apiGroup organization
     * @apiParam {int} id 机构ID（url占位符）
     * @apiError (Error 400) message 该机构或子机构下存在用户.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        organizationService.delete(id);
    }

    /**
     * @api {put} /organization 修改机构信息
     * @apiName update
     * @apiGroup organization
     * @apiParam {int} id 机构ID
     * @apiParam {int} parentId 上级机构ID（根机构传0）
     * @apiParam {String} code 机构编码
     * @apiParam {String} name 机构名称
     * @apiParam {String} depict 机构描述（未输入传""值）
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiParam {int} type 机构类型（可选值：1-公司；2-部门；3-科室）
     * @apiError (Error 400) message 1.机构编码已存在；2.部门与科室下无法创建公司；3.根节点无法创建部门与科室 4.机构不存在或已被删除.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody Organization organization) {
        organizationService.update(organization);
    }

    /**
     * @api {get} /organization/{id} 获取机构详情
     * @apiName get
     * @apiGroup organization
     * @apiParam {int} id 机构ID（url占位符）
     * @apiSuccess (Success 200) {int} id 机构ID.
     * @apiSuccess (Success 200) {String} code 机构编码.
     * @apiSuccess (Success 200) {String} name 机构名称.
     * @apiSuccess (Success 200) {int} parentId 上级机构ID.
     * @apiSuccess (Success 200) {int} type 机构类型
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": 机构ID,"code": "机构编码","name": "机构名称","parentId": 上级机构ID,"type": 机构类型}}.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable("id") int id) {
        return systemQuery.getOrganization(UInteger.valueOf(id));
    }

    /**
     * @api {patch} /organization/{upId},{downId}/priority 修改机构排序优先级
     * @apiName priority
     * @apiGroup organization
     * @apiParam {int} upId 上移机构ID（url占位符）
     * @apiParam {int} downId 下移机构ID（url占位符）
     * @apiError (Error 400) message 1.机构类型或上级机构不一致 2.机构不存在或已被删除.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/{upId},{downId}/priority", method = RequestMethod.PATCH)
    public void priority(@PathVariable("upId") int upId, @PathVariable("downId") int downId) {
        organizationService.priority(upId, downId);
    }
}
