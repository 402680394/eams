package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.domain.archives.application.event.ArchivesGroupDeleteEvent;
import com.ztdx.eams.domain.archives.model.ArchivesGroup;
import com.ztdx.eams.domain.archives.repository.ArchivesGroupRepository;
import com.ztdx.eams.domain.archives.repository.ArchivesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public ArchivesGroup get(int id) {
        return archivesGroupRepository.findById(id).orElse(null);
    }

    @Transactional
    public void save(ArchivesGroup archivesGroup) {
        archivesGroupRepository.save(archivesGroup);
    }

    @Transactional
    public void delete(int id) {
        archivesGroupRepository.setDeleteById(id, 1);
        archivesRepository.setDeleteByArchivesGroupId(id, 1);
    }

    @Transactional
    public void update(ArchivesGroup archivesGroup) {
        archivesGroupRepository.update(archivesGroup);
    }

    @EventListener
    public void validateBoxNumber(ArchivesGroupDeleteEvent event) {
        archivesGroupRepository.setDeleteByFondsId(event.getFondsId(), 1);
    }
}
