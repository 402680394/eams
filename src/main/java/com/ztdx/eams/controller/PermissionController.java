package com.ztdx.eams.controller;

import com.ztdx.eams.domain.system.application.PermissionService;
import com.ztdx.eams.domain.system.application.ResourceService;
import com.ztdx.eams.domain.system.model.ResourceCategory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/permission")
public class PermissionController {

    private PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @api {get} /permission/metadata/{id} 查询某个分类下的权限定义
     * @apiName categoryPermission
     * @apiGroup permission
     * @apiParam {id} id 权限分类 path参数
     * 100, "全局"
     * 200, "全宗"
     * 301, "档案管理-一文一件"
     * 302, "档案管理-传统立卷案卷"
     * 303, "档案管理-传统立卷卷内"
     * 304, "档案管理-项目档案"
     * 401, "档案利用-一文一件档案"
     * 402, "档案利用-传统立卷案卷"
     * 403, "档案利用-传统立卷卷内"
     * 403, "档案利用-项目档案"
     * @apiSuccess (Success 200) {Array} data 档案目录权限列表
     * @apiSuccess (Success 200) {number} data.id 子节点id
     * @apiSuccess (Success 200) {String="FunctionGroup","Function"} data.type 子节点类型
     * @apiSuccess (Success 200) {String} data.name 子节点名称
     * @apiSuccess (Success 200) {Array} [data.children] 子节点 节点类型为"FunctionGroup"有此属性
     * @apiSuccess (Success 200) {number} data.children.id 子节点id
     * @apiSuccess (Success 200) {String="FunctionGroup","Function"} data.children.type 子节点类型
     * @apiSuccess (Success 200) {String} data.children.name 子节点名称
     * @apiSuccess (Success 200) {Array} [data.children.children] 子节点 节点类型为"FunctionGroup"有此属性
     * @apiSuccess (Success 200) {Array} [data.children.resourceUrl] 资源url 节点类型为"Function"有此属性
     *
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "data":[
     *         {
     *             "id":1,
     *             "name":"系统管理",
     *             "type":"FunctionGroup",
     *             "children":[
     *                 {
     *                     "id":2,
     *                     "name":"用户管理",
     *                     "type":"FunctionGroup",
     *                     "children":[
     *                         {
     *                             "id":1,
     *                             "name":"添加",
     *                             "type":"Function",
     *                             "resourceUrl":"user_add"
     *                         }
     *                     ]
     *                 }
     *             ]
     *         },
     *         {
     *             "id":2,
     *             "name":"权限管理",
     *             "type":"Function",
     *             "resourceUrl":"permission_manage"
     *         }
     *     ]
     * }
     */
    @RequestMapping(value = "/metadata/{id}", method = RequestMethod.GET)
    public List<Map> listCategoryPermission(@PathVariable("id") int id){
        return permissionService.listCategoryPermission(ResourceCategory.create(id));
    }

    /**
     * @api {get} /permission/my 查询我的权限
     * @apiName myPermission
     * @apiGroup permission
     * @apiSuccess (Success 200) {Object} global 全局权限列表
     * @apiSuccess (Success 200) {Object} global.permissionKey 权限的key
     * @apiSuccess (Success 200) {String} global.permissionKey.id 权限的id
     * @apiSuccess (Success 200) {String} global.permissionKey.name 权限的名称\
     * @apiSuccess (Success 200) {number} global.permissionKey.fondsId 全宗id
     * @apiSuccess (Success 200) {number} global.permissionKey.archiveId 档案id
     * @apiSuccess (Success 200) {String} global.permissionKey.resourceUrl 资源url
     * @apiSuccess (Success 200) {Object} fonds 全宗权限列表
     * @apiSuccess (Success 200) {Object} fonds.fondsId 全宗id
     * @apiSuccess (Success 200) {Object} fonds.fondsId.permissionKey 权限的key
     * @apiSuccess (Success 200) {String} fonds.fondsId.permissionKey.id 权限的id
     * @apiSuccess (Success 200) {String} fonds.fondsId.permissionKey.name 权限的名称\
     * @apiSuccess (Success 200) {number} fonds.fondsId.permissionKey.fondsId 全宗id
     * @apiSuccess (Success 200) {number} fonds.fondsId.permissionKey.archiveId 档案id
     * @apiSuccess (Success 200) {String} fonds.fondsId.permissionKey.resourceUrl 资源url
     * @apiSuccess (Success 200) {Object} archiveCatalogue 档案目录权限列表
     * @apiSuccess (Success 200) {Object} archiveCatalogue.catalogueId 全宗id
     * @apiSuccess (Success 200) {Object} archiveCatalogue.catalogueId.permissionKey 权限的key
     * @apiSuccess (Success 200) {String} archiveCatalogue.catalogueId.permissionKey.id 权限的id
     * @apiSuccess (Success 200) {String} archiveCatalogue.catalogueId.permissionKey.name 权限的名称\
     * @apiSuccess (Success 200) {number} archiveCatalogue.catalogueId.permissionKey.fondsId 全宗id
     * @apiSuccess (Success 200) {number} archiveCatalogue.catalogueId.permissionKey.archiveId 档案id
     * @apiSuccess (Success 200) {String} archiveCatalogue.catalogueId.permissionKey.resourceUrl 资源url
     *
     *
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "data": {
     *         "global": {
     *             "role_add": {
     *                 "id": 1,
     *                 "name": "添加",
     *                 "fondsId": null,
     *                 "archiveId": null,
     *                 "resourceUrl": "user_add"
     *             },
     *             "permission_manage": {
     *                 "id": 2,
     *                 "name": "权限管理",
     *                 "fondsId": null,
     *                 "archiveId": null,
     *                 "resourceUrl": "permission_manage"
     *             }
     *         },
     *         "fonds": {
     *             "1": {
     *                 "user_add": {
     *                     "id": 3,
     *                     "name": "添加",
     *                     "fondsId": null,
     *                     "archiveId": null,
     *                     "resourceUrl": "user_add"
     *                 }
     *             },
     *             "2": {
     *                 "user_add": {
     *                     "id": 3,
     *                     "name": "添加",
     *                     "fondsId": null,
     *                     "archiveId": null,
     *                     "resourceUrl": "user_add"
     *                 }
     *             }
     *         },
     *         "archiveCatalogue": {
     *             "1": {
     *                 "user_add": {
     *                     "id": 3,
     *                     "name": "添加",
     *                     "fondsId": null,
     *                     "archiveId": null,
     *                     "resourceUrl": "user_add"
     *                 }
     *             },
     *             "2": {
     *                 "user_add": {
     *                     "id": 3,
     *                     "name": "添加",
     *                     "fondsId": null,
     *                     "archiveId": null,
     *                     "resourceUrl": "user_add"
     *                 }
     *             }
     *         }
     *     }
     * }
     */
    public void myPermission(){
        
    }
}
