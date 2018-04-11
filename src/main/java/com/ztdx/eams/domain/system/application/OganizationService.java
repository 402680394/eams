package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.domain.system.model.Oganization;
import com.ztdx.eams.domain.system.repository.OganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by li on 2018/4/11.
 */
@Service
public class OganizationService {
    private final OganizationRepository oganizationRepository;

    @Autowired
    public OganizationService(OganizationRepository oganizationRepository) {
        this.oganizationRepository = oganizationRepository;
    }

    public void save(Oganization oganization) {
    }

    public void delete(int id) {
    }

    public void update(Oganization oganization) {
    }
}
