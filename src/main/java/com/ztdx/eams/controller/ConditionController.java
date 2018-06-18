package com.ztdx.eams.controller;

import com.ztdx.eams.domain.archives.model.condition.EntryCondition;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/condition")
public class ConditionController {

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
    public void entryColumns(){

    }

    /**
     * @api {post} /condition/entry 增加档案库查询条件
     * @apiName saveEntryCondition
     * @apiGroup condition
     * @apiParam {Number} cid 档案库目录id
     * @apiParam {String} name 条件名称
     * @apiParam {Object[]} conditions 查询条件
     * @apiParam {String="and","or"} [conditions.logical] 逻辑运算符，默认为and
     * @apiParam {String} conditions.column 要搜索的列
     * @apiParam {String} conditions.operator 运算符
     * @apiParam {Object} conditions.value 值，可以嵌套一个conditions
     * @apiParamExample {json} Request-Example:
     * {
     *     "cid": 1,
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
     * @apiError NameExists 名称已存在
     */
    @RequestMapping(value = "/entry", method = RequestMethod.POST)
    public Object saveEntryCondition(@RequestBody EntryCondition condition){
        return condition;
    }

    /**
     * @api {put} /condition/entry/{id} 修改档案库查询条件
     * @apiName updateEntryCondition
     * @apiGroup condition
     * @apiParam {Number} id 条件id
     * @apiParam {Number} cid 档案库目录id
     * @apiParam {String} name 条件名称
     * @apiParam {Object[]} conditions 查询条件
     * @apiParam {String="and","or"} [conditions.logical] 逻辑运算符，默认为and
     * @apiParam {String} conditions.column 要搜索的列
     * @apiParam {String} conditions.operator 运算符
     * @apiParam {Object} conditions.value 值，可以嵌套一个conditions
     * @apiParamExample {json} Request-Example:
     * {
     *     "cid": 1,
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
    public void updateEntryCondition(){

    }

    /**
     * @api {get} /condition/entry?cid={cid} 查询档案库的查询条件
     * @apiName listEntryCondition
     * @apiGroup condition
     * @apiParam {Number} id 条件id
     * @apiSuccess {Number} id 条件id
     * @apiSuccess {String} name 条件名称
     * @apiSuccess {Object[]} conditions 查询条件
     * @apiSuccess {String="and","or"} [conditions.logical] 逻辑运算符，默认为and
     * @apiSuccess {String} conditions.column 要搜索的列
     * @apiSuccess {String} conditions.operator 运算符
     * @apiSuccess {Object} conditions.value 值，可以嵌套一个conditions
     * @apiSuccessExample {json} Success-Response
     * {
     *     "id": 1,
     *     "cid": 1,
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
     */
    public void listEntryCondition(){

    }

    /**
     * @api {get} /condition/entry/{id} 获取档案库的查询条件
     * @apiName getEntryCondition
     * @apiGroup condition
     * @apiParam {Number} cid 档案库目录id
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
    public void getEntryCondition(){

    }
}
