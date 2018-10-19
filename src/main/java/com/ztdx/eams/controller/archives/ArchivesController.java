package com.ztdx.eams.controller.archives;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.basic.exception.ForbiddenException;
import com.ztdx.eams.domain.archives.application.ArchivesService;
import com.ztdx.eams.domain.archives.application.task.EntryAsyncTask;
import com.ztdx.eams.domain.archives.model.Archives;
import com.ztdx.eams.domain.system.application.PermissionService;
import com.ztdx.eams.domain.system.application.RoleService;
import com.ztdx.eams.query.ArchivesQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/archives")
public class ArchivesController {

    private final ArchivesService archivesService;

    private final ArchivesQuery archivesQuery;

    private final RoleService roleService;

    private final PermissionService permissionService;

    private final EntryAsyncTask entryAsyncTask;

    @Autowired
    public ArchivesController(ArchivesService archivesService, ArchivesQuery archivesQuery, RoleService roleService, PermissionService permissionService, EntryAsyncTask entryAsyncTask) {
        this.archivesService = archivesService;
        this.archivesQuery = archivesQuery;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.entryAsyncTask = entryAsyncTask;
    }

    /**
     * @api {get} /archives/treeList?archiveType={archiveType} 获取全宗、库分组、库、目录树
     * @apiName treeList
     * @apiGroup archives
     * @apiParam {Number} archiveType 档案库类型 0 全部类型 1 登记库(默认值) 2 归档库
     * @apiSuccess (Success 200) {String} childrenType 节点类型(Fonds-全宗;ArchivesGroup-档案分组;Archives-档案库;Catalogue-目录).
     * @apiSuccess (Success 200) {Object[]} children 子节点信息
     * @apiSuccess (Success 200) {Number} Fonds:id 全宗ID.
     * @apiSuccess (Success 200) {String} Fonds:code 全宗号.
     * @apiSuccess (Success 200) {String} Fonds:name 全宗名称.
     * @apiSuccess (Success 200) {Number} Fonds:parentId 上级全宗ID.
     * @apiSuccess (Success 200) {Number} Fonds:orderNumber 排序号.
     * @apiSuccess (Success 200) {Number} Fonds:type 全宗类型
     * @apiSuccess (Success 200) {Number} ArchivesGroup:id 档案库分组ID.
     * @apiSuccess (Success 200) {String} ArchivesGroup:name 档案库分组名称.
     * @apiSuccess (Success 200) {Number} ArchivesGroup:parentId 上级档案库分组ID.
     * @apiSuccess (Success 200) {Number} ArchivesGroup:fondsId 所属全宗ID
     * @apiSuccess (Success 200) {Number} Archives:id 档案库ID
     * @apiSuccess (Success 200) {Number} Archives:structure 档案库结构(1 一文一件；2 案卷；3 项目)
     * @apiSuccess (Success 200) {Number} Archives:name 档案库名称
     * @apiSuccess (Success 200) {Number} Archives:archivesGroupId 所属档案库分组ID
     * @apiSuccess (Success 200) {Number} Archives:type 档案库类型(1 登记库； 2 归档库)
     * @apiSuccess (Success 200) {Number} Catalogue:id 目录ID
     * @apiSuccess (Success 200) {Number} Catalogue:catalogueType 目录类型(1 一文一件/卷内 2 案卷 3 项目）
     * @apiSuccess (Success 200) {Number} Catalogue:archivesId 档案库ID
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"childrenType": "Fonds","id": 全宗ID,"code": "全宗号","name": "全宗名称","parentId": 上级全宗ID,"orderNumber": 排序号,"type": 全宗类型,"children": [
     * {"childrenType": "ArchivesGroup","id": 档案库分组ID,"name": "档案库分组名称","fondsId": 所属全宗ID,"parentId": 上级档案库分组ID,"children": [
     * {"childrenType": "ArchivesGroup","id": 档案库分组ID,"name": "档案库分组名称","fondsId": 所属全宗ID,"parentId": 上级档案库分组ID},
     * {"childrenType": "Archives","id": 档案库ID,"structure": 档案库结构,"name": "档案库名称","archivesGroupId": 所属档案库分组ID,"type": 档案库类型,"children": [
     * {"childrenType": "Catalogue","id": 目录ID,"catalogueType": 目录类型,"archivesId": 档案库ID}]}]},
     * {"childrenType": "Fonds","id": 全宗ID,"code": "全宗号","name": "全宗名称","parentId": 上级全宗ID,"orderNumber": 排序号,"type": 全宗类型}]}]}}.
     */
    @RequestMapping(value = "/treeList", method = RequestMethod.GET)
    public Map<String, Object> treeList(
            @SessionAttribute(required = false) UserCredential LOGIN_USER
            , @RequestParam(value = "archiveType", defaultValue = "1") int archiveType) {
        int userId;
        if (LOGIN_USER != null) {
            userId = LOGIN_USER.getUserId();
        } else {
            throw new ForbiddenException("拒绝访问");
        }
        //可以管理的全宗
        Set<Integer> fondsIds = roleService.findUserManageFonds(userId);
        Set<Integer> catalogueIds = roleService.findUserManageArchiveCatalogue(userId);

        return archivesQuery.getFondsToCatalogueTreeMap(
                archiveType
                , a -> hasPermission(fondsIds, a)
                , a -> hasPermission(catalogueIds, a));
    }

