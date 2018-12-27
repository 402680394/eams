package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.model.MetadataStandards;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import com.ztdx.eams.domain.archives.repository.MetadataRepository;
import com.ztdx.eams.domain.archives.repository.MetadataStandardsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Created by li on 2018/4/22.
 */
@Service
public class MetadataStandardsService {

    private final MetadataStandardsRepository metadataStandardsRepository;

    private final MetadataRepository metadataRepository;

    private final CatalogueRepository catalogueRepository;

    @Autowired
    public MetadataStandardsService(MetadataStandardsRepository metadataStandardsRepository, MetadataRepository metadataRepository, CatalogueRepository catalogueRepository) {
        this.metadataStandardsRepository = metadataStandardsRepository;
        this.metadataRepository = metadataRepository;
        this.catalogueRepository = catalogueRepository;
    }

    /**
     * 新增元数据规范
     */
    @Transactional
    public void save(MetadataStandards metadataStandards) {
        if (metadataStandardsRepository.existsByCode(metadataStandards.getCode())) {
            throw new InvalidArgumentException("编号已存在");
        }
        //排序
        Integer orderNumber = metadataStandardsRepository.findMaxOrderNumber();
        if (orderNumber != null) {
            metadataStandards.setOrderNumber(orderNumber + 1);
        } else {
            metadataStandards.setOrderNumber(1);
        }
        metadataStandardsRepository.save(metadataStandards);
    }

    /**
     * 删除元数据规范
     */
    @Transactional
    public void delete(int id) {
        Optional<MetadataStandards> optional = metadataStandardsRepository.findById(id);
        if (!optional.isPresent()) {
            throw new InvalidArgumentException("该项不存在或已被删除");
        }
        if(catalogueRepository.existsByMetadataStandardsId(id)){
            throw new BusinessException("元数据规范已被使用");
        }
        metadataRepository.deleteByMetadataStandardsId(id);
        metadataStandardsRepository.deleteById(id);
    }

    /**
     * 修改元数据规范
     */
    @Transactional
    public void update(MetadataStandards metadataStandards) {
        Optional<MetadataStandards> optional = metadataStandardsRepository.findById(metadataStandards.getId());
        if (!optional.isPresent()) {
            throw new InvalidArgumentException("该项不存在或已被删除");
        }
        if (!optional.get().getCode().equals(metadataStandards.getCode())) {
            if (metadataStandardsRepository.existsByCode(metadataStandards.getCode())) {
                throw new InvalidArgumentException("编号已存在");
            }
        }
        metadataStandardsRepository.updateById(metadataStandards);
    }

    /**
     * 修改元数据规范排序优先级
     */
    @Transactional
    public void priority(int upId, int downId) {

        Optional<MetadataStandards> up = metadataStandardsRepository.findById(upId);
        Optional<MetadataStandards> down = metadataStandardsRepository.findById(downId);
        if (!up.isPresent() || !down.isPresent()) {
            throw new InvalidArgumentException("该项不存在或已被删除");
        }
        metadataStandardsRepository.updateOrderNumberById(upId, down.get().getOrderNumber());
        metadataStandardsRepository.updateOrderNumberById(downId, up.get().getOrderNumber());
    }
}
