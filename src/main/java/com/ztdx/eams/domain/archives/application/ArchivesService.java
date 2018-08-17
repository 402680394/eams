package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.domain.archives.model.Archives;
import com.ztdx.eams.domain.archives.model.Catalogue;
import com.ztdx.eams.domain.archives.repository.ArchivesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 档案库服务
 */
@Service
public class ArchivesService {

    private final ArchivesRepository archivesRepository;

    /**
     * 构造函数
     */
    @Autowired
    public ArchivesService(ArchivesRepository archivesRepository) {
        this.archivesRepository = archivesRepository;
    }

    /**
     * 更新档案库
     */
    public void save(Archives archives) {
        archivesRepository.save(archives);
    }

    public Archives get(Integer id) {
        Optional<Archives> archives = archivesRepository.findById(id);
        return archives.get();
    }

    public List<Archives> findAllById(List<Integer> archiveIds) {
        return archivesRepository.findAllById(archiveIds);
    }

    public List<Archives> findByType(Integer archiveType) {
        return archivesRepository.findByType(archiveType);
    }

    public String findArchivesNameByCatalogue_CatalogueId(int catalogueId) {
        return archivesRepository.findArchivesNameByCatalogue_CatalogueId(catalogueId);
    }
}
