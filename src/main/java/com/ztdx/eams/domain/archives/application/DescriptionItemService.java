package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.domain.archives.model.DescriptionItem;
import org.springframework.beans.factory.annotation.Autowired;
import com.ztdx.eams.domain.archives.repository.DescriptionItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DescriptionItemService {

    private final DescriptionItemRepository descriptionItemRepository;

    @Autowired
    public DescriptionItemService(DescriptionItemRepository descriptionItemRepository) {
        this.descriptionItemRepository = descriptionItemRepository;
    }

    public Map<String, DescriptionItem> list(int catalogueId){
        return descriptionItemRepository.findByCatalogueId(catalogueId).stream().collect(Collectors.toMap(DescriptionItem::getMetadataName,(d) -> d, (d1,d2)->d2));
    }

    //条目数据验证
    public boolean Verification(int catalogueId, Map<String, Object> dataMap) {
        //获取
        List<DescriptionItem> descriptionItemList = descriptionItemRepository.findByCatalogueId(catalogueId);
        //遍历目录著录项信息
        for (DescriptionItem descriptionItem : descriptionItemList) {

        }
        return true;
    }
}
