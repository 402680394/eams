package com.ztdx.eams.domain.business.application;

import com.ztdx.eams.domain.business.repository.DictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by li on 2018/4/18.
 */
@Service
public class DictionaryService {

    private final DictionaryRepository dictionaryRepository;

    @Autowired
    public DictionaryService(DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
    }
}
