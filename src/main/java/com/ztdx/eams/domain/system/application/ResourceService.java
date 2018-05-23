package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.system.model.Resource;
import com.ztdx.eams.domain.system.repository.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.List;

@Service
public class ResourceService {

    private ResourceRepository resourceRepository;

    @Autowired
    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }


    public void save(Resource resource) {
        if (resourceRepository.existsByResourceUrl(resource.getResourceUrl())) {
            throw new InvalidArgumentException("资源路径已存在");
        }

        if (resource.getParentId() != null && !resourceRepository.existsById(resource.getParentId())) {
            throw new InvalidArgumentException("上级节点id不存在");
        }

        //设置同级机构优先级
        Integer orderNumber = resourceRepository.findMaxOrderNumber(resource.getParentId() == null ? 0 : resource.getParentId(), resource.getResourceCategory());
        if (orderNumber != null) {
            resource.setOrderNumber(orderNumber + 1);
        } else {
            resource.setOrderNumber(1);
        }
        //设置创建时间
        resource.setGmtCreate(Calendar.getInstance().getTime());

        //存储数据
        resourceRepository.save(resource);
    }

    @Transactional
    public void delete(long id) {
        if (resourceRepository.existsById(id)) {
            List<Resource> list = resourceRepository.findAllByParentId(id);
            if (!list.isEmpty()) {
                for (Resource f : list) {
                    delete(f.getId());
                }
            }
            resourceRepository.deleteById(id);
        }
    }

    public void update(Resource resource) {
        if (resourceRepository.existsById(resource.getId())) {
            save(resource);
            return;
        }

        Resource find = resourceRepository.findByResourceUrl(resource.getResourceUrl());
        if (find != null && find.getId() != resource.getId()) {
            throw new InvalidArgumentException("资源路径已存在");
        }

        if (resource.getParentId() != null && !resourceRepository.existsById(resource.getParentId())) {
            throw new InvalidArgumentException("上级节点id不存在");
        }

        //修改数据
        resourceRepository.updateById(resource);
    }

    public void priority(long upId, long downId) {

        Resource up = resourceRepository.findById(upId);
        Resource down = resourceRepository.findById(downId);

        if (up == null || down == null) {
            throw new InvalidArgumentException("资源不存在");
        }

        if (!up.getParentId().equals(down.getParentId())) {
            throw new InvalidArgumentException("不在一个节点上");
        }
        resourceRepository.updateOrderNumberById(upId, down.getOrderNumber());
        resourceRepository.updateOrderNumberById(downId, up.getOrderNumber());
    }

    public Resource getResource(long id) {
        return resourceRepository.findById(id);
    }
}