    private boolean hasPermission(Set<Integer> ids, int id) {
        if (permissionService.hasAnyAuthority("ROLE_ADMIN")) {
            return true;
        } else {
            return ids.contains(id);
        }

    }

    /**
     * @api {get} /archives/treeListBelowFonds?id={id} 通过目录ID获取同属全宗下库分组、库树
     * @apiName treeListBelowFonds
     * @apiGroup archives
     * @apiParam {Number} id 目录ID
     * @apiParam {Number} archiveType 库类型 0 全部类型 1 登记库 2 归档库(默认值)
     * @apiSuccess (Success 200) {String} childrenType 节点类型(ArchivesGroup-档案分组;Archives-档案库;Catalogue-目录).
     * @apiSuccess (Success 200) {Object[]} children 子节点信息
     * @apiSuccess (Success 200) {Number} ArchivesGroup:id 档案库分组ID.
     * @apiSuccess (Success 200) {String} ArchivesGroup:name 档案库分组名称.
     * @apiSuccess (Success 200) {Number} ArchivesGroup:parentId 上级档案库分组ID.
     * @apiSuccess (Success 200) {Number} ArchivesGroup:fondsId 所属全宗ID
     * @apiSuccess (Success 200) {Number} Archives:id 档案库ID
     * @apiSuccess (Success 200) {Number} Archives:structure 档案库结构(1 一文一件；2 案卷；3 项目)
     * @apiSuccess (Success 200) {Number} Archives:name 档案库名称
     * @apiSuccess (Success 200) {Number} Archives:archivesGroupId 所属档案库分组ID
     * @apiSuccess (Success 200) {Number} Archives:type 档案库类型(1 登记库； 2 归档库)
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"childrenType": "ArchivesGroup","id": 档案库分组ID,"name": "档案库分组名称","fondsId": 所属全宗ID,"parentId": 上级档案库分组ID,"children": [
     * {"childrenType": "ArchivesGroup","id": 档案库分组ID,"name": "档案库分组名称","fondsId": 所属全宗ID,"parentId": 上级档案库分组ID},
     * {"childrenType": "Archives","id": 档案库ID,"structure": 档案库结构,"name": "档案库名称","archivesGroupId": 所属档案库分组ID,"type": 档案库类型}]}]}}
     */
    @RequestMapping(value = "/treeListBelowFonds", method = RequestMethod.GET)
    public Map<String, Object> treeListBelowFonds(
            @SessionAttribute(required = false) UserCredential LOGIN_USER
            , @RequestParam(value = "archiveType", defaultValue = "2") int archiveType
            , @RequestParam("id") int id) {
        int userId;
        if (LOGIN_USER != null) {
            userId = LOGIN_USER.getUserId();
        } else {
            throw new ForbiddenException("拒绝访问");
        }
        //可以管理的目录
        Set<Integer> catalogueIds = roleService.findUserManageArchiveCatalogue(userId);

        int fondsId = archivesQuery.getFondsIdByArchiveId(UInteger.valueOf(id));

        return archivesQuery.getArchivesGroupToArchivesTreeMap(
                fondsId
                , archiveType
                , a -> hasPermission(catalogueIds, a));
    }

