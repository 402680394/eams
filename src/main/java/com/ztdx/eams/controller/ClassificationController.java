package com.ztdx.eams.controller;

import com.ztdx.eams.domain.business.application.ClassificationService;
import com.ztdx.eams.domain.business.model.Classification;
import com.ztdx.eams.query.BusinessQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by li on 2018/4/18.
 */
@RestController
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
     * @api {get} /classification/{fondsId}/list 获取全宗下档案分类列表
     * @apiName list
     * @apiGroup classification
     * @apiParam {int} fondsId 所属全宗ID(全局ID值为0)
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
    @RequestMapping(value = "/{fondsId}/list", method = RequestMethod.GET)
    public Map<String, Object> list(@PathVariable("fondsId") int fondsId) {
        return businessQuery.getAllClassificationTreeMap(UInteger.valueOf(fondsId));
    }

    /**
     * @api {post} /classification 新增档案分类
     * @apiName save
     * @apiGroup classification
     * @apiParam {int} parentId 上级档案分类ID（根节点传0）
     * @apiParam {String} code 档案分类编码
     * @apiParam {String} name 档案分类名称
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiParam {String} retentionPeriod 保管期限
     * @apiError (Error 400) message 档案分类编码已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody Classification classification) {
        classificationService.save(classification);
    }

    /**
     * @api {delete} /classification/{id} 删除档案分类
     * @apiName delete
     * @apiGroup classification
     * @apiParam {int} id 全宗ID（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        classificationService.delete(id);
    }

    /**
     * @api {put} /classification 修改档案分类信息
     * @apiName update
     * @apiGroup classification
     * @apiParam {int} id 档案分类ID
     * @apiParam {int} parentId 上级档案分类ID（根节点传0）
     * @apiParam {String} code 档案分类编码
     * @apiParam {String} name 档案分类名称
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiParam {String} retentionPeriod 保管期限
     * @apiError (Error 400) message 档案分类编码已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody Classification classification) {
        classificationService.update(classification);
    }

    /**
     * @api {get} /classification/{id} 获取档案分类详情
     * @apiName get
     * @apiGroup classification
     * @apiParam {int} id 档案分类ID（url占位符）
     * @apiSuccess (Success 200) {int} id 档案分类ID.
     * @apiSuccess (Success 200) {String} code 档案分类编码.
     * @apiSuccess (Success 200) {String} name 档案分类名称.
     * @apiSuccess (Success 200) {String} retentionPeriod 保管期限.
     * @apiSuccess (Success 200) {int} parentId 上级档案分类ID.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": 档案分类ID,"code": "档案分类编码","name": "档案分类名称","retentionPeriod": "保管期限","parentId": 上级档案分类ID,"remark": "备注"}}.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable("id") int id) {
        return businessQuery.getClassification(UInteger.valueOf(id));
    }

    /**
     * @api {patch} /classification/{upId},{downId}/priority 修改档案分类排序优先级
     * @apiName priority
     * @apiGroup classification
     * @apiParam {int} upId 上移档案分类ID（url占位符）
     * @apiParam {int} downId 下移档案分类ID（url占位符）
     * @apiError (Error 400) message 上级档案分类不一致.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/{upId},{downId}/priority", method = RequestMethod.PATCH)
    public void priority(@PathVariable("upId") int upId, @PathVariable("downId") int downId) {
        classificationService.priority(upId, downId);
    }
}
