package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.archives.repository.DescriptionItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by li on 2018/5/15.
 */
@Service
public class DescriptionItemService {

    private final DescriptionItemRepository descriptionItemRepository;

    @Autowired
    public DescriptionItemService(DescriptionItemRepository descriptionItemRepository) {
        this.descriptionItemRepository = descriptionItemRepository;
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
