package com.ztdx.eams.controller;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.archives.application.ConditionService;
import com.ztdx.eams.domain.archives.model.condition.EntryCondition;
import com.ztdx.eams.query.ArchivesQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/condition")
public class ConditionController {

    private final ConditionService conditionService;

    private final ArchivesQuery archivesQuery;

    /**
     * 构造函数
     */
    @Autowired
    public ConditionController(ConditionService conditionService,ArchivesQuery archivesQuery){
        this.conditionService = conditionService;
        this.archivesQuery = archivesQuery;
    }

    /**
     * @api {get} /condition/entry/columns?cid={cid} 档案库可以搜索的列
     * @apiName entryColumn
     * @apiGroup condition
     * @apiParam {Number} cid 档案库目录id
     * @apiSuccessExample {json} Success-Response:
     * HTTP/1.1 200 OK
     * [
     *     {
     *         "metadataId": 1,
     *         "metadataName":"name",
     *         "displayName": "姓名",
     *         "dataType": 1,
     *         "allowSymbol": [
     *             "equal",
     *             "notEqual"
     *             "greaterThan",
     *             "greaterThanOrEqual",
     *             "lessThan",
     *             "lessThanOrEqual",
     *             "contain",
     *             "notContain"
     *         ]
     *     }
     * ]
     */
    @RequestMapping(value = "/entry/columns",method = RequestMethod.GET)
    public Map<String,Object> entryColumns(@JsonParam Integer catalogueId){
        //double(4) integer(1) date(3) 不能使用包含不包含操作符
        Map<String,Object> resultMap = archivesQuery.getEntryColumns(catalogueId);

        return resultMap;
    }

    /**
     * @api {post} /condition/entry 增加档案库查询条件
     * @apiName saveEntryCondition
     * @apiGroup condition
     * @apiParam {Number} cid 档案库目录id
     * @apiParam {String} name 条件名称
     * @apiParam {String="system","custom"} entryConditionType 条件类型
     * @apiParam {Object[]} conditions 查询条件
     * @apiParam {String="and","or"} [conditions.logical] 逻辑运算符，默认为and
     * @apiParam {String} conditions.column 要搜索的列
     * @apiParam {String} conditions.operator 运算符
     * @apiParam {Object} conditions.value 值，可以嵌套一个conditions
     * @apiParamExample {json} Request-Example:
     * {
     *     "cid": 1,
     *     "name": "测试",
     *     "entryConditionType": "custom",
     *     "conditions": [
     *         {
     *             "column":"age",
     *             "operator": "greaterThan",
     *             "value": 10
     *         },
     *         {
     *             "logical": "and",
     *             "column":"age",
     *             "operator": "lessThanOrEqual",
     *             "value": 30
     *         }
     *     ]
     * }
     * @apiError NameExists 名称已存在
     */
    @RequestMapping(value = "/entry", method = RequestMethod.POST)
    public Object saveEntryCondition(@RequestBody EntryCondition condition, @SessionAttribute UserCredential LOGIN_USER){
        condition.setOwner(LOGIN_USER.getUserId());
        return conditionService.save(condition);
    }

    /**
     * @api {put} /condition/entry/{id} 修改档案库查询条件
     * @apiName updateEntryCondition
     * @apiGroup condition
     * @apiParam {Number} id 条件id
     * @apiParam {String} name 条件名称
     * @apiParam {Object[]} conditions 查询条件
     * @apiParam {String="and","or"} [conditions.logical] 逻辑运算符，默认为and
     * @apiParam {String} conditions.column 要搜索的列
     * @apiParam {String} conditions.operator 运算符
     * @apiParam {Object} conditions.value 值，可以嵌套一个conditions
     * @apiParamExample {json} Request-Example:
     * {
     *     "id":  1,
     *     "name": "测试",
     *     "conditions": [
     *         {
     *             "column":"age",
     *             "operator": "greaterThan",
     *             "value": 10
     *         },
     *         {
     *             "logical": "and",
     *             "column":"age",
     *             "operator": "lessThanOrEqual",
     *             "value": 30
     *         }
     *     ]
     * }
     *
     * @apiError NameExists 名称已存在
     */
    @RequestMapping(value = "/entry",method = RequestMethod.PUT)
    public void updateEntryCondition(@RequestBody EntryCondition condition){
        conditionService.update(condition);
    }

    /**
     * @api {get} /condition/entry?cid={cid} 查询档案库的查询条件
     * @apiName listEntryCondition
     * @apiGroup condition
     * @apiParam {Number} cid  档案库目录id
     * @apiSuccess {Object[]} system 系统条件列表
     * @apiSuccess {Number} system.id 条件id
     * @apiSuccess {String} system.name 条件名称
     * @apiSuccess {Object[]} custom 自定义条件列表，只能看到自己添加的条件
     * @apiSuccess {Number} custom.id 条件id
     * @apiSuccess {String} custom.name 条件名称
     * @apiSuccessExample {json} Success-Response
     * {
     *     "system":[
     *         {
     *             "id": 1,
     *             "name": "系统条件"
     *         }
     *     ],
     *     "custom":[
     *         {
     *             "id": 2,
     *             "name": "自定义条件"
     *         }
     *     ]
     * }
     *
     */
    @RequestMapping(value = "/entry",method = RequestMethod.GET)
    public void listEntryCondition(@JsonParam Integer catalogueId){

    }

    /**
     * @api {get} /condition/entry/{id} 获取档案库的查询条件
     * @apiName getEntryCondition
     * @apiGroup condition
     * @apiParam {Number} id 条件id
     * @apiSuccess {Object[]} content 条件列表
     * @apiSuccess {String} content.name 条件名称
     * @apiSuccess {Object[]} content.conditions 查询条件
     * @apiSuccess {String="and","or"} [content.conditions.logical] 逻辑运算符，默认为and
     * @apiSuccess {String} content.conditions.column 要搜索的列
     * @apiSuccess {String} content.conditions.operator 运算符
     * @apiSuccess {Object} content.conditions.value 值，可以嵌套一个conditions
     * @apiSuccessExample {json} Success-Response
     * [
     *     {
     *         "cid": 1,
     *         "name": "测试",
     *         "conditions": [
     *             {
     *                 "column":"age",
     *                 "operator": "greaterThan",
     *                 "value": 10
     *             },
     *             {
     *                 "logical": "and",
     *                 "column":"age",
     *                 "operator": "lessThanOrEqual",
     *                 "value": 30
     *             }
     *         ]
     *     }
     * ]
     *
     */
    @RequestMapping(value = "/entry/{cid}",method = RequestMethod.GET)
    public EntryCondition getEntryCondition(@PathVariable("cid") int conditionId){
        return conditionService.getEntryCondition(conditionId);
    }
}
