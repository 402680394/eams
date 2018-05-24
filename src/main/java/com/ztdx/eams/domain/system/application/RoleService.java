package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.system.model.*;
import com.ztdx.eams.domain.system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private RoleRepository roleRepository;

    private ResourceRepository resourceRepository;

    private PermissionRepository permissionRepository;

    private RoleOfUserRepository roleOfUserRepository;

    private FondsRepository fondsRepository;

    private UserRepository userRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository, ResourceRepository resourceRepository, PermissionRepository permissionRepository, RoleOfUserRepository roleOfUserRepository, FondsRepository fondsRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.resourceRepository = resourceRepository;
        this.permissionRepository = permissionRepository;
        this.roleOfUserRepository = roleOfUserRepository;
        this.fondsRepository = fondsRepository;
        this.userRepository = userRepository;
    }

    public void save(Role role) {

        //if (!resourceRepository.existsById(role.getFondsId())) {
            //role.setFondsId(null);
        //}

        if (role.getFondsId() != null
                && roleRepository.existsByRoleNameAndFondsId(role.getRoleName(), role.getFondsId())
                || role.getFondsId() == null
                && roleRepository.existsByRoleNameAndFondsIdIsNull(role.getRoleName())
                ) {
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

    @Transactional
    public void update(Role role) {

        if (!roleRepository.existsById(role.getId())) {
            save(role);
            return;
        }

        if (role.getFondsId() != null
                && roleRepository.existsByRoleNameAndFondsIdAndIdNot(role.getRoleName(), role.getFondsId(), role.getId())
                || role.getFondsId() == null
                && roleRepository.existsByRoleNameAndFondsIdIsNullAndIdNot(role.getRoleName(), role.getId())
                ) {
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
        List<String> existsUrl = permissionRepository.findByRoleIdAndResourceUrlIn(roleId, resourceUrls).stream().map(Object::toString).collect(Collectors.toList());

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

        //删除掉取消的权限
        List<Permission> delPermission = permissionRepository.findByRoleIdAndResourceUrlNotIn(roleId, resourceUrls);
        permissionRepository.deleteInBatch(delPermission);

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


    public List<Role> findByFondsIdIn(Iterable<Integer> fondsId) {
        return roleRepository.findByFondsIdIn(fondsId);
    }

    /**
     * 获取用户可以管理的全宗，以及全宗下的角色
     * @param userId 用户id
     * @return 全宗的数组，包括全宗的角色
     */
    public List<Object> listGlobalRole(int userId) {

        //TODO lijie 判断用户权限，如果没有全局的权限管理权限则返回null
        List<Role> roles = roleRepository.findByFondsIdIsNull();

        return roles.stream().map(this::getRoleMap).collect(Collectors.toList());
    }

    private Map<String, Object> getRoleMap(Role a) {
        Map<String, Object> mapb = new HashMap<>();
        mapb.put("id", a.getId());
        mapb.put("name", a.getRoleName());
        mapb.put("type", "Role");
        return mapb;
    }

    /**
     * 获取用户可以管理的全宗，以及全宗下的角色
     * @param userId 用户id
     * @return 全宗的数组，包括全宗的角色
     */
    public List<Object> listFondsRole(int userId) {
        //查询出当前用户可以管理的全宗
        //并把角色挂到全宗树上

        //TODO lijie 如果是超级管理员则查看所有全宗
        Set<Integer> fondsIds = this.findUserManageFonds(userId);

        List<Role> roles = this.findByFondsIdIn(fondsIds);

        List<Fonds> fonds = fondsRepository.findAllById(fondsIds);

        Map<Integer, List<Role>> fondsGroup = roles.stream().collect(Collectors.groupingBy(Role::getFondsId));

        return fonds.stream().map((a) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("name", a.getName());
            map.put("type", "Fonds");
            map.put("allowAdd", true);
            List<Role> childrenList = fondsGroup.getOrDefault(a.getId(),null);
            List<Object> children = childrenList.stream().map(this::getRoleMap).collect(Collectors.toList());
            map.put("children", children);
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 查找用户管理得全宗id
     * @param userId 用户id
     * @return 全宗id列表
     */
    public Set<Integer> findUserManageFonds(int userId){
        List<RoleOfUser> roleOfUsers = roleOfUserRepository.findByUserId(userId);
        HashSet<Long> roleIds = roleOfUsers.stream().collect(HashSet::new,(a,b)->a.add(b.getRoleId()),HashSet::addAll);
        List<Permission> permissions = permissionRepository.findByRoleIdIn(roleIds);
        return permissions.stream().collect(HashSet::new,(a, b)->{
            if (b.getFondsId() != null) {
                a.add(b.getFondsId());
            }
        },HashSet::addAll);
    }

    public Map<String, Map> listRolePermission(long roleId) {
        List<Permission> permissions = permissionRepository.findByRoleId(roleId);
        Map<Long, Resource> resourceMap = resourceRepository.findAllById(
                permissions
                        .stream()
                        .map(Permission::getResourceId)
                        .collect(Collectors.toList()))
        .stream().collect(Collectors.toMap(Resource::getId,p -> p));

        Map<Integer, Map<String, Object>> fonds = groupPermission(permissions
                , a -> a.getFondsId() == null ? 0 : a.getFondsId(), resourceMap );

        Map<Integer, Map<String, Object>> archive = groupPermission(permissions
                , a -> a.getArchiveId() == null ? 0 : a.getArchiveId(), resourceMap );

        Map<String, Map> result = new HashMap<>();
        result.put("global", fonds.get(0));
        fonds.remove(0);
        archive.remove(0);
        result.put("fonds", fonds);
        result.put("archiveCatalogue", archive);
        return result;
    }

    private Map<Integer, Map<String, Object>> groupPermission(List<Permission> permissions, Function<Permission, Integer> groupKey, Map<Long, Resource> resourceMap) {
        Map<Integer, List<Permission>> group = permissions.stream().collect(Collectors.groupingBy(groupKey));
        Map<Integer, Map<String, Object>> result = new HashMap<>();
        group.forEach((a, b) -> {
            result.put(a, b.stream().collect(
                    Collectors.toMap(
                            p -> PermissionKeyMap(p, resourceMap), p -> PermissionMap(p, resourceMap))
            ));
        });
        return result;
    }

    private Map<String, Object> PermissionMap(Permission permission, Map<Long, Resource> resourceMap){
        Map<String, Object> map = new HashMap<>();
        map.put("id", permission.getId());
        map.put("name", resourceMap.get(permission.getResourceId()).getResourceName());
        map.put("resourceUrl", permission.getResourceUrl());

        return map;
    }

    private String PermissionKeyMap(Permission permission, Map<Long, Resource> resourceMap){
        return resourceMap.get(permission.getResourceId()).getResourceUrl();
    }
}
