package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.domain.archives.repository.ArchivesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by li on 2018/5/3.
 */
@Service
public class ArchivesGroupService {

    private final ArchivesRepository archivesRepository;
    @Autowired
    public ArchivesGroupService(ArchivesRepository archivesRepository) {
        this.archivesRepository = archivesRepository;
    }
}
