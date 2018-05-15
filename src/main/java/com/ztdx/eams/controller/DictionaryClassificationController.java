package com.ztdx.eams.controller;

import com.ztdx.eams.domain.archives.application.DictionaryClassificationService;
import com.ztdx.eams.domain.archives.model.DictionaryClassification;
import com.ztdx.eams.query.ArchivesQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by li on 2018/4/19.
 */
@RestController
@RequestMapping(value = "/dictionaryClassification")
public class DictionaryClassificationController {

    private final DictionaryClassificationService dictionaryClassificationService;

    private final ArchivesQuery archivesQuery;

    @Autowired
    public DictionaryClassificationController(DictionaryClassificationService dictionaryClassificationService, ArchivesQuery archivesQuery) {
        this.dictionaryClassificationService = dictionaryClassificationService;
        this.archivesQuery = archivesQuery;
    }

    /**
     * @api {get} /dictionaryClassification/treeList 获取全宗、词典分类树形列表
     * @apiName treeList
     * @apiGroup dictionaryClassification
     * @apiSuccess (Success 200) {String} childrenType 节点类型(1.DictionaryClassification-词典分类;2.Fonds-全宗).
     * @apiSuccess (Success 200) {arr} children 子节点信息
     * @apiSuccess (Success 200) {int} DictionaryClassification:id 全宗ID.
     * @apiSuccess (Success 200) {String} DictionaryClassification:code 全宗号.
     * @apiSuccess (Success 200) {String} DictionaryClassification:name 全宗名称.
     * @apiSuccess (Success 200) {int} DictionaryClassification:parentId 上级全宗ID.
     * @apiSuccess (Success 200) {int} DictionaryClassification:orderNumber 排序号.
     * @apiSuccess (Success 200) {int} DictionaryClassification:type 全宗类型.
     * @apiSuccess (Success 200) {int} Fonds:id 词典分类ID.
     * @apiSuccess (Success 200) {String} Fonds:code 词典分类编码.
     * @apiSuccess (Success 200) {String} Fonds:name 词典分类名称.
     * @apiSuccess (Success 200) {String} Fonds:remark 备注.
     * @apiSuccess (Success 200) {int} Fonds:fondsId 所属全宗ID
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"item": [{"childrenType": "DictionaryClassification","id": 词典分类ID,"code": "词典分类编码","name": "词典分类名称","remark": 备注,"fondsId": 所属全宗ID},
     * {"childrenType": "Fonds","id": 全宗ID,"code": "全宗号","name": "全宗名称","parentId": 上级全宗ID,"orderNumber": 排序号,"type": 全宗类型,"children": [
     * {"childrenType": "DictionaryClassification","id": 词典分类ID,"code": "词典分类编码","name": "词典分类名称","remark": 备注,"fondsId": 所属全宗ID},
     * {"childrenType": "Fonds","id": 全宗ID,"code": "全宗号","name": "全宗名称","parentId": 上级全宗ID,"orderNumber": 排序号,"type": 全宗类型}]}]}}
     */
    @RequestMapping(value = "/treeList", method = RequestMethod.GET)
    public Map<String, Object> treeList() {
        return archivesQuery.getDictionaryClassificationTreeMap();
    }

    /**
     * @api {get} /dictionaryClassification/listByFondsId 获取全宗所属词典分类列表
     * @apiName listByFondsId
     * @apiGroup dictionaryClassification
     * @apiParam {int} fondsId 全宗ID（url参数）
     * @apiSuccess (Success 200) {int} id 词典分类ID.
     * @apiSuccess (Success 200) {String} code 词典分类编码.
     * @apiSuccess (Success 200) {String} name 词典分类名称.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccessExample {json} Success-Response:
     * {"data": {items:[{"id": 词典分类ID,"code": "词典分类编码","name": "词典分类名称","remark": 备注}]}}
     */
    @RequestMapping(value = "/listByFonds", method = RequestMethod.GET)
    public Map<String, Object> listByFonds(@RequestParam int fondsId) {
        return archivesQuery.getDictionaryClassificationListByFonds(UInteger.valueOf(fondsId));
    }

    /**
     * @api {post} /dictionaryClassification 新增词典分类
     * @apiName save
     * @apiGroup dictionaryClassification
     * @apiParam {int} fondsId 所属全宗ID（全局传1）
     * @apiParam {String} code 词典分类编码
     * @apiParam {String} name 词典分类名称
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiError (Error 400) message 词典分类编码已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody DictionaryClassification dictionaryClassification) {
        dictionaryClassificationService.save(dictionaryClassification);
    }

    /**
     * @api {delete} /dictionaryClassification/{id} 删除词典分类
     * @apiName delete
     * @apiGroup dictionaryClassification
     * @apiParam {int} id 词典分类ID（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        dictionaryClassificationService.delete(id);
    }

    /**
     * @api {put} /dictionaryClassification 修改词典分类信息
     * @apiName update
     * @apiGroup dictionaryClassification
     * @apiParam {int} id 词典分类ID
     * @apiParam {int} fondsId 所属全宗ID（全局传1）
     * @apiParam {String} code 词典分类编码
     * @apiParam {String} name 词典分类名称
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiError (Error 400) message 词典分类编码已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody DictionaryClassification dictionaryClassification) {
        dictionaryClassificationService.update(dictionaryClassification);
    }

    /**
     * @api {get} /dictionaryClassification/{id} 获取词典分类详情
     * @apiName get
     * @apiGroup dictionaryClassification
     * @apiParam {int} id 词典分类ID（url占位符）
     * @apiSuccess (Success 200) {int} id 词典分类ID.
     * @apiSuccess (Success 200) {String} code 词典分类编码.
     * @apiSuccess (Success 200) {String} name 词典分类名称.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": 词典分类ID,"code": "词典分类编码","name": "词典分类名称","remark": 备注}}.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable("id") int id) {
        return archivesQuery.getDictionaryClassification(UInteger.valueOf(id));
    }
}
