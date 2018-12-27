package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.application.event.ArchivesGroupDeleteEvent;
import com.ztdx.eams.domain.archives.model.Archives;
import com.ztdx.eams.domain.archives.model.ArchivesGroup;
import com.ztdx.eams.domain.archives.repository.ArchivesGroupRepository;
import com.ztdx.eams.domain.archives.repository.ArchivesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
        List<Archives> archives = archivesRepository.findByArchivesGroupIdAndGmtDeleted(id, 0);
        List<ArchivesGroup> archivesGroups = archivesGroupRepository.findByParentIdAndGmtDeleted(id, 0);
        if (archives.size() != 0 && archivesGroups.size() != 0) {
            throw new BusinessException("该分组下存在子分组或档案库");
        }
        Optional<ArchivesGroup> optional = archivesGroupRepository.findById(id);
        if (!optional.isPresent() || optional.get().getGmtDeleted() == 1) {
            throw new InvalidArgumentException("该项不存在或已被删除");
        }
        archivesGroupRepository.setDeleteById(id, 1);
//        archivesRepository.setDeleteByArchivesGroupId(id, 1);
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
