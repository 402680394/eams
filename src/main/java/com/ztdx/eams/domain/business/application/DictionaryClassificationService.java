package com.ztdx.eams.domain.business.application;

import com.ztdx.eams.domain.business.repository.DictionaryClassificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by li on 2018/4/18.
 */
@Service
public class DictionaryClassificationService {

    private final DictionaryClassificationRepository dictionaryClassificationRepository;

    @Autowired
    public DictionaryClassificationService(DictionaryClassificationRepository dictionaryClassificationRepository) {
        this.dictionaryClassificationRepository = dictionaryClassificationRepository;
    }
}
