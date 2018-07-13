package com.ztdx.eams.controller.store;

import com.ztdx.eams.query.ArchivesQuery;
import com.ztdx.eams.query.StoreQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by li on 2018/7/11.
 */
@RestController
@RequestMapping(value = "boxCodeRule")
public class BoxCodeRuleController {

    private final StoreQuery storeQuery;

    private final ArchivesQuery archivesQuery;

    @Autowired
    public BoxCodeRuleController(StoreQuery storeQuery, ArchivesQuery archivesQuery) {
        this.storeQuery = storeQuery;
        this.archivesQuery = archivesQuery;
    }

    /**
     * @api {get} /boxCodeRule/ruleApply 通过档案库id查询盒号规则应用信息
     * @apiName list
     * @apiGroup boxCodeRule
     * @apiParam {Number} archivesId 所属库ID(url参数)
     * @apiSuccess (Success 200) {Number} type 规则类型（1-著录项值 2-著录项值对应编码 3-档案库所属全宗号 4-固定值 5-流水号）.
     * @apiSuccess (Success 200) {String} name 名称.
     * @apiSuccess (Success 200) {String} value 值.
     * @apiSuccess (Success 200) {Number} isDictionary 是否使用字典(0 否 1 是).
     * @apiSuccess (Success 200) {Number} dictionaryType 字典类型(1 目录字典 2 档案分类 3 组织机构）.
     * @apiSuccess (Success 200) {Number} dictionaryNodeId 字典节点标识（字典类型为目录字典时为字典分类ID，字典类型为档案分类时为上级档案分类节点ID，字典类型为目录字典时为上级组织机构节点ID）.
     * @apiSuccess (Success 200) {Number} dictionaryRootSelect 字典节点标识字典中，是否根节点可选(当字典类型为“档案分类”、“组织结构”时有效)(可选值：0-否 1-是).
     * @apiSuccess (Success 200) {Number} interceptionLength 截取长度.
     * @apiSuccess (Success 200) {Number} flowNumberLength 流水号长度.
     * @apiSuccess (Success 200) {Number} orderNumber 排序号.
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"code": 盒号,"filesTotal": 文件份数,"pagesTotal": 文件页数,"maxPagesTotal": 容纳最大页数,"width": 盒子宽度,"onStand": 是否在架,"point": "位置编码","remark": "备注"}]}}.
     */
    @RequestMapping(value = "/ruleApply", method = RequestMethod.GET)
    public Map<String, Object> get(@RequestParam("archivesId") int archivesId) {
        List<Map<String, Object>> descriptionItems = (List<Map<String, Object>>) archivesQuery.getDescriptionItemList(UInteger.valueOf(archivesId)).get("items");
        return storeQuery.ruleApply(UInteger.valueOf(archivesId), descriptionItems);
    }
}
