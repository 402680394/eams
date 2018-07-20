package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.domain.archives.model.ArchivesGroup;
import com.ztdx.eams.domain.archives.repository.ArchivesGroupRepository;
import com.ztdx.eams.domain.archives.repository.ArchivesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by li on 2018/5/3.
 */
@Service
public class ArchivesGroupService {

    private final ArchivesRepository archivesRepository;

    private final ArchivesGroupRepository archivesGroupRepository;

    @Autowired
    public ArchivesGroupService(ArchivesRepository archivesRepository, ArchivesGroupRepository archivesGroupRepository) {
        this.archivesRepository = archivesRepository;
        this.archivesGroupRepository = archivesGroupRepository;
    }

    public List<ArchivesGroup> findAllById(List<Integer> archiveGroupIds) {
        return archivesGroupRepository.findAllById(archiveGroupIds);
    }

    public ArchivesGroup get(int id){
        return archivesGroupRepository.findById(id).orElse(null);
    }
}
