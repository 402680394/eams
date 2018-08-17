package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.basic.spel.TemplateParserContext;
import com.ztdx.eams.domain.system.model.Permission;
import com.ztdx.eams.domain.system.model.Resource;
import com.ztdx.eams.domain.system.model.ResourceCategory;
import com.ztdx.eams.domain.system.model.UserPermission;
import com.ztdx.eams.domain.system.repository.PermissionRepository;
import com.ztdx.eams.domain.system.repository.ResourceRepository;
import com.ztdx.eams.domain.system.repository.UserPermissionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.expression.ExpressionParser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private PermissionRepository permissionRepository;

    private ResourceRepository resourceRepository;

    private ExpressionParser parser;

    private UserPermissionRepository userPermissionRepository;

    public PermissionService(PermissionRepository permissionRepository, ResourceRepository resourceRepository, ExpressionParser parser, UserPermissionRepository userPermissionRepository) {
        this.permissionRepository = permissionRepository;
        this.resourceRepository = resourceRepository;
        this.parser = parser;
        this.userPermissionRepository = userPermissionRepository;
    }

    public List<Map> listCategoryPermission(ResourceCategory resourceCategory) {
        List<Resource> list = resourceRepository.findAll(Sort.by("orderNumber"));
        Optional<Resource> first = list.stream().filter(a -> a.getResourceCategory() == resourceCategory).findFirst();
        if (!first.isPresent()){
            return null;
        }
        Resource top = first.get();
        long topId = top.getId();
        List<Map> container = new ArrayList<>();
        makeTree(list, topId, container);
        return container;
    }

    private void makeTree(List<Resource> list, long parentId, List<Map> container) {

        List<Map> result = list.stream()
                .filter(a -> a.getParentId()!= null && a.getParentId() == parentId)
                .map(this::mapResource)
                .collect(Collectors.toList());

        result.forEach(a -> {
            long id = (long) a.get("id");
            List<Map> map = (List<Map>) a.get("children");
            makeTree(list, id, map);
        });
        container.addAll(result);
    }

    private Map mapResource(Resource resource) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", resource.getId());
        result.put("name", resource.getResourceName());
        result.put("value", resource.getId());
        result.put("label", resource.getResourceName());
        result.put("type", resource.getResourceCategory().toString());
        result.put("children", new ArrayList<Map>());
        if (resource.getResourceCategory() == ResourceCategory.Function){
            result.put("resourceUrl", resource.getResourceUrl());
        }

        return result;
    }

    @Transactional
    public void saveAll(List<Permission> permissions){

        Map<String, List<Permission>> list= permissions.stream().collect(
                Collectors.groupingBy(this::getGroupKey));

        List<Long> roleIds = permissions.stream().map(Permission::getRoleId).collect(Collectors.toList());

        Map<String, List<Permission>> existsPermissions = permissionRepository.findByRoleIdIn(
                roleIds).stream().collect(Collectors.groupingBy(this::getGroupKey));

        List<Permission> save = new ArrayList<>();
        List<Permission> delete = new ArrayList<>();

        list.forEach((groupKey, rolePermissions) ->{
            if (!existsPermissions.containsKey(groupKey)){
                save.addAll(rolePermissions);
            }
        });

        existsPermissions.forEach((groupKey, rolePermissions) -> {
            if (!list.containsKey(groupKey)){
                delete.addAll(rolePermissions);
            }
        });

        save.forEach(a -> {
            a.setResourceUrl(
                    parser.parseExpression(
                            a.getResourceUrl()
                            , new TemplateParserContext()
                    ).getValue(a, String.class)
            );
        });

        permissionRepository.saveAll(save);
        permissionRepository.deleteInBatch(delete);
    }

    public boolean hasAuthority(String expectedAuthority){
        return this.hasAnyAuthority(expectedAuthority);
    }

    public boolean hasAnyAuthority(String... expectedAuthorities) {

        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        for (String role : expectedAuthorities) {
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals(role))) {
                return true;
            }
        }
        return false;
    }

    private String getGroupKey(Permission permission){
        StringBuilder sb = new StringBuilder();
        sb.append(permission.getRoleId());

        sb.append("_");
        sb.append(permission.getResourceId());

        Integer fondsId = permission.getFondsId();
        if (fondsId != null) {
            sb.append("_");
            sb.append(permission.getFondsId());
        }

        Integer archievId = permission.getArchiveId();
        if (archievId != null) {
            sb.append("_");
            sb.append(permission.getArchiveId());
        }

        return sb.toString();
    }

    public void addUserPermission(int userId, Collection<String> resourceUrls, int days){
        addUserPermission(userId, resourceUrls, Date.from(Instant.now().plus(days, ChronoUnit.DAYS)));
    }

    public void addUserPermission(int userId, Collection<String> resourceUrls, Date expiryTime){
        List<UserPermission> list = new ArrayList<>();

        List<UserPermission> exists = userPermissionRepository.findByUserIdAndResourceUrlInAndExpiryTimeGreaterThanEqual(userId, resourceUrls, Date.from(Instant.now()));

        Set<String> existsKey = exists.stream().map(UserPermission::getResourceUrl).collect(Collectors.toSet());

        resourceUrls.forEach(resourceUrl -> {
            if (existsKey.contains(resourceUrl)){
                return;
            }

            Date time;
            if (expiryTime == null){
                time = Date.from(Instant.now().plus(10, ChronoUnit.YEARS));
            }else{
                time = expiryTime;
            }
            UserPermission userPermission = new UserPermission();
            userPermission.setUserId(userId);
            userPermission.setExpiryTime(time);
            userPermission.setResourceUrl(resourceUrl);
            userPermission.setGmtCreate(Date.from(Instant.now()));

            list.add(userPermission);
        });

        userPermissionRepository.saveAll(list);
    }

    public List<UserPermission> listUserPermission(int userId){
        return userPermissionRepository.findByUserIdAndExpiryTimeGreaterThanEqual(userId, Date.from(Instant.now()));
    }
}
