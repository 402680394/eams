package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.application.task.EntryAsyncTask;
import com.ztdx.eams.domain.archives.model.*;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import com.ztdx.eams.domain.archives.repository.DescriptionItemRepository;
import com.ztdx.eams.domain.archives.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DescriptionItemService {

    private final DescriptionItemRepository descriptionItemRepository;


    private final MetadataRepository metadataRepository;

    private final CatalogueRepository catalogueRepository;

    @Autowired
    public DescriptionItemService(DescriptionItemRepository descriptionItemRepository, MetadataRepository metadataRepository, CatalogueRepository catalogueRepository) {
        this.descriptionItemRepository = descriptionItemRepository;
        this.metadataRepository = metadataRepository;
        this.catalogueRepository = catalogueRepository;
    }

    public <R> Map<String, R> list(int catalogueId, Function<DescriptionItem, R> map) {
        return descriptionItemRepository.findByCatalogueId(catalogueId).stream()
                .collect(
                        Collectors.toMap(
                                DescriptionItem::getMetadataName
                                , map
                                , (d1, d2) -> d2));
    }

    public List<DescriptionItem> findAllById(Collection<Integer> ids) {
        return descriptionItemRepository.findAllById(ids);
    }

    public List<DescriptionItem> findAllByCatalogueIdIn(Collection<Integer> ids) {
        return descriptionItemRepository.findByCatalogueIdIn(ids);
    }

    public List<DescriptionItem> findByCatalogueId(int catalogueId) {
        return descriptionItemRepository.findByCatalogueId(catalogueId);
    }


    public DescriptionItem findByCatalogueIdAndPropertyType(int catalogueId, PropertyType boxNumber) {
        return descriptionItemRepository.findByCatalogueIdAndPropertyType(catalogueId, boxNumber);
    }

    public void delete(List<Integer> ids) {
        descriptionItemRepository.deleteByIdIn(ids);
    }

    @Transactional
    public List<DescriptionItem> save(int catalogueId, List<Integer> metadataIds) {
        Catalogue catalogue = catalogueRepository.findById(catalogueId).orElse(null);
        if (null == catalogue) {
            throw new BusinessException("目录不存在");
        }

        List<Metadata> metadatas = metadataRepository.findByIdIn(metadataIds);
        List<DescriptionItem> descriptionItems = new ArrayList<>();
        int metadataStandardsId = metadatas.get(0).getMetadataStandardsId();

        if (null == catalogue.getMetadataStandardsId()) {
            catalogue.setMetadataStandardsId(metadataStandardsId);
            catalogueRepository.save(catalogue);
        } else {
            metadataStandardsId = catalogue.getMetadataStandardsId();
        }

        for (Metadata metadata : metadatas) {
            if (metadataStandardsId != metadata.getMetadataStandardsId()) {
                throw new InvalidArgumentException("元数据不在同一个元数据规范中");
            }
            DescriptionItem descriptionItem = new DescriptionItem();
            descriptionItem.setCatalogueId(catalogueId);
            descriptionItem.setMetadataId(metadata.getId());
            descriptionItem.setMetadataName(metadata.getName());
            descriptionItem.setDisplayName(metadata.getDisplayName());
            descriptionItem.setPropertyType(metadata.getFieldProperties());
            descriptionItem.setDefaultValue(metadata.getDefaultValue());
            descriptionItem.setDataType(metadata.getDataType());
            descriptionItem.setFieldWidth(metadata.getFieldWidth());
            descriptionItem.setFieldPrecision(metadata.getFieldPrecision());

            descriptionItems.add(descriptionItem);
        }
        return descriptionItemRepository.saveAll(descriptionItems);
    }

    @Transactional
    public void update(DescriptionItem descriptionItem) {
        if (DescriptionItemDataType.Integer != descriptionItem.getDataType() && 1 == descriptionItem.getIsIncrement()) {
            throw new BusinessException("设置自增时，数据类型必须为数值 ");
        }
        descriptionItemRepository.updateById(descriptionItem);
    }

    public List<DescriptionItem> findByMetadataId(int metadataId){
        return descriptionItemRepository.findByMetadataId(metadataId);
    }

}
