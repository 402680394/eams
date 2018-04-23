package com.ztdx.eams.controller;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.exception.NotFoundException;
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

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody Role role) {
        roleService.save(role);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") long id) {
        roleService.delete(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void update(@PathVariable("id") long id, @RequestBody Role role) {
        role.setId(id);
        roleService.update(role);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Role get(@PathVariable("id") long id) {
        Role role = roleService.getRole(id);
        if (role == null) {
            throw new NotFoundException("没有找到角色");
        }
        return role;
    }

    @RequestMapping(value = "/{id}/permissions", method = RequestMethod.POST)
    public Map<String, List<String>> addPermission(@PathVariable long id, @RequestParam List<String> resourceUrls) {
        if (resourceUrls.size() == 0) {
            throw new InvalidArgumentException("参数resourceUrls错误");
        }

        return roleService.addPermission(id, resourceUrls);
    }

    @RequestMapping(value = "/{id}/users", method = RequestMethod.POST)
    public Map<String, List<Integer>> addUser(@PathVariable long id, @RequestParam List<Integer> userIds) {
        if (userIds.size() == 0) {
            throw new InvalidArgumentException("参数userIds错误");
        }

        return roleService.addUser(id, userIds);
    }
}
