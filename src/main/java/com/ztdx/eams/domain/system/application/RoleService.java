package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.system.model.Permission;
import com.ztdx.eams.domain.system.model.Role;
import com.ztdx.eams.domain.system.model.RoleOfUser;
import com.ztdx.eams.domain.system.repository.PermissionRepository;
import com.ztdx.eams.domain.system.repository.ResourceRepository;
import com.ztdx.eams.domain.system.repository.RoleOfUserRepository;
import com.ztdx.eams.domain.system.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class RoleService {

    private RoleRepository roleRepository;

    private ResourceRepository resourceRepository;

    private PermissionRepository permissionRepository;

    private RoleOfUserRepository roleOfUserRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository, ResourceRepository resourceRepository, PermissionRepository permissionRepository, RoleOfUserRepository roleOfUserRepository) {
        this.roleRepository = roleRepository;
        this.resourceRepository = resourceRepository;
        this.permissionRepository = permissionRepository;
        this.roleOfUserRepository = roleOfUserRepository;
    }

    public void save(Role role) {

        if (!resourceRepository.existsById(role.getResourceId())) {
            role.setResourceId(1L);
        }

        if (roleRepository.existsByRoleNameAndResourceId(role.getRoleName(), role.getResourceId())) {
            throw new InvalidArgumentException("角色名称已存在");
        }

        //设置创建时间
        role.setGmtCreate(Calendar.getInstance().getTime());

        //存储数据
        roleRepository.save(role);
    }

    @Transactional
    public void delete(long id) {
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);
        }
    }

    public void update(Role role) {

        if (roleRepository.existsById(role.getId())) {
            save(role);
            return;
        }

        if (roleRepository.existsByRoleNameAndResourceIdAndIdNot(role.getRoleName(), role.getResourceId(), role.getId())) {
            throw new InvalidArgumentException("角色名称已存在");
        }
        //修改数据
        roleRepository.updateById(role);
    }

    public Role getRole(long id) {
        Optional<Role> role = roleRepository.findById(id);
        return role.orElse(null);
    }

    @Transactional
    public Map<String, List<String>> addPermission(long roleId, List<String> resourceUrls) {
        List<Permission> existsPermissions = permissionRepository.findByRoleIdAndResourceUrlIn(roleId, resourceUrls);
        LinkedList<String> existsUrl = new LinkedList<>();
        for (Permission permission : existsPermissions) {
            existsUrl.add(permission.getResourceUrl());
        }

        resourceUrls.removeAll(existsUrl);
        Date now = Calendar.getInstance().getTime();

        HashSet<Permission> batch = new HashSet<>();

        for (String url :resourceUrls){
            Permission permission = new Permission();
            permission.setRoleId(roleId);
            permission.setResourceUrl(url);
            permission.setGmtCreate(now);
            batch.add(permission);
        }
        permissionRepository.saveAll(batch);

        Map<String, List<String>> result = new HashMap<>();
        result.put("added", resourceUrls);
        result.put("existed", existsUrl);
        return result;
    }

    @Transactional
    public Map<String, List<Integer>> addUser(long roleId, List<Integer> addUserIds) {
        List<RoleOfUser> existsUsers = roleOfUserRepository.findByRoleIdAndUserIdIn(roleId, addUserIds);
        LinkedList<Integer> existsUserIds = new LinkedList<>();
        for (RoleOfUser user : existsUsers) {
            existsUserIds.add(user.getUserId());
        }

        addUserIds.removeAll(existsUserIds);
        Date now = Calendar.getInstance().getTime();

        HashSet<RoleOfUser> batch = new HashSet<>();

        for (Integer userId :addUserIds){
            RoleOfUser user = new RoleOfUser();
            user.setRoleId(roleId);
            user.setUserId(userId);
            user.setGmtCreate(now);
            batch.add(user);
        }
        roleOfUserRepository.saveAll(batch);

        Map<String, List<Integer>> result = new HashMap<>();
        result.put("added", addUserIds);
        result.put("existed", existsUserIds);
        return result;
    }
}
