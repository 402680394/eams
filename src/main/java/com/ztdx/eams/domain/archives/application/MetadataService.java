package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.model.Metadata;
import com.ztdx.eams.domain.archives.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Created by li on 2018/4/22.
 */
@Service
public class MetadataService {

    private final MetadataRepository metadataRepository;

    @Autowired
    public MetadataService(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    /**
     * 新增
     */
    @Transactional
    public void save(Metadata metadata) {
        if (metadataRepository.existsByName(metadata.getName())) {
            throw new InvalidArgumentException("名称已存在");
        }
        if (metadataRepository.existsByDisplayName(metadata.getDisplayName())) {
            throw new InvalidArgumentException("显示名称已存在");
        }
        Integer orderNumber = metadataRepository.findMaxOrderNumber();
        if (orderNumber != null) {
            orderNumber++;
        } else {
            orderNumber=1;
        }
        metadata.setOrderNumber(orderNumber);
        metadataRepository.save(metadata);
    }

    /**
     * 删除
     */
    @Transactional
    public void delete(int id) {
        if (!metadataRepository.findById(id).isPresent()) {
            throw new InvalidArgumentException("数据不存在或已被删除");
        }
        metadataRepository.deleteById(id);
    }

    /**
     * 修改
     */
    @Transactional
    public void update(Metadata metadata) {
        if (!metadataRepository.findById(metadata.getId()).isPresent()) {
            throw new InvalidArgumentException("数据不存在或已被删除");
        }
        if (!metadataRepository.existsByNameAndId(metadata.getName(), metadata.getId())) {
            if (metadataRepository.existsByName(metadata.getName())) {
                throw new InvalidArgumentException("名称已存在");
            }
        }
        if (!metadataRepository.existsByDisplayNameAndId(metadata.getDisplayName(), metadata.getId())) {
            if (metadataRepository.existsByDisplayName(metadata.getDisplayName())) {
                throw new InvalidArgumentException("显示名称已存在");
            }
        }
        metadataRepository.updateById(metadata);
    }

    /**
     * 排序
     */
    @Transactional
    public void priority(int upId, int downId) {

        Optional<Metadata> up = metadataRepository.findById(upId);
        Optional<Metadata> down = metadataRepository.findById(downId);
        if (!up.isPresent() || !down.isPresent()) {
            throw new InvalidArgumentException("数据不存在或已被删除");
        }
        metadataRepository.updateOrderNumberById(upId, down.get().getOrderNumber());
        metadataRepository.updateOrderNumberById(downId, up.get().getOrderNumber());
    }
}
