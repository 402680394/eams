package com.ztdx.eams.controller;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.basic.exception.ForbiddenException;
import com.ztdx.eams.domain.archives.application.ArchivesService;
import com.ztdx.eams.domain.system.application.PermissionService;
import com.ztdx.eams.domain.system.application.RoleService;
import com.ztdx.eams.query.ArchivesQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/archives")
public class ArchivesController {

    private final ArchivesService archivesService;

    private final ArchivesQuery archivesQuery;

    private final RoleService roleService;

    private final PermissionService permissionService;

    @Autowired
    public ArchivesController(ArchivesService archivesService, ArchivesQuery archivesQuery, RoleService roleService, PermissionService permissionService) {
        this.archivesService = archivesService;
        this.archivesQuery = archivesQuery;
        this.roleService = roleService;
        this.permissionService = permissionService;
    }

    /**
     * @api {get} /archives/treeList?archiveType={archiveType} 获取全宗、档案库分组、登记库、目录树形列表
     * @apiName treeList
     * @apiGroup archives
     * @apiParam {Number} [archiveType] 档案库类型 1 登记库(默认值) 2 归档库
     * @apiSuccess (Success 200) {String} childrenType 节点类型(1.Fonds-全宗;2.ArchivesGroup-档案分组;3.Archives-档案库;4.Catalogue-目录).
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
     * @apiSuccess (Success 200) {Number} Catalogue:tableName 存储物理表名
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"childrenType": "Fonds","id": 全宗ID,"code": "全宗号","name": "全宗名称","parentId": 上级全宗ID,"orderNumber": 排序号,"type": 全宗类型,"children": [
     * {"childrenType": "ArchivesGroup","id": 档案库分组ID,"name": "档案库分组名称","fondsId": 所属全宗ID,"parentId": 上级档案库分组ID,"children": [
     * {"childrenType": "ArchivesGroup","id": 档案库分组ID,"name": "档案库分组名称","fondsId": 所属全宗ID,"parentId": 上级档案库分组ID},
     * {"childrenType": "Archives","id": 档案库ID,"structure": 档案库结构,"name": "档案库名称","archivesGroupId": 所属档案库分组ID,"type": 档案库类型,"children": [
     * {"childrenType": "Catalogue","id": 目录ID,"catalogueType": 目录类型,"archivesId": 档案库ID,"tableName": "存储物理表名"}]}]},
     * {"childrenType": "Fonds","id": 全宗ID,"code": "全宗号","name": "全宗名称","parentId": 上级全宗ID,"orderNumber": 排序号,"type": 全宗类型}]}]}}.
     */
    @RequestMapping(value = "/treeList", method = RequestMethod.GET)
    public Map<String, Object> treeList(
            @SessionAttribute(required = false) UserCredential LOGIN_USER
            , @RequestParam(value = "archiveType", defaultValue = "1") int archiveType) {
        int userId;
        if (LOGIN_USER != null){
            userId = LOGIN_USER.getUserId();
        }else{
            throw new ForbiddenException("拒绝访问");
        }
        //可以管理的全宗
        Set<Integer> fondsIds = roleService.findUserManageFonds(userId);
        Set<Integer> catalogueIds = roleService.findUserManageArchiveCatalogue(userId);

        return archivesQuery.getCatalogueTreeMap(
                archiveType
                , a -> hasPermission(fondsIds, a)
                , a -> hasPermission(catalogueIds, a));
    }

    private boolean hasPermission(Set<Integer> ids, int id){
        if (permissionService.hasAnyAuthority("ROLE_ADMIN")){
            return true;
        }else{
            return ids.contains(id);
        }

    }
}