    /**
     * @api {get} /archives/fondsToArchivesTree?archiveType={archiveType} 获取全宗、库分组、库树
     * @apiName fondsToArchivesTree
     * @apiGroup archives
     * @apiParam {Number} archiveType 档案库类型 0 全部类型(默认值) 1 登记库 2 归档库
     * @apiSuccess (Success 200) {String} childrenType 节点类型(Fonds-全宗;ArchivesGroup-档案分组;Archives-档案库.
     * @apiSuccess (Success 200) {Object[]} children 子节点信息
     * @apiSuccess (Success 200) {Number} Fonds:id 全宗ID.
     * @apiSuccess (Success 200) {String} Fonds:code 全宗号.
     * @apiSuccess (Success 200) {String} Fonds:name 全宗名称.
     * @apiSuccess (Success 200) {Number} Fonds:parentId 上级全宗ID.
     * @apiSuccess (Success 200) {Number} Fonds:orderNumber 排序号.
     * @apiSuccess (Success 200) {Number} Fonds:type 全宗类型
     * @apiSuccess (Success 200) {Number} ArchivesGroup:id 档案库分组ID.
     * @apiSuccess (Success 200) {String} ArchivesGroup:name 档案库分组名称.
     * @apiSuccess (Success 200) {Number} ArchivesGroup:parentId 上级档案库分组ID.
     * @apiSuccess (Success 200) {Number} ArchivesGroup:fondsId 所属全宗ID
     * @apiSuccess (Success 200) {Number} Archives:id 档案库ID
     * @apiSuccess (Success 200) {Number} Archives:structure 档案库结构(1 一文一件；2 案卷；3 项目)
     * @apiSuccess (Success 200) {Number} Archives:name 档案库名称
     * @apiSuccess (Success 200) {Number} Archives:archivesGroupId 所属档案库分组ID
     * @apiSuccess (Success 200) {Number} Archives:type 档案库类型(1 登记库； 2 归档库)
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"childrenType": "Fonds","id": 全宗ID,"code": "全宗号","name": "全宗名称","parentId": 上级全宗ID,"orderNumber": 排序号,"type": 全宗类型,"children": [
     * {"childrenType": "ArchivesGroup","id": 档案库分组ID,"name": "档案库分组名称","fondsId": 所属全宗ID,"parentId": 上级档案库分组ID,"children": [
     * {"childrenType": "ArchivesGroup","id": 档案库分组ID,"name": "档案库分组名称","fondsId": 所属全宗ID,"parentId": 上级档案库分组ID},
     * {"childrenType": "Archives","id": 档案库ID,"structure": 档案库结构,"name": "档案库名称","archivesGroupId": 所属档案库分组ID,"type": 档案库类型}]},
     * {"childrenType": "Fonds","id": 全宗ID,"code": "全宗号","name": "全宗名称","parentId": 上级全宗ID,"orderNumber": 排序号,"type": 全宗类型}]}]}}.
     */
    @RequestMapping(value = "/fondsToArchivesTree", method = RequestMethod.GET)
    public Map<String, Object> fondsToArchivesTree(
            @SessionAttribute(required = false) UserCredential LOGIN_USER
            , @RequestParam(value = "archiveType", defaultValue = "0") int archiveType) {
        int userId;
        if (LOGIN_USER != null) {
            userId = LOGIN_USER.getUserId();
        } else {
            throw new ForbiddenException("拒绝访问");
        }
        //可以管理的全宗、目录
        Set<Integer> fondsIds = roleService.findUserManageFonds(userId);
        Set<Integer> catalogueIds = roleService.findUserManageArchiveCatalogue(userId);

        return archivesQuery.getFondsToArchivesTreeMap(
                archiveType
                , a -> hasPermission(fondsIds, a)
                , a -> hasPermission(catalogueIds, a));
    }

