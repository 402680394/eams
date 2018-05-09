package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.model.MetadataStandards;
import com.ztdx.eams.domain.archives.repository.MetadataRepository;
import com.ztdx.eams.domain.archives.repository.MetadataStandardsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by li on 2018/4/22.
 */
@Service
public class MetadataStandardsService {

    private final MetadataStandardsRepository metadataStandardsRepository;

    private final MetadataRepository metadataRepository;

    @Autowired
    public MetadataStandardsService(MetadataStandardsRepository metadataStandardsRepository, MetadataRepository metadataRepository) {
        this.metadataStandardsRepository = metadataStandardsRepository;
        this.metadataRepository = metadataRepository;
    }

    /**
     * 新增元数据规范
     */
    @Transactional
    public void save(MetadataStandards metadataStandards) {
        if (metadataStandardsRepository.existsByCode(metadataStandards.getCode())) {
            throw new InvalidArgumentException("编号已存在");
        }
        //设置排序优先级
        Integer orderNumber = metadataStandardsRepository.findMaxOrderNumber();
        if (orderNumber != null) {
            metadataStandards.setOrderNumber(orderNumber + 1);
        } else {
            metadataStandards.setOrderNumber(1);
        }
        //存储数据
        metadataStandardsRepository.save(metadataStandards);
    }
    /**
     * 删除元数据规范
     */
    @Transactional
    public void delete(int id) {
        if (metadataStandardsRepository.existsById(id)) {
            //删除元数据规范下元数据
            metadataRepository.deleteByMetadataStandardsId(id);
            //删除元数据规范
            metadataStandardsRepository.deleteById(id);
        }
    }
    /**
     * 修改元数据规范
     */
    @Transactional
    public void update(MetadataStandards metadataStandards) {
        if (!metadataStandardsRepository.existsByCodeAndId(metadataStandards.getCode(), metadataStandards.getId())) {
            if (metadataStandardsRepository.existsByCode(metadataStandards.getCode())) {
                throw new InvalidArgumentException("编号已存在");
            }
        }
        //修改数据
        if (metadataStandardsRepository.existsById(metadataStandards.getId())) {
            metadataStandardsRepository.updateById(metadataStandards);
        }
    }
    /**
     * 修改元数据规范排序优先级
     */
    @Transactional
    public void priority(int upId, int downId) {

        MetadataStandards up=metadataStandardsRepository.findById(upId);
        MetadataStandards down=metadataStandardsRepository.findById(downId);
        if(up==null||down==null){
            throw new InvalidArgumentException("元数据规范不存在或已被删除");
        }
        metadataStandardsRepository.updateOrderNumberById(upId,down.getOrderNumber());
        metadataStandardsRepository.updateOrderNumberById(downId,up.getOrderNumber());
    }
}
