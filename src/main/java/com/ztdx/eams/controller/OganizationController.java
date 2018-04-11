package com.ztdx.eams.controller;

import com.ztdx.eams.domain.system.application.OganizationService;
import com.ztdx.eams.query.SystemQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * Created by li on 2018/4/11.
 */
@RestController
@RequestMapping(value = "/oganization")
public class OganizationController {

    private final OganizationService oganizationService;

    private final SystemQuery systemQuery;

    @Autowired
    public OganizationController(OganizationService oganizationService, SystemQuery systemQuery) {
        this.oganizationService = oganizationService;
        this.systemQuery = systemQuery;
    }

    /**
     * @api {get} /oganization/{id}/lowerlist 下级机构列表
     * @apiName getLowerList
     * @apiGroup oganization
     * @apiParam {int} id 机构ID（根机构传0）
     * @apiSuccess (Success 200) {int} id 机构ID.
     * @apiSuccess (Success 200) {String} code 机构编码.
     * @apiSuccess (Success 200) {String} name 机构名称.
     * @apiSuccess (Success 200) {int} parentId 上级机构ID.
     * @apiSuccess (Success 200) {int} orderNumber 同级排序编号.
     * @apiSuccess (Success 200) {int} type 机构类型
     * @apiSuccessExample {json} Success-Response:
     *     {
     *     "data":{
     *              "items":[{"id": 机构ID,"code": "机构编码","name": "机构名称","parentId": 上级机构ID,"orderNumber": 同级排序编号,"type": 机构类型}
     *                      {"id": 机构ID,"code": "机构编码","name": "机构名称","parentId": 上级机构ID,"orderNumber": 同级排序编号,"type": 机构类型}]}
     *     }.
     * @apiError (Error 401) message 未登录.
     * @apiUse ErrorExample
     */
     @RequestMapping(value = "/{id}/lowerlist", method = RequestMethod.GET)
     public HashMap<String,Object> getLowerList(@PathVariable("id") int orgId){
         return systemQuery.getLowerList(orgId);
     }
}
