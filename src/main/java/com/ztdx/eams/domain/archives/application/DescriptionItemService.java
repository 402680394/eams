package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.domain.archives.repository.DescriptionItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