    /**
     * @api {get} /archives/fondsToArchivesGroupTree 获取全宗、库分组树
     * @apiName fondsToArchivesGroupTree
     * @apiGroup archives
     * @apiSuccess (Success 200) {String} childrenType 节点类型(Fonds-全宗;ArchivesGroup-档案分组).
     * @apiSuccess (Success 200) {Object[]} children 子节点信息
     * @apiSuccess (Success 200) {Number} Fonds:id 全宗ID.
     * @apiSuccess (Success 200) {String} Fonds:code 全宗号.
     * @apiSuccess (Success 200) {String} Fonds:name 全宗名称.
     * @apiSuccess (Success 200) {Number} Fonds:parentId 上级全宗ID.
     * @apiSuccess (Success 200) {Number} Fonds:orderNumber 排序号.
     * @apiSuccess (Success 200) {Number} Fonds:type 全宗类型
     * @apiSuccess (Success 200) {Number} ArchivesGroup:id 档案库分组ID.
     * @apiSuccess (Success 200) {String} ArchivesGroup:name 档案库分组名称.
     * @apiSuccess (Success 200) {Number} ArchivesGroup:parentId 上级档案库分组ID.
     * @apiSuccess (Success 200) {Number} ArchivesGroup:fondsId 所属全宗ID
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"childrenType": "Fonds","id": 全宗ID,"code": "全宗号","name": "全宗名称","parentId": 上级全宗ID,"orderNumber": 排序号,"type": 全宗类型,"children": [
     * {"childrenType": "ArchivesGroup","id": 档案库分组ID,"name": "档案库分组名称","fondsId": 所属全宗ID,"parentId": 上级档案库分组ID,"children": [
     * {"childrenType": "ArchivesGroup","id": 档案库分组ID,"name": "档案库分组名称","fondsId": 所属全宗ID,"parentId": 上级档案库分组ID}]},
     * {"childrenType": "Fonds","id": 全宗ID,"code": "全宗号","name": "全宗名称","parentId": 上级全宗ID,"orderNumber": 排序号,"type": 全宗类型}]}]}}.
     */
    @RequestMapping(value = "/fondsToArchivesGroupTree", method = RequestMethod.GET)
    public Map<String, Object> fondsToArchivesGroupTree(
            @SessionAttribute(required = false) UserCredential LOGIN_USER) {
        int userId;
        if (LOGIN_USER != null) {
            userId = LOGIN_USER.getUserId();
        } else {
            throw new ForbiddenException("拒绝访问");
        }
        //可以管理的全宗
        Set<Integer> fondsIds = roleService.findUserManageFonds(userId);

        return archivesQuery.getFondsToArchivesGroupTree(a -> hasPermission(fondsIds, a));
    }


