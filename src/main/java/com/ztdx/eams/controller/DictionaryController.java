package com.ztdx.eams.controller;

import com.ztdx.eams.domain.archives.application.DictionaryService;
import com.ztdx.eams.domain.archives.model.Dictionary;
import com.ztdx.eams.query.ArchivesQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by li on 2018/4/21.
 */
@RestController
@RequestMapping(value = "/dictionary")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    private final ArchivesQuery archivesQuery;
    @Autowired
    public DictionaryController(DictionaryService dictionaryService, ArchivesQuery archivesQuery) {
        this.dictionaryService = dictionaryService;
        this.archivesQuery = archivesQuery;
    }

    /**
     * @api {get} /dictionary/list 获取词典列表
     * @apiName list
     * @apiGroup dictionary
     * @apiParam {int} dictionaryClassificationId 词典分类ID(url参数)
     * @apiParam {String} name 词典名称(未输入传""值)(url参数)
     * @apiSuccess (Success 200) {int} id 词典ID.
     * @apiSuccess (Success 200) {String} code 词典编码.
     * @apiSuccess (Success 200) {String} name 词典名称.
     * @apiSuccess (Success 200) {String} classificationName 词典分类名称.
     * @apiSuccess (Success 200) {int} businessLevel 业务级别.
     * @apiSuccess (Success 200) {String} businessExpansion 业务扩展.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": 词典ID,"code": "词典编码","name": "词典名称","classificationName": "词典分类名称","businessLevel": 业务级别,"businessExpansion": "业务扩展","remark": "备注"}]}}.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map<String, Object> list(@RequestParam("dictionaryClassificationId") int dictionaryClassificationId,@RequestParam(value = "name",defaultValue = "") String name) {
        return archivesQuery.getDictionaryList(UInteger.valueOf(dictionaryClassificationId),name);
    }

    /**
     * @api {get} /dictionary/selectList 通过词典分类ID获取词典下拉列表
     * @apiName selectList
     * @apiGroup dictionary
     * @apiParam {int} dictionaryClassificationId 词典分类ID(url参数)
     * @apiSuccess (Success 200) {int} id 词典ID.
     * @apiSuccess (Success 200) {String} code 词典编码.
     * @apiSuccess (Success 200) {String} name 词典名称.
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": 词典ID,"code": "词典编码","name": "词典名称"}]}}.
     */
    @RequestMapping(value = "/selectList", method = RequestMethod.GET)
    public Map<String, Object> selectList(@RequestParam("dictionaryClassificationId") int dictionaryClassificationId) {
        return archivesQuery.getDictionarySelectList(UInteger.valueOf(dictionaryClassificationId));
    }

    /**
     * @api {post} /dictionary 新增词典
     * @apiName save
     * @apiGroup dictionary
     * @apiParam {String} code 词典编码
     * @apiParam {String} name 词典名称
     * @apiParam {int} classificationId 词典分类ID
     * @apiParam {int} businessLevel 业务级别
     * @apiParam {String} businessExpansion 业务扩展（未输入传""值）
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiError (Error 400) message 词典编码已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody Dictionary dictionary) {
        dictionaryService.save(dictionary);
    }

    /**
     * @api {delete} /dictionary/{id} 删除词典
     * @apiName delete
     * @apiGroup dictionary
     * @apiParam {int} id 词典ID（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        dictionaryService.delete(id);
    }

    /**
     * @api {put} /dictionary 修改词典信息
     * @apiName update
     * @apiGroup dictionary
     * @apiParam {int} id 词典ID
     * @apiParam {String} code 词典编码
     * @apiParam {String} name 词典名称
     * @apiParam {int} businessLevel 业务级别
     * @apiParam {String} businessExpansion 业务扩展（未输入传""值）
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiError (Error 400) message 词典编码已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody Dictionary dictionary) {
        dictionaryService.update(dictionary);
    }

    /**
     * @api {get} /dictionary/{id} 获取词典详情
     * @apiName get
     * @apiGroup dictionary
     * @apiParam int id 词典ID（url占位符）
     * @apiSuccess (Success 200) {int} id 词典ID.
     * @apiSuccess (Success 200) {String} code 词典编码.
     * @apiSuccess (Success 200) {String} name 词典名称.
     * @apiSuccess (Success 200) {int} businessLevel 业务级别.
     * @apiSuccess (Success 200) {String} businessExpansion 业务扩展.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": 档案分类ID,"code": "档案分类编码","name": "档案分类名称","retentionPeriod": "保管期限","parentId": 上级档案分类ID,"remark": "备注"}}.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable("id") int id) {
        return archivesQuery.getDictionary(UInteger.valueOf(id));
    }

    /**
     * @api {patch} /dictionary/{upId},{downId}/priority 修改词典排序优先级
     * @apiName priority
     * @apiGroup dictionary
     * @apiParam {int} upId 上移词典ID（url占位符）
     * @apiParam {int} downId 下移词典ID（url占位符）
     */
    @RequestMapping(value = "/{upId},{downId}/priority", method = RequestMethod.PATCH)
    public void priority(@PathVariable("upId") int upId, @PathVariable("downId") int downId) {
        dictionaryService.priority(upId, downId);
    }
}