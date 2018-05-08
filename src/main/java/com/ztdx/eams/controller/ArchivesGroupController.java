package com.ztdx.eams.controller;

import com.ztdx.eams.domain.archives.application.ArchivesGroupService;
import com.ztdx.eams.domain.archives.application.ArchivesService;
import com.ztdx.eams.query.ArchivesQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by li on 2018/5/3.
 */
@RestController
@RequestMapping(value = "/archivesGroup")
public class ArchivesGroupController {

    private final ArchivesQuery archivesQuery;

    private final ArchivesGroupService archivesGroupService;

    @Autowired
    public ArchivesGroupController(ArchivesQuery archivesQuery, ArchivesGroupService archivesGroupService) {
        this.archivesQuery = archivesQuery;
        this.archivesGroupService = archivesGroupService;
    }

    /**
     * @api {get} /archivesGroup/treeList 获取全宗、档案库分组树形列表
     * @apiName treeList
     * @apiGroup archivesGroup
     * @apiSuccess (Success 200) {int} id 全宗ID/档案库分组ID.
     * @apiSuccess (Success 200) {String} code 全宗号.
     * @apiSuccess (Success 200) {String} name 全宗名称/档案库分组名称.
     * @apiSuccess (Success 200) {int} parentId 上级全宗ID/上级档案库分组ID.
     * @apiSuccess (Success 200) {int} orderNumber 排序号.
     * @apiSuccess (Success 200) {int} type 全宗类型
     * @apiSuccess (Success 200) {int} fondsId 所属全宗ID
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccess (Success 200) {arr} subFonds 子全宗信息
     * @apiSuccess (Success 200) {arr} subArchivesGroup 所属档案库分组信息
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": 全宗ID,"code": "全宗号","name": "全宗名称","parentId": 上级全宗ID,"orderNumber": 排序号,"type": 全宗类型,"subArchivesGroup": [
     * {"id": 档案库分组ID,"name": "档案库分组名称","fondsId": 所属全宗ID,"parentId": 上级档案库分组ID,"remark": 备注}],"subFonds": [
     * {"id": 全宗ID,"code": "全宗号","name": "全宗名称","parentId": 上级全宗ID,"orderNumber": 排序号,"type": 全宗类型}]}]}}.
     */
    @RequestMapping(value = "/treeList", method = RequestMethod.GET)
    public Map<String, Object> treeList() {
        return archivesQuery.getArchivesGroupTreeMap();
    }
}
