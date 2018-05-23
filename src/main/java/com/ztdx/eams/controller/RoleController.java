package com.ztdx.eams.controller;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.exception.NotFoundException;
import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.system.application.FondsService;
import com.ztdx.eams.domain.system.application.OrganizationService;
import com.ztdx.eams.domain.system.application.RoleService;
import com.ztdx.eams.domain.system.application.UserService;
import com.ztdx.eams.domain.system.model.Role;
import com.ztdx.eams.domain.system.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/role")
public class RoleController {

    private RoleService roleService;

    private UserService userService;

    private OrganizationService organizationService;

    private FondsService fondsService;

    public RoleController(RoleService roleService, UserService userService, OrganizationService organizationService, FondsService fondsService) {
        this.roleService = roleService;
        this.userService = userService;
        this.organizationService = organizationService;
        this.fondsService = fondsService;
    }

    /**
     * @api {post} /role 新增角色
     * @apiName save
     * @apiGroup role
     * @apiParam {String{50}} roleName 角色名称
     * @apiParam {Number} [resourceId] 资源节点id
     * @apiError (Error 400) message 1.角色名称已存在
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody Role role) {
        roleService.save(role);
    }

    /**
     * @api {delete} /role/{id} 删除角色
     * @apiName delete
     * @apiGroup role
     * @apiParam {Number} id 角色id（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") long id) {
        roleService.delete(id);
    }

    /**
     * @api {put} /role/{id} 修改角色
     * @apiName update
     * @apiGroup role
     * @apiParam {Number} id 角色id（url占位符）
     * @apiParam {String{50}} roleName 角色名称
     * @apiParam {Number} [fondsId] 全宗节点id
     * @apiError (Error 400) message 1.角色名称已存在
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void update(@PathVariable("id") long id, @RequestBody Role role) {
        role.setId(id);
        roleService.update(role);
    }

    /**
     * @api {get} /role/{id} 获取角色详情
     * @apiName get
     * @apiGroup role
     * @apiParam {Number} id 角色id（url占位符）
     * @apiSuccess (Success 200) {Number} id 角色id
     * @apiSuccess (Success 200) {String} roleName 角色名称
     * @apiSuccess (Success 200) {Number} resourceId 资源节点id
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"id": 1,"roleName": "角色名称","resourceId": "资源节点id"}}
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Role get(@PathVariable("id") long id) {
        Role role = roleService.getRole(id);
        if (role == null) {
            throw new NotFoundException("没有找到角色");
        }
        return role;
    }

    /**
     * @api {post} /role/{id}/permissions 新增角色拥有的权限
     * @apiName addPermissions
     * @apiGroup role
     * @apiParam {Number} id 角色id（url占位符）
     * @apiParam {Object} global 全局权限
     * @apiParam {Object} global.permissionKey 权限的key
     * @apiParam {String} global.permissionKey.id 权限的id
     * @apiParam {String} global.permissionKey.name 权限的名称
     * @apiParam {String} global.permissionKey.resourceUrl 资源url
     * @apiParam {Object} fonds 全宗权限列表
     * @apiParam {Object} fonds.fondsId 全宗id
     * @apiParam {Object} fonds.fondsId.permissionKey 权限的key
     * @apiParam {String} fonds.fondsId.permissionKey.id 权限的id
     * @apiParam {String} fonds.fondsId.permissionKey.name 权限的名称
     * @apiParam {String} fonds.fondsId.permissionKey.resourceUrl 资源url
     * @apiParam {Object} archiveCatalogue 档案目录权限列表
     * @apiParam {Object} archiveCatalogue.catalogueId 全宗id
     * @apiParam {Object} archiveCatalogue.catalogueId.permissionKey 权限的key
     * @apiParam {String} archiveCatalogue.catalogueId.permissionKey.id 权限的id
     * @apiParam {String} archiveCatalogue.catalogueId.permissionKey.name 权限的名称
     * @apiParam {String} archiveCatalogue.catalogueId.permissionKey.resourceUrl 资源url
     * @apiParamExample {json} Request-Example:
     * {
     *     "data": {
     *         "global": {
     *             "role_add": {
     *                 "id": 1,
     *                 "name": "添加",
     *                 "resourceUrl": "user_add"
     *             }
     *         },
     *         "fonds": {
     *             "1": {
     *                 "user_add": {
     *                     "id": 3,
     *                     "name": "添加",
     *                     "resourceUrl": "user_add"
     *                 }
     *             }
     *         },
     *         "archiveCatalogue": {
     *             "1": {
     *                 "user_add": {
     *                     "id": 3,
     *                     "name": "添加",
     *                     "resourceUrl": "user_add"
     *                 }
     *             }
     *         }
     *     }
     * }
     * @apiError (Error 400) message 1.参数resourceUrls错误 2.全宗不存在 3.档案库目录不存在
     * @apiError (Error 403) message 无权限
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/{id}/permissions", method = RequestMethod.POST)
    public Map<String, List<String>> addPermission(@PathVariable long id, @JsonParam List<String> resourceUrls) {
        if (resourceUrls.size() == 0) {
            throw new InvalidArgumentException("参数resourceUrls错误");
        }

        return roleService.addPermission(id, resourceUrls);
    }

    /**
     * @api {post} /role/{id}/users 新增拥有此角色的用户
     * @apiName addUsers
     * @apiGroup role
     * @apiParam {Number} id 角色id（url占位符）
     * @apiParam {Number[]} userIds 用户id
     * @apiSuccess (Success 200) String[] added 增加成功的用户id
     * @apiSuccess (Success 200) String[] existed 已存在的用户id
     * @apiError (Error 400) message 1.参数userIds错误
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"added":[1,2],"existed":[3]}}
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/{id}/users", method = RequestMethod.POST)
    public Map<String, List<Integer>> addUser(@PathVariable long id, @JsonParam List<Integer> userIds) {
        if (userIds.size() == 0) {
            throw new InvalidArgumentException("参数userIds错误");
        }

        return roleService.addUser(id, userIds);
    }


    /**
     * @api {get} /role 查询角色列表
     * @apiName listRole
     * @apiGroup role
     * @apiSuccess (Success 200) {Array} global 全宗角色列表
     * @apiSuccess (Success 200) {int} global.id 子节点id
     * @apiSuccess (Success 200) {String="Fonds","Role"} global.type 子节点类型 Fonds:全宗，有children属性 Role:角色，无children属性
     * @apiSuccess (Success 200) {String} global.name 子节点名称
     * @apiSuccess (Success 200) {Array} fonds 全宗列表
     * @apiSuccess (Success 200) {int} fonds.id 全宗id
     * @apiSuccess (Success 200) {String="Fonds","Role"} fonds.type 节点类型 Fonds:全宗，有children属性 Role:角色，无children属性
     * @apiSuccess (Success 200) {String} fonds.name 全宗名称
     * @apiSuccess (Success 200) {bool} fonds.allowAdd 是否可以添加角色
     * @apiSuccess (Success 200) {Array} [fonds.children] 子节点 节点类型为"Fonds"有此属性
     * @apiSuccess (Success 200) {int} fonds.children.id 子节点id
     * @apiSuccess (Success 200) {String="Fonds","Role"} fonds.children.type 子节点类型 Fonds:全宗，有children属性 Role:角色，无children属性
     * @apiSuccess (Success 200) {String} fonds.children.name 子节点名称
     * @apiSuccess (Success 200) {bool} [fonds.children.allowAdd] 是否可以添加角色，节点类型为"Fonds"有此属性
     * @apiSuccess (Success 200) {Array} [fonds.children.children] 子节点 节点类型为"Fonds"有此属性
     *
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "data":{
     *         "global":[
     *             {
     *                 "id":4,
     *                 "name":"全局角色D",
     *                 "type":"Role"
     *             },
     *             {
     *                 "id":5,
     *                 "name":"全局角色E",
     *                 "type":"Role"
     *             }
     *         ],
     *         "fonds":[
     *             {
     *                 "id":1,
     *                 "name":"全宗名称",
     *                 "type":"Fonds",
     *                 "allowAdd":false,
     *                 "children":[
     *                     {
     *                         "id":1,
     *                         "name":"全局角色A",
     *                         "type":"Role"
     *                     }
     *                     {
     *                         "id":2,
     *                         "name":"全局角色B",
     *                         "type":"Role"
     *                     }
     *                 ]
     *             }
     *         ]
     *     }
     * }
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Map<String, Object> listRole(){
        //如果是管理员可以查看到全局角色列表
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUserName(userName);

        List<Object> global = roleService.listGlobalRole(1);
        List<Object> fonds = roleService.listFondsRole(1);

        Map<String, Object> result = new HashMap<>();
        result.put("global", global);
        result.put("fonds", fonds);

        return result;

    }


    /**
     * @api {get} /role/{id}/permissions 查询角色的权限
     * @apiName rolePermission
     * @apiGroup role
     * @apiParam {long} id 角色id path参数
     * @apiSuccess (Success 200) {Object} global 全局权限列表
     * @apiSuccess (Success 200) {Object} global.permissionKey 权限的key
     * @apiSuccess (Success 200) {String} global.permissionKey.id 权限的id
     * @apiSuccess (Success 200) {String} global.permissionKey.name 权限的名称
     * @apiSuccess (Success 200) {String} global.permissionKey.resourceUrl 资源url
     * @apiSuccess (Success 200) {Object} fonds 全宗权限列表
     * @apiSuccess (Success 200) {Object} fonds.fondsId 全宗id
     * @apiSuccess (Success 200) {Object} fonds.fondsId.permissionKey 权限的key
     * @apiSuccess (Success 200) {String} fonds.fondsId.permissionKey.id 权限的id
     * @apiSuccess (Success 200) {String} fonds.fondsId.permissionKey.name 权限的名称
     * @apiSuccess (Success 200) {String} fonds.fondsId.permissionKey.resourceUrl 资源url
     * @apiSuccess (Success 200) {Object} archiveCatalogue 档案目录权限列表
     * @apiSuccess (Success 200) {Object} archiveCatalogue.catalogueId 全宗id
     * @apiSuccess (Success 200) {Object} archiveCatalogue.catalogueId.permissionKey 权限的key
     * @apiSuccess (Success 200) {String} archiveCatalogue.catalogueId.permissionKey.id 权限的id
     * @apiSuccess (Success 200) {String} archiveCatalogue.catalogueId.permissionKey.name 权限的名称
     * @apiSuccess (Success 200) {String} archiveCatalogue.catalogueId.permissionKey.resourceUrl 资源url
     *
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "data": {
     *         "global": {
     *             "role_add": {
     *                 "id": 1,
     *                 "name": "添加",
     *                 "resourceUrl": "user_add"
     *             },
     *             "permission_manage": {
     *                 "id": 2,
     *                 "name": "权限管理",
     *                 "resourceUrl": "permission_manage"
     *             }
     *         },
     *         "fonds": {
     *             "1": {
     *                 "user_add": {
     *                     "id": 3,
     *                     "name": "添加",
     *                     "resourceUrl": "user_add"
     *                 }
     *             },
     *             "2": {
     *                 "user_add": {
     *                     "id": 3,
     *                     "name": "添加",
     *                     "resourceUrl": "user_add"
     *                 }
     *             }
     *         },
     *         "archiveCatalogue": {
     *             "1": {
     *                 "user_add": {
     *                     "id": 3,
     *                     "name": "添加",
     *                     "resourceUrl": "user_add"
     *                 }
     *             },
     *             "2": {
     *                 "user_add": {
     *                     "id": 3,
     *                     "name": "添加",
     *                     "resourceUrl": "user_add"
     *                 }
     *             }
     *         }
     *     }
     * }
     */
    public void rolePermission(long id){

    }
}
