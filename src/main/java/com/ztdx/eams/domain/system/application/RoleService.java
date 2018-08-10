package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.system.model.*;
import com.ztdx.eams.domain.system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private RoleRepository roleRepository;

    private ResourceRepository resourceRepository;

    private PermissionRepository permissionRepository;

    private RoleOfUserRepository roleOfUserRepository;

    private FondsRepository fondsRepository;

    private UserRepository userRepository;

    private OrganizationRepository organizationRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository, ResourceRepository resourceRepository, PermissionRepository permissionRepository, RoleOfUserRepository roleOfUserRepository, FondsRepository fondsRepository, UserRepository userRepository, OrganizationRepository organizationRepository) {
        this.roleRepository = roleRepository;
        this.resourceRepository = resourceRepository;
        this.permissionRepository = permissionRepository;
        this.roleOfUserRepository = roleOfUserRepository;
        this.fondsRepository = fondsRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
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
    public Map<String, Object> addUser(long roleId, List<Integer> addUserIds) {
        List<RoleOfUser> existsUsers = roleOfUserRepository.findByRoleId(roleId);
        Set<Integer> existsUserIds = existsUsers.stream().map(RoleOfUser::getUserId).collect(Collectors.toSet());

        Set<Integer> addUserIdSet = addUserIds.stream().collect(Collectors.toSet());

        addUserIdSet.removeAll(existsUserIds);
        Date now = Calendar.getInstance().getTime();

        HashSet<RoleOfUser> batch = new HashSet<>();

        for (Integer userId :addUserIdSet){
            RoleOfUser user = new RoleOfUser();
            user.setRoleId(roleId);
            user.setUserId(userId);
            user.setGmtCreate(now);
            batch.add(user);
        }
        roleOfUserRepository.saveAll(batch);

        existsUserIds.removeAll(addUserIds);

        roleOfUserRepository.deleteInBatch(
                roleOfUserRepository.findByRoleIdAndUserIdIn(roleId, existsUserIds)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("added", addUserIdSet);
        result.put("deleted", existsUserIds);
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
     * 查找用户管理得档案目录id
     * @param userId 用户id
     * @return 档案目录id列表
     */
    public Set<Integer> findUserManageArchiveCatalogue(int userId){
        return findUserPermissions(userId, Permission::getArchiveId, Collectors.toSet());
    }

    /**
     * 查找用户管理得全宗id
     * @param userId 用户id
     * @return 全宗id列表
     */
    public Set<Integer> findUserManageFonds(int userId){
        return findUserPermissions(userId, Permission::getFondsId, Collectors.toSet());
    }

    private <M, A, R> R findUserPermissions(int userId, Function<Permission, M> map, Collector<M,A,R> to){
        HashSet<Long> roleIds = getUserRoleIds(userId);
        List<Permission> permissions = permissionRepository.findByRoleIdIn(roleIds);
        return permissions.stream().map(map).collect(to);
    }

    public HashSet<Long> getUserRoleIds(int userId) {
        List<RoleOfUser> roleOfUsers = roleOfUserRepository.findByUserId(userId);
        return roleOfUsers.stream().collect(HashSet::new,(a,b)->a.add(b.getRoleId()),HashSet::addAll);
    }

    public Map<String, Object> listRolePermission(long roleId) {
        List<Long> ids = new ArrayList<>();
        ids.add(roleId);
        return this.listRolePermissionArray(ids);
    }

    public Map<String, Object> listRolePermissionArray(Iterable<Long> roleIds) {
        List<Permission> permissions = permissionRepository.findByRoleIdIn(roleIds);
        Map<Long, Resource> resourceMap = resourceRepository.findAllById(
                permissions
                        .stream()
                        .map(Permission::getResourceId)
                        .collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Resource::getId,p -> p));

        Map<Integer, List<Long>> fonds = groupPermission(permissions
                , "fonds", a -> a.getFondsId() == null ? 0 : a.getFondsId(), resourceMap );

        Map<Integer, List<Long>> archive = groupPermission(permissions
                , "archive", a -> a.getArchiveId() == null ? 0 : a.getArchiveId(), resourceMap );

        return formatRolePermission(fonds, archive);
    }

    private <R> Map<String, Object> formatRolePermission(Map<Integer, R> fonds, Map<Integer, R> archive) {
        Map<String, Object> result = new HashMap<>();
        result.put("global", fonds.get(0));
        result.put("object", fonds.get(-1));
        fonds.remove(0);
        fonds.remove(-1);
        fonds.remove(-2);
        archive.remove(0);
        result.put("fonds", fonds);
        result.put("archiveCatalogue", archive);
        return result;
    }

    public Map<String, Object> listRolePermission(Iterable<Long> roleIds) {
        List<Permission> permissions = permissionRepository.findByRoleIdIn(roleIds);
        Map<Long, Resource> resourceMap = resourceRepository.findAllById(
                permissions
                        .stream()
                        .map(Permission::getResourceId)
                        .collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Resource::getId,p -> p));

        Map<Integer, Map<String, Object>> fonds = groupPermission(permissions
                , a -> {
                    String g = a.getResourceUrl().split("_")[0];
                    switch (g) {
                        case "global":
                            return 0;
                        case "object":
                            return -1;
                        default:
                            return a.getFondsId() != null ? a.getFondsId() : -2;
                    }
                }//a -> a.getFondsId() == null ? (a.getArchiveId() == null ? 0 : -1) : a.getFondsId()
                , resourceMap );

        Map<Integer, Map<String, Object>> archive = groupPermission(permissions
                , a -> a.getArchiveId() == null ? 0 : a.getArchiveId(), resourceMap );

        return formatRolePermission(fonds, archive);
    }

    public Map<String, Object> listUserPermission(int userId) {
        Iterable<Long> ids = this.getUserRoleIds(userId);
        return this.listRolePermission(ids);
    }

    public List<Permission> listUserPermissionSimple(int userId) {
        Iterable<Long> ids = this.getUserRoleIds(userId);
        return permissionRepository.findByRoleIdIn(ids);
    }

    private Map<Integer, List<Long>> groupPermission(
            List<Permission> permissions
            , String groupColumn
            , Function<Permission, Integer> groupKey
            , Map<Long, Resource> resourceMap
    ) {
        return permissions.stream().collect(
                HashMap::new,(a,b) -> {
                    Integer k = groupKey.apply(b);
                    Resource resource = resourceMap.getOrDefault(b.getResourceId(), null);
                    if (resource == null
                            || (!groupColumn.equals(resource.getResourceUrl().split("_")[0])
                                && !"global".equals(resource.getResourceUrl().split("_")[0])
                            || resource.getResourceCategory() != ResourceCategory.Function
                    )
                    ){
                        return;
                    }

                    List<Long> l = new ArrayList<>();
                    l.add(b.getResourceId());
                    a.merge(k, l, (o, n) -> {
                        o.addAll(n);
                        return o;
                    });
                },(a, b) -> b.forEach((bk, bv) ->{
                    a.merge(bk, bv, (o, n) -> {
                        o.addAll(n);
                        return o;
                    });
                }));
    }

    private Map<Integer, Map<String, Object>> groupPermission(
            List<Permission> permissions
            , Function<Permission, Integer> groupKey
            , Map<Long, Resource> resourceMap) {
        Map<Integer, List<Permission>> group = permissions.stream().collect(Collectors.groupingBy(groupKey));
        Map<Integer, Map<String, Object>> result = new HashMap<>();
        group.forEach((a, b) -> {
            result.put(a, b.stream().collect(
                    HashMap::new,(a1,b1) -> {
                        Resource resource = resourceMap.getOrDefault(b1.getResourceId(), null);
                        if (resource == null){
                            return;
                        }

                        a1.put(
                                PermissionKeyMap(b1, resourceMap)
                                , PermissionMap(b1, resourceMap)
                        );
                    }, HashMap::putAll
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
        if (permission.getResourceUrl().split("_")[0].equals("object")){
            return permission.getResourceUrl();
        }
        return resourceMap.get(permission.getResourceId()).getResourceUrl();
    }

    public List<Map<String, Object>> roleUsers(long roleId) {
        List<RoleOfUser> list = roleOfUserRepository.findByRoleId(roleId);
        Set<Integer> userids = list.stream().map(RoleOfUser::getUserId).collect(Collectors.toSet());
        List<User> users = userRepository.findAllById(userids);
        Set<Integer> organizationIds = users.stream().map(User::getOrganizationId).collect(Collectors.toSet());
        Map<Integer, String> organizations = organizationRepository.findAllById(organizationIds)
                .stream().collect(Collectors.toMap(Organization::getId, Organization::getName));

        return users.stream().map(u -> {
            Map<String, Object> result = new HashMap<>();
            result.put("id", u.getId());
            result.put("name", u.getName());
            result.put("organization", organizations.getOrDefault(u.getOrganizationId(), ""));
            return result;
        }).collect(Collectors.toList());
    }
}
