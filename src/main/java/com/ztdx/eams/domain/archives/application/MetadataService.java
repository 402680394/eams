package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.model.Metadata;
import com.ztdx.eams.domain.archives.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 新增词典
     */
    @Transactional
    public void save(Metadata metadata) {
        if (metadataRepository.existsByName(metadata.getName())) {
            throw new InvalidArgumentException("字段名称已存在");
        }
        //设置排序优先级
        Integer orderNumber = metadataRepository.findMaxOrderNumber();
        if (orderNumber != null) {
            metadata.setOrderNumber(orderNumber + 1);
        } else {
            metadata.setOrderNumber(1);
        }
        //存储数据
        metadataRepository.save(metadata);
    }
    /**
     * 删除词典
     */
    @Transactional
    public void delete(int id) {
        if (metadataRepository.existsById(id)) {
            //删除词典
            metadataRepository.deleteById(id);
        }
    }
    /**
     * 修改词典
     */
    @Transactional
    public void update(Metadata metadata) {
        if (!metadataRepository.existsByNameAndId(metadata.getName(), metadata.getId())) {
            if (metadataRepository.existsByName(metadata.getName())) {
                throw new InvalidArgumentException("字段名称已存在");
            }
        }
        //修改数据
        if (metadataRepository.existsById(metadata.getId())) {
            metadataRepository.updateById(metadata);
        }
    }
    /**
     * 修改词典排序优先级
     */
    @Transactional
    public void priority(int upId, int downId) {

        Metadata up=metadataRepository.findById(upId);
        Metadata down=metadataRepository.findById(downId);
        if(up==null||down==null){
            throw new InvalidArgumentException("词典不存在或已被删除");
        }
        metadataRepository.updateOrderNumberById(upId,down.getOrderNumber());
        metadataRepository.updateOrderNumberById(downId,up.getOrderNumber());
    }
}
