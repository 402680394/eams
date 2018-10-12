package com.ztdx.eams.controller.archives;

import com.ztdx.eams.domain.archives.application.ArchivesGroupService;
import com.ztdx.eams.domain.archives.model.ArchivesGroup;
import com.ztdx.eams.domain.archives.model.Classification;
import com.ztdx.eams.query.ArchivesQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * @api {post} /archivesGroup 新增档案库分组
     * @apiName save
     * @apiGroup archivesGroup
     * @apiParam {Number} parentId 上级档案库分组ID（根节点传1）
     * @apiParam {String} name 档案库分组名称
     * @apiParam {String} fondsId 所属全宗ID
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiError (Error 400) message 档案库分组名称已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody ArchivesGroup archivesGroup) {
        archivesGroupService.save(archivesGroup);
    }

    /**
     * @api {delete} /archivesGroup/{id} 删除档案库分组
     * @apiName delete
     * @apiGroup archivesGroup
     * @apiParam {Number} id 档案库分组ID（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        archivesGroupService.delete(id);
    }

    /**
     * @api {put} /archivesGroup 修改档案库分组
     * @apiName update
     * @apiGroup archivesGroup
     * @apiParam {Number} id 档案库分组ID
     * @apiParam {Number} parentId 上级档案库分组ID（根节点传0）
     * @apiParam {String{30}} name 档案库分组名称
     * @apiParam {String{100}} remark 备注（未输入传""值）
     * @apiError (Error 400) message 档案库分组名称已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody ArchivesGroup archivesGroup) {
        archivesGroupService.update(archivesGroup);
    }

    /**
     * @api {get} /archivesGroup/treeList 获取全宗所属档案库分组树
     * @apiName treeList
     * @apiGroup archivesGroup
     * @apiParam {Number} fondsId 所属全宗ID(url参数)
     * @apiSuccess (Success 200) {Number} id 档案库分组ID.
     * @apiSuccess (Success 200) {String} name 案库分组名称.
     * @apiSuccess (Success 200) {Number} parentId 上级档案库分组ID.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccess (Success 200) {Object[]} children 子节点信息
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": 2,"name": "文书档案","parentId": 1,"remark": "备注"},
     * {"id": 3,"name": "金石档案","parentId": 1,"remark": "备注""children": [
     * {"id": 4,"name": "石碑档案","parentId": 3,"remark": "备注"}]}]}}.
     */
    @RequestMapping(value = "/treeList", method = RequestMethod.GET)
    public Map<String, Object> treeList(@RequestParam("fondsId") int fondsId) {
        return archivesQuery.getArchivesGroupTreeMap(UInteger.valueOf(fondsId));
    }

    /**
     * @api {get} /archivesGroup/treeListForUpdate 获取全宗所属档案库分组树（编辑时下拉树）
     * @apiName treeListForUpdate
     * @apiGroup archivesGroup
     * @apiParam {Number} fondsId 所属全宗ID(url参数)
     * @apiParam {Number} archivesGroupId 当前档案库分组ID(url参数)
     * @apiSuccess (Success 200) {Number} id 档案库分组ID.
     * @apiSuccess (Success 200) {String} name 案库分组名称.
     * @apiSuccess (Success 200) {Number} parentId 上级档案库分组ID.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccess (Success 200) {Object[]} children 子节点信息
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": 2,"name": "文书档案","parentId": 1,"remark": "备注"},
     * {"id": 3,"name": "金石档案","parentId": 1,"remark": "备注""children": [
     * {"id": 4,"name": "石碑档案","parentId": 3,"remark": "备注"}]}]}}.
     */
    @RequestMapping(value = "/treeListForUpdate", method = RequestMethod.GET)
    public Map<String, Object> treeListForUpdate(@RequestParam("fondsId") int fondsId, @RequestParam("archivesGroupId") int archivesGroupId) {
        return archivesQuery.getArchivesGroupTreeMapForUpdate(UInteger.valueOf(fondsId), UInteger.valueOf(archivesGroupId));
    }
}
