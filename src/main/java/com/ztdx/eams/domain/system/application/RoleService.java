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
    public Map<String, List<String>> addPermission(long roleId, Map<String, Object> permissions) {
        /*List<String> existsUrl = permissionRepository.findByRoleIdAndResourceUrlIn(roleId, resourceUrls).stream().map(Object::toString).collect(Collectors.toList());

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
        return result;*/
        return null;
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

    public List<Role> findByFondsIdIsNull() {
        return roleRepository.findByFondsIdIsNull();
    }

    public List<Role> findByFondsIdIsNotNull() {
        return roleRepository.findByFondsIdIsNotNull();
    }

    /**
     * 查找用户管理得全宗id
     * @param userId 用户id
     * @return 全宗id列表
     */
    public Set<Integer> findUserManageFonds(int userId){
        HashSet<Long> roleIds = getUserRoleIds(userId);
        List<Permission> permissions = permissionRepository.findByRoleIdIn(roleIds);
        return permissions.stream().collect(HashSet::new,(a, b)->{
            if (b.getFondsId() != null) {
                a.add(b.getFondsId());
            }
        },HashSet::addAll);
    }

    public HashSet<Long> getUserRoleIds(int userId) {
        List<RoleOfUser> roleOfUsers = roleOfUserRepository.findByUserId(userId);
        return roleOfUsers.stream().collect(HashSet::new,(a,b)->a.add(b.getRoleId()),HashSet::addAll);
    }

    public Map<String, Map> listRolePermission(long roleId) {
        List<Long> ids = new ArrayList<>();
        ids.add(roleId);
        return this.listRolePermission(ids);
    }

    public Map<String, Map> listRolePermission(Iterable<Long> roleIds) {
        List<Permission> permissions = permissionRepository.findByRoleIdIn(roleIds);
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

    public Map<String, Map> listUserPermission(int userId) {
        Iterable<Long> ids = this.getUserRoleIds(userId);
        return this.listRolePermission(ids);
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
        map.put("id", permission.getResourceId());
        map.put("name", resourceMap.get(permission.getResourceId()).getResourceName());
        map.put("resourceUrl", resourceMap.get(permission.getResourceId()).getResourceUrl());

        return map;
    }

    private String PermissionKeyMap(Permission permission, Map<Long, Resource> resourceMap){
        return resourceMap.get(permission.getResourceId()).getResourceUrl();
    }
}
