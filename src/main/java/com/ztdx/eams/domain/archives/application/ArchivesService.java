package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.domain.archives.application.task.EntryAsyncTask;
import com.ztdx.eams.domain.archives.model.Archives;
import com.ztdx.eams.domain.archives.model.Catalogue;
import com.ztdx.eams.domain.archives.model.CatalogueType;
import com.ztdx.eams.domain.archives.repository.ArchivesRepository;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 档案库服务
 */
@Service
public class ArchivesService {

    private final ArchivesRepository archivesRepository;

    private final CatalogueRepository catalogueRepository;

    private final EntryAsyncTask entryAsyncTask;

    /**
     * 构造函数
     */
    @Autowired
    public ArchivesService(ArchivesRepository archivesRepository, CatalogueRepository catalogueRepository, EntryAsyncTask entryAsyncTask) {
        this.archivesRepository = archivesRepository;
        this.catalogueRepository = catalogueRepository;
        this.entryAsyncTask = entryAsyncTask;
    }

    /**
     * 创建档案库
     */
    @Transactional
    public List<Integer> save(Archives archives) {
        archives = archivesRepository.save(archives);
        List<Integer> catalogueIds = new ArrayList<>();
        switch (archives.getStructure()) {
            case ArticleOne: {
                Catalogue file = new Catalogue();
                file.setArchivesId(archives.getId());
                file.setCatalogueType(CatalogueType.File);
                file = catalogueRepository.save(file);
                catalogueIds.add(file.getId());
                break;
            }
            case TraditionalArchives: {
                Catalogue folder = new Catalogue();
                folder.setArchivesId(archives.getId());
                folder.setCatalogueType(CatalogueType.Folder);
                folder = catalogueRepository.save(folder);
                catalogueIds.add(folder.getId());

                Catalogue folderFile = new Catalogue();
                folderFile.setArchivesId(archives.getId());
                folderFile.setCatalogueType(CatalogueType.FolderFile);
                folderFile = catalogueRepository.save(folderFile);
                catalogueIds.add(folderFile.getId());
                break;
            }
            case Project: {
                Catalogue project = new Catalogue();
                project.setArchivesId(archives.getId());
                project.setCatalogueType(CatalogueType.Subject);
                project = catalogueRepository.save(project);
                catalogueIds.add(project.getId());
                break;
            }
        }
        return catalogueIds;
    }

    public Archives get(Integer id) {
        Optional<Archives> archives = archivesRepository.findById(id);
        return archives.get();
    }

    public List<Archives> findAllById(List<Integer> archiveIds) {
        return archivesRepository.findAllById(archiveIds);
    }


    public String findArchivesNameByCatalogue_CatalogueId(int catalogueId) {
        return archivesRepository.findArchivesNameByCatalogue_CatalogueId(catalogueId);
    }

    @Transactional
    public void delete(int id) {
        archivesRepository.setDeleteById(id, 1);
    }

    @Transactional
    public void update(Archives archives) {
        archivesRepository.update(archives);
    }
}
