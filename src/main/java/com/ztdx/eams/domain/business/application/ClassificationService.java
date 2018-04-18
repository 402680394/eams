package com.ztdx.eams.domain.business.application;

import com.ztdx.eams.domain.business.repository.ClassificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by li on 2018/4/18.
 */
@Service
public class ClassificationService {

    private final ClassificationRepository classificationRepository;

    @Autowired
    public ClassificationService(ClassificationRepository classificationRepository) {
        this.classificationRepository = classificationRepository;
    }
}