    /**
     * @api {post} /archives 新增档案库
     * @apiName save
     * @apiGroup archives
     * @apiParam {String} name 档案库名称
     * @apiParam {Number} structure 档案库结构(1 一文一件；2 传统立卷；3 项目)
     * @apiParam {Number} archivesGroupId 档案库分组ID
     * @apiParam {Number} contentTypeId 档案库内容类型ID
     * @apiParam {Number} type 档案库类型(1 登记库； 2 归档库)
     * @apiParam {String} remark 备注（未输入传""值）
     * @apiError (Error 400) message 档案库名称已存在.
     * @apiUse ErrorExample
     */
    @Transactional
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody Archives archives) {
        List<Integer> catalogueIds = archivesService.save(archives);
        catalogueIds.forEach(id -> entryAsyncTask.createCatalogueInit(id));
    }

    /**
     * @api {delete} /archives/{id} 删除档案库
     * @apiName delete
     * @apiGroup archives
     * @apiParam {Number} id 档案库ID（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        archivesService.delete(id);
    }

    /**
     * @api {put} /archives 修改档案库
     * @apiName update
     * @apiGroup archives
     * @apiParam {Number} id 档案库ID
     * @apiParam {String{30}} name 档案库名称
     * @apiParam {Number} archivesGroupId 档案库分组ID
     * @apiParam {String{100}} remark 备注（未输入传""值）
     * @apiError (Error 400) message 档案库名称已存在.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody Archives archives) {
        archivesService.update(archives);
    }

    /**
     * @api {get} /archives/{id} 获取档案库详情
     * @apiName get
     * @apiGroup archives
     * @apiParam {Number} id 档案库ID（url占位符）
     * @apiSuccess (Success 200) {Number} id 档案库ID.
     * @apiSuccess (Success 200) {Number} name 档案库名称.
     * @apiSuccess (Success 200) {Number} structure 档案库结构(1 一文一件；2 传统立卷；3 项目).
     * @apiSuccess (Success 200) {Number} archivesGroupId 档案库分组ID.
     * @apiSuccess (Success 200) {Number} contentTypeId 档案库内容类型ID.
     * @apiSuccess (Success 200) {Number} type 档案库类型(1 登记库； 2 归档库).
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": 2,"name": "档案库名称","structure": "1","archivesGroupId": 3,"contentTypeId": 6,"type": 2,"remark": "备注"}}.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable("id") int id) {
        return archivesQuery.getArchives(UInteger.valueOf(id));
    }

    /**
     * @api {get} /archives/archivesToCatalogueTree 获取档案库分组下档案库、目录列表
     * @apiName archivesToCatalogueTree
     * @apiGroup archives
     * @apiParam {Number} archivesGroupId 档案库分组ID
     * @apiSuccess (Success 200) {String} childrenType 节点类型(Archives-档案库;Catalogue-目录).
     * @apiSuccess (Success 200) {Number} Archives:id 档案库ID
     * @apiSuccess (Success 200) {Number} Archives:structure 档案库结构(1 一文一件；2 案卷；3 项目)
     * @apiSuccess (Success 200) {Number} Archives:name 档案库名称
     * @apiSuccess (Success 200) {Number} Archives:archivesGroupId 所属档案库分组ID
     * @apiSuccess (Success 200) {Number} Archives:contentType 档案库内容类型
     * @apiSuccess (Success 200) {Number} Archives:type 档案库类型(1 登记库； 2 归档库)
     * @apiSuccess (Success 200) {Number} Catalogue:id 目录ID
     * @apiSuccess (Success 200) {Number} Catalogue:catalogueType 目录类型(1 一文一件/卷内 2 案卷 3 项目）
     * @apiSuccess (Success 200) {Number} Catalogue:archivesId 档案库ID
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"items": {"childrenType": "Archives","id": 档案库ID,"structure": 档案库结构,"name": "档案库名称","archivesGroupId": 所属档案库分组ID,"type": 档案库类型,"children": [
     * {"childrenType": "Catalogue","id": 目录ID,"catalogueType": 目录类型,"archivesId": 档案库ID}]}}}.
     */
    @RequestMapping(value = "/archivesToCatalogueTree", method = RequestMethod.GET)
    public Map<String, Object> archivesToCatalogueTree(@RequestParam("archivesGroupId") int archivesGroupId) {
        return archivesQuery.getArchivesToCatalogueTree(UInteger.valueOf(archivesGroupId));
    }

}
