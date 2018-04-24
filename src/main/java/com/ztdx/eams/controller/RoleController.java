package com.ztdx.eams.controller;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.exception.NotFoundException;
import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.system.application.RoleService;
import com.ztdx.eams.domain.system.model.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/role")
public class RoleController {

    private RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * @api {post} /role 新增角色
     * @apiName save
     * @apiGroup role
     * @apiParam {String} roleName 角色名称
     * @apiParam {long} [resourceId] 资源节点id
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
     * @apiParam {long} id 角色id（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") long id) {
        roleService.delete(id);
    }

    /**
     * @api {put} /role/{id} 修改角色
     * @apiName update
     * @apiGroup role
     * @apiParam {long} id 角色id（url占位符）.
     * @apiParam {String} roleName 角色名称.
     * @apiParam {long} [resourceId] 资源节点id.
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
     * @apiParam {long} id 角色id（url占位符）
     * @apiSuccess (Success 200) {int} id 角色id
     * @apiSuccess (Success 200) {String} roleName 角色名称
     * @apiSuccess (Success 200) {long} resourceId 资源节点id
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
     * @apiParam {long} id 角色id（url占位符）
     * @apiParam {String[]} resourceUrls 权限
     * @apiSuccess (Success 200) String[] added 增加成功的权限
     * @apiSuccess (Success 200) String[] existed 已存在的权限
     * @apiError (Error 400) message 1.参数resourceUrls错误
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"added":["权限1","权限2"],"existed":["权限3"]}}
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
     * @apiParam {long} id 角色id（url占位符）
     * @apiParam {int[]} userIds 用户id
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
}
