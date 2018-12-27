package com.ztdx.eams.controller.system;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.basic.exception.ForbiddenException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.exception.NotFoundException;
import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.system.application.*;
import com.ztdx.eams.domain.system.model.Fonds;
import com.ztdx.eams.domain.system.model.Permission;
import com.ztdx.eams.domain.system.model.Resource;
import com.ztdx.eams.domain.system.model.Role;
import com.ztdx.eams.query.SystemQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/role")
public class RoleController {

    private RoleService roleService;

    private FondsService fondsService;

    private PermissionService permissionService;

    private ResourceService resourceService;

    private SystemQuery systemQuery;
    @Autowired
    public RoleController(SystemQuery systemQuery,RoleService roleService, FondsService fondsService, PermissionService permissionService, ResourceService resourceService) {
        this.roleService = roleService;
        this.fondsService = fondsService;
        this.permissionService = permissionService;
        this.resourceService = resourceService;
        this.systemQuery=systemQuery;
    }

    /**
     * @api {post} /role 新增角色
     * @apiName save
     * @apiGroup role
     * @apiParam {String{50}} roleName 角色名称
     * @apiParam {Number} [fondsId] 全宗id
     * @apiParam {String(200)} remark 描述(非必须)
     * @apiError (Error 400) message 1.角色名称已存在
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    @PreAuthorize("hasAnyRole('ADMIN') " +
            "|| (#role.fondsId != null) && hasAnyAuthority('fonds_role_write_' + #role.fondsId) " +
            "|| (#role.fondsId == null) && hasAnyAuthority('global_role_write')")
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
     * @apiParam {String(200)} remark 描述(非必须)
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
     *     "global": {
     *         "role_add": {
     *             "id": 1,
     *             "name": "添加",
     *             "resourceUrl": "user_add"
     *         }
     *     },
     *     "fonds": {
     *         "1": {
     *             "user_add": {
     *                 "id": 3,
     *                 "name": "添加",
     *                 "resourceUrl": "user_add"
     *             }
     *         }
     *     },
     *     "archiveCatalogue": {
     *         "1": {
     *             "user_add": {
     *                 "id": 3,
     *                 "name": "添加",
     *                 "resourceUrl": "user_add"
     *             }
     *         }
     *     }
     * }
     * @apiParamExample {json} Request-Example1:
     * {
     *     "global": [1,2,3],
     *     "fonds": {
     *         "1": [1,2,3]
     *     },
     *     "archiveCatalogue": {
     *         "1": [1,2,3]
     *     }
     * }
     * @apiError (Error 400) message 1.参数permissions错误 2.全宗不存在 3.档案库目录不存在
     * @apiError (Error 403) message 1.无权限设置全局权限 2.无权限设置全宗权限 3.无权限设置档案库权限
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/{id}/permissions", method = RequestMethod.POST)
    public Map<String, List<String>> addPermission(@PathVariable long id, @RequestBody JsonNode permissions) {
        if (permissions.size() == 0) {
            throw new InvalidArgumentException("参数permissions错误");
        }

        List<Permission> list = new ArrayList<>();

        permissions.fieldNames().forEachRemaining(categroy -> {
            if (!permissions.hasNonNull(categroy)){
                return;
            }
            JsonNode node = permissions.get(categroy);
            /*if (!node.isObject() || !node.fieldNames().hasNext()){
                throw new InvalidArgumentException(node + "参数错误");
            }*/
            switch (categroy) {
                case "global":
                    list.addAll(listParsePermission(id, null, null, node));
                    break;
                case "fonds":
                case "archiveCatalogue":
                    if (!node.fieldNames().hasNext()){
                        return;
                    }
                    node.fieldNames().forEachRemaining(tid -> {
                        if ("fonds".equals(categroy)){
                            list.addAll(listParsePermission(id, Integer.parseInt(tid), null, node.get(tid)));
                        }else{
                            list.addAll(listParsePermission(id, null, Integer.parseInt(tid), node.get(tid)));
                        }
                    });

                    break;
                default:
                    break;
            }

        });

        permissionService.saveAll(list);
        return null;
    }

    private List<Permission> listParsePermission(
            long roleId, Integer fondsId, Integer archiveId, JsonNode permissionNode){
        List<Permission> result = new ArrayList<>();

        if (permissionNode.getNodeType() == JsonNodeType.ARRAY){
            permissionNode.forEach(a -> {
                if (a.isInt()){
                    result.add(makePermission(a.asLong(), roleId, fondsId, archiveId));
                }
            });
        }else if (permissionNode.getNodeType() == JsonNodeType.OBJECT) {
            permissionNode.fieldNames().forEachRemaining(field -> {
                JsonNode node = permissionNode.get(field);
                if (!node.isObject() || !node.has("id")) {
                    throw new InvalidArgumentException("参数错误，缺少id字段");
                }
                JsonNode idNode = node.get("id");
                if (!idNode.isInt()) {
                    throw new InvalidArgumentException("参数错误，id字段格式错误");
                }
                result.add(makePermission(idNode.asLong(), roleId, fondsId, archiveId));
            });
        }

        return result;
    }

    private Permission makePermission(long resourceId,long roleId, Integer fondsId, Integer archiveId){
        //TODO @lijie resourceService.getResource 必须使用缓存
        Resource resource = resourceService.getResource(resourceId);
        if (resource == null){
            throw new InvalidArgumentException("权限("+resourceId+")不存在");
        }
        Permission permission = new Permission();
        permission.setRoleId(roleId);
        permission.setResourceUrl(resource.getResourceUrl());
        permission.setArchiveId(archiveId);
        permission.setFondsId(fondsId);
        permission.setResourceId(resourceId);
        return permission;
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
    public Map<String, Object> addUser(@PathVariable long id, @JsonParam List<Integer> userIds) {
        if (userIds.size() == 0) {
            throw new InvalidArgumentException("参数userIds错误");
        }

        return roleService.addUser(id, userIds);
    }


    /**
     * @api {get} /role 查询角色列表
     * @apiName listRole
     * @apiGroup role
     * @apiSuccess (Success 200) {Array} global 全局角色列表
     * @apiSuccess (Success 200) {int} global.id 子节点id
     * @apiSuccess (Success 200) {String="Fonds","Role"} global.type 子节点类型 Fonds:全宗，有children属性 Role:角色，无children属性
     * @apiSuccess (Success 200) {String} global.name 子节点名称
     * @apiSuccess (Success 200) {Number} global.fondsId 全宗id，全局角色值为Null
     * @apiSuccess (Success 200) {String} global.remark 备注
     * @apiSuccess (Success 200) {Array} fonds 全宗列表
     * @apiSuccess (Success 200) {int} fonds.id 全宗id
     * @apiSuccess (Success 200) {String="Fonds","Role"} fonds.type 节点类型 Fonds:全宗，有children属性 Role:角色，无children属性
     * @apiSuccess (Success 200) {String} fonds.name 全宗名称
     * @apiSuccess (Success 200) {bool} fonds.allowAdd 是否可以添加角色
     * @apiSuccess (Success 200) {Array} [fonds.children] 子节点 节点类型为"Fonds"有此属性
     * @apiSuccess (Success 200) {int} fonds.children.id 子节点id
     * @apiSuccess (Success 200) {String="Fonds","Role"} fonds.children.type 子节点类型 Fonds:全宗，有children属性 Role:角色，无children属性
     * @apiSuccess (Success 200) {String} fonds.children.name 子节点名称
     * @apiSuccess (Success 200) {Number} fonds.children.fondsId 全宗id，全局角色值为Null
     * @apiSuccess (Success 200) {String} fonds.children.remark 备注
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
     *                 "type":"Role",
     *                 "fondsId": null,
     *                 "remark": "备注"
     *             },
     *             {
     *                 "id":5,
     *                 "name":"全局角色E",
     *                 "type":"Role",
     *                 "fondsId": null,
     *                 "remark": "备注"
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
     *                         "type":"Role",
     *                         "fondsId": 1,
     *                         "remark": "备注"
     *                     }
     *                     {
     *                         "id":2,
     *                         "name":"全局角色B",
     *                         "type":"Role",
     *                         "fondsId": 1,
     *                         "remark": "备注"
     *                     }
     *                 ]
     *             }
     *         ]
     *     }
     * }
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    //@PreAuthorize("hasAnyRole('global_role_query', 'ADMIN')")
    public Map<String, Object> listRole(@SessionAttribute(required = false) UserCredential LOGIN_USER){
        //如果是管理员可以查看到全局角色列表
        int userId;
        if (LOGIN_USER != null){
            userId = LOGIN_USER.getUserId();
        }else{
            throw new ForbiddenException("拒绝访问");
        }



        List<Object> global = null;
        if (permissionService.hasAnyAuthority("global_role_read", "ROLE_ADMIN")) {
            global = this.listGlobalRole();
        }

        List<Fonds> fonds;
        List<Role> roles;
        if (permissionService.hasAnyAuthority("global_role_read", "ROLE_ADMIN")) {
            fonds = fondsService.findAll();
            roles = roleService.findByFondsIdIsNotNull();
        }else{
            Set<Integer> fondsIds = roleService.findUserManageFonds(userId);
            List<Integer> filterFondsIds = fondsIds.stream().filter(
                    a -> permissionService.hasAuthority(String.format("fonds_role_read_%d", a)))
            .collect(Collectors.toList());
            fonds = fondsService.findAllById(filterFondsIds);
            roles = roleService.findByFondsIdIn(fondsIds);
        }
        List<Object> fondsMap = this.listFondsRole(roles, fonds);

        Map<String, Object> result = new HashMap<>();
        result.put("global", global);
        result.put("fonds", fondsMap);

        return result;

    }

    /**
     * 获取用户可以管理的全宗，以及全宗下的角色
     * @return 全宗的数组，包括全宗的角色
     */
    private List<Object> listGlobalRole() {

        List<Role> roles = roleService.findByFondsIdIsNull();

        return roles.stream().map(this::getRoleMap).collect(Collectors.toList());
    }

    /**
     * 获取全宗角色
     * @param roles 全宗的角色
     * @param fonds 全宗
     * @return 全宗角色列表
     */
    private List<Object> listFondsRole(List<Role> roles, List<Fonds> fonds) {
        //查询出当前用户可以管理的全宗
        //并把角色挂到全宗树上

        Map<Integer, List<Role>> fondsGroup = roles.stream().collect(Collectors.groupingBy(Role::getFondsId));

        return fonds.stream().map((a) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("name", a.getName());
            map.put("type", "Fonds");
            map.put("allowAdd", true);
            List<Role> childrenList = fondsGroup.getOrDefault(a.getId(),null);
            if (childrenList != null) {
                List<Object> children = childrenList.stream().map(this::getRoleMap).collect(Collectors.toList());
                map.put("children", children);
            }
            return map;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> getRoleMap(Role a) {
        Map<String, Object> mapb = new HashMap<>();
        mapb.put("id", a.getId());
        mapb.put("name", a.getRoleName());
        mapb.put("type", "Role");
        mapb.put("fondsId", a.getFondsId());
        mapb.put("remark", a.getRemark());
        return mapb;
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
     * @apiSuccess (Success 200) {Array} fonds.fondsId 全宗id(动态值)，内容为权限的id数组
     * @apiSuccess (Success 200) {Object} archiveCatalogue 档案目录权限列表
     * @apiSuccess (Success 200) {Array} archiveCatalogue.catalogueId 档案库目录id(动态值)，内容为权限的id数组
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "data": {
     *         "global": [1, 2, 3],
     *         "fonds": {
     *             "1": [3, 4, 5],
     *             "2": [3, 4, 5]
     *         },
     *         "archiveCatalogue": {
     *             "1": [3, 4, 5],
     *             "2": [3, 4, 5]
     *         }
     *     }
     * }
     */
    @RequestMapping(value = "/{id}/permissions", method = RequestMethod.GET)
    public Map rolePermission(@PathVariable("id") long id){
         return roleService.listRolePermission(id);
    }

    /**
     * @api {get} /role/{id}/users 查询角色的权限
     * @apiName roleUsers
     * @apiGroup role
     * @apiParam {long} id 角色id path参数
     * @apiSuccess (Success 200) {Array} data 用户列表
     * @apiSuccess (Success 200) {Number} data.id 用户id
     * @apiSuccess (Success 200) {String} data.name 用户姓名
     * @apiSuccess (Success 200) {String} data.organization 公司
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "data": [
     *         {
     *             "id": 1,
     *             "name": "姓名",
     *             "organization": "公司"
     *         }
     *     ]
     * }
     */
    @RequestMapping(value = "/{id}/users", method = RequestMethod.GET)
    public List<Map<String, Object>> roleUsers(@PathVariable("id") long id){
        return roleService.roleUsers(id);
    }
    /**
     * @api {get} /role/listByFonds 查询全宗下角色列表
     * @apiName listByFonds
     * @apiGroup role
     * @apiParam {Number} id 全宗id (不传默认全局)
     * @apiSuccess (Success 200) {NUmber} id 角色id
     * @apiSuccess (Success 200) {String} name 角色名称
     * @apiSuccess (Success 200) {String} remark 备注
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "data": [
     *                  {
     *                       "id":4,
     *                       "name":"角色D",
     *                       "remark": "备注"
     *                   },
     *                   {
     *                       "id":5,
     *                       "name":"角色E",
     *                       "remark": "备注"
     *                   }
     *               ]
     * }
     */
    @RequestMapping(value = "/listByFonds", method = RequestMethod.GET)
    public List<Map<String, Object>> listByFonds(@RequestParam(name = "id",required = false) Integer fondsId){
        return systemQuery.getRoleListByFonds(null==fondsId ? null : UInteger.valueOf(fondsId));
    }
}
