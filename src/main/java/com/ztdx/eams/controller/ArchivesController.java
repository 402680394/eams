package com.ztdx.eams.controller;

import com.ztdx.eams.domain.archives.application.ArchivesService;
import com.ztdx.eams.domain.archives.model.Archives;
import com.ztdx.eams.domain.archives.model.Structure;
import com.ztdx.eams.query.ArchivesQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/archives")
public class ArchivesController {

    private final ArchivesService archivesService;

    private final ArchivesQuery archivesQuery;

    @Autowired
    public ArchivesController(ArchivesService archivesService, ArchivesQuery archivesQuery) {
        this.archivesService = archivesService;
        this.archivesQuery = archivesQuery;
    }

    /**
     * @api {get} /archives/treeList 获取全宗、档案库分组、登记库、目录树形列表
     * @apiName treeList
     * @apiGroup archives
     * @apiSuccess (Success 200) {String} childrenType 节点类型(1.Fonds-全宗;2.ArchivesGroup-档案分组;3.Archives-档案库;4.Catalogue-目录).
     * @apiSuccess (Success 200) {arr} children 子节点信息
     * @apiSuccess (Success 200) {int} Fonds:id 全宗ID.
     * @apiSuccess (Success 200) {String} Fonds:code 全宗号.
     * @apiSuccess (Success 200) {String} Fonds:name 全宗名称.
     * @apiSuccess (Success 200) {int} Fonds:parentId 上级全宗ID.
     * @apiSuccess (Success 200) {int} Fonds:orderNumber 排序号.
     * @apiSuccess (Success 200) {int} Fonds:type 全宗类型
     * @apiSuccess (Success 200) {int} ArchivesGroup:id 档案库分组ID.
     * @apiSuccess (Success 200) {String} ArchivesGroup:name 档案库分组名称.
     * @apiSuccess (Success 200) {int} ArchivesGroup:parentId 上级档案库分组ID.
     * @apiSuccess (Success 200) {int} ArchivesGroup:fondsId 所属全宗ID
     * @apiSuccess (Success 200) {int} Archives:id 档案库ID
     * @apiSuccess (Success 200) {int} Archives:structure 档案库结构(1 一文一件；2 案卷；3 项目)
     * @apiSuccess (Success 200) {int} Archives:name 档案库名称
     * @apiSuccess (Success 200) {int} Archives:archivesGroupId 所属档案库分组ID
     * @apiSuccess (Success 200) {int} Archives:type 档案库类型(1 登记库； 2 归档库)
     * @apiSuccess (Success 200) {int} Catalogue:id 目录ID
     * @apiSuccess (Success 200) {int} Catalogue:catalogueType 目录类型(1 一文一件/卷内 2 案卷 3 项目）
     * @apiSuccess (Success 200) {int} Catalogue:archivesId 档案库ID
     * @apiSuccess (Success 200) {int} Catalogue:tableName 存储物理表名
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"childrenType": "Fonds","id": 全宗ID,"code": "全宗号","name": "全宗名称","parentId": 上级全宗ID,"orderNumber": 排序号,"type": 全宗类型,"children": [
     * {"childrenType": "ArchivesGroup","id": 档案库分组ID,"name": "档案库分组名称","fondsId": 所属全宗ID,"parentId": 上级档案库分组ID,"children": [
     * {"childrenType": "ArchivesGroup","id": 档案库分组ID,"name": "档案库分组名称","fondsId": 所属全宗ID,"parentId": 上级档案库分组ID},
     * {"childrenType": "Archives","id": 档案库ID,"structure": 档案库结构,"name": "档案库名称","archivesGroupId": 所属档案库分组ID,"type": 档案库类型,"children": [
     * {"childrenType": "Catalogue","id": 目录ID,"catalogueType": 目录类型,"archivesId": 档案库ID,"tableName": "存储物理表名"}]}]},
     * {"childrenType": "Fonds","id": 全宗ID,"code": "全宗号","name": "全宗名称","parentId": 上级全宗ID,"orderNumber": 排序号,"type": 全宗类型}]}]}}.
     */
    @RequestMapping(value = "/treeList", method = RequestMethod.GET)
    public Map<String, Object> treeList() {
        return archivesQuery.getCatalogueTreeMap();
    }
}