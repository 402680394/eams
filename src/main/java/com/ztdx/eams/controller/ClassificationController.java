package com.ztdx.eams.controller;

import com.ztdx.eams.domain.business.application.ClassificationService;
import com.ztdx.eams.query.BusinessQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * Created by li on 2018/4/18.
 */
@Controller
@RequestMapping(value = "/classification")
public class ClassificationController {

    private final ClassificationService classificationService;

    private final BusinessQuery businessQuery;

    @Autowired
    public ClassificationController(ClassificationService classificationService, BusinessQuery businessQuery) {
        this.classificationService = classificationService;
        this.businessQuery = businessQuery;
    }

    /**
     * @api {get} /classification/list 获取全宗档案分类列表
     * @apiName list
     * @apiGroup classification
     * @apiSuccess (Success 200) {int} id 档案分类ID.
     * @apiSuccess (Success 200) {String} code 档案分类编码.
     * @apiSuccess (Success 200) {String} name 档案分类名称.
     * @apiSuccess (Success 200) {String} retentionPeriod 保管期限.
     * @apiSuccess (Success 200) {int} parentId 上级档案分类ID.
     * @apiSuccess (Success 200) {int} orderNumber 同级排序编号.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccess (Success 200) {arr} subClassification 子档案分类信息
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": 档案分类ID,"code": "档案分类编码","name": "父档案分类0","retentionPeriod": "保管期限","parentId": 上级档案分类ID,"orderNumber": 同级排序编号,"remark": "备注"},
     * {"id": 档案分类ID,"code": "档案分类编码","name": "父档案分类1","retentionPeriod": "保管期限","parentId": 上级档案分类ID,"orderNumber": 同级排序编号,"remark": "备注","subClassification": [
     * {"id": 档案分类ID,"code": "档案分类编码","name": "子档案分类1","retentionPeriod": "保管期限","parentId": 上级档案分类ID,"orderNumber": 同级排序编号,"remark": "备注"}]}]}}.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map<String, Object> list() {
        return businessQuery.getClassificationListMap(UInteger.valueOf(0));
    }
}
