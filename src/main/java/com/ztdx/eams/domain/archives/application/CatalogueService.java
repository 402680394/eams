package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.domain.archives.model.Archives;
import com.ztdx.eams.domain.archives.model.Catalogue;
import com.ztdx.eams.domain.archives.model.CatalogueType;
import com.ztdx.eams.domain.archives.repository.ArchivesRepository;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogueService {
    private CatalogueRepository catalogueRepository;
    private ArchivesRepository archivesRepository;

    public CatalogueService(CatalogueRepository catalogueRepository, ArchivesRepository archivesRepository) {
        this.catalogueRepository = catalogueRepository;
        this.archivesRepository = archivesRepository;
    }

    public List<Catalogue> findAllById(Collection<Integer> ids) {
        return catalogueRepository.findAllById(ids);
    }

    public Catalogue get(int id) {
        return catalogueRepository.findById(id).orElse(null);
    }

    public Catalogue getFolderFileCatalogueByFolderCatalogueId(int id) {
        Catalogue folder = catalogueRepository.findById(id).orElse(null);
        if (folder == null) {
            return null;
        }
        Catalogue folderFile = catalogueRepository.findByArchivesIdAndCatalogueType(folder.getArchivesId(), CatalogueType.FolderFile).orElse(null);
        if (folderFile == null) {
            return null;
        }
        return folderFile;
    }

    public boolean exists(int id) {
        return catalogueRepository.existsById(id);
    }

    public Catalogue getMainCatalogue(int archivesId) {
        return catalogueRepository.findByArchivesIdAndCatalogueTypeNot(archivesId, CatalogueType.FolderFile).orElse(null);
    }


    public List<Catalogue> list(Integer archiveType) {
        List<Archives> archives = archivesRepository.findByTypeAndGmtDeleted(archiveType, 0);
        if (archives == null || archives.size() == 0) {
            return null;
        }
        List<Integer> ids = archives.stream().map(Archives::getId).collect(Collectors.toList());
        return catalogueRepository.findAllByArchivesIdIn(ids);
    }

    public List<Catalogue> getRelationCatalogueIds(List<Integer> mainCatalogueIds) {
        List<Integer> archiveIds = catalogueRepository.findAllById(mainCatalogueIds).stream().map(Catalogue::getArchivesId).collect(Collectors.toList());
        return catalogueRepository.findAllByArchivesIdInAndCatalogueType(archiveIds, CatalogueType.FolderFile);
    }
}
