package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.domain.system.model.Permission;
import com.ztdx.eams.domain.system.model.Resource;
import com.ztdx.eams.domain.system.model.ResourceCategory;
import com.ztdx.eams.domain.system.repository.PermissionRepository;
import com.ztdx.eams.domain.system.repository.ResourceRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private PermissionRepository permissionRepository;

    private ResourceRepository resourceRepository;

    public PermissionService(PermissionRepository permissionRepository, ResourceRepository resourceRepository) {
        this.permissionRepository = permissionRepository;
        this.resourceRepository = resourceRepository;
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
        result.put("type", resource.getResourceCategory().toString());
        result.put("children", new ArrayList<Map>());

        return result;
    }

    public void saveAll(List<Permission> permissions){
        //TODO lijie 排除已经存在的，删除取消的
        permissionRepository.saveAll(permissions);
    }
}
