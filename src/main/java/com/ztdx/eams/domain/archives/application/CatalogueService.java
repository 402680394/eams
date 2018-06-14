package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.model.Catalogue;
import com.ztdx.eams.domain.archives.model.CatalogueType;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class CatalogueService {
    private CatalogueRepository catalogueRepository;

    public CatalogueService(CatalogueRepository catalogueRepository) {
        this.catalogueRepository = catalogueRepository;
    }

    public List<Catalogue> findAllById(Collection<Integer> ids){
        return catalogueRepository.findAllById(ids);
    }

    public Catalogue get(int id){
        return catalogueRepository.findById(id).orElse(null);
    }

    public Catalogue getFolderFileCatalogueByFolderCatalogueId(int id){
        Catalogue folder = catalogueRepository.findById(id).orElse(null);
        if (folder == null){
            throw new InvalidArgumentException("目录未找到");
        }
        Catalogue folderFile = catalogueRepository.findByArchivesIdAndCatalogueType(folder.getArchivesId(), CatalogueType.FolderFile).orElse(null);
        if (folderFile == null){
            throw new InvalidArgumentException("卷内目录未找到");
        }
        return folderFile;
    }
}
