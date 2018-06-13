package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.Catalogue;
import com.ztdx.eams.domain.archives.model.CatalogueType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;
import java.util.List;

@Repository
@Table(name = "archives_catalogue")
@Qualifier("catalogueRepository")
public interface CatalogueRepository extends JpaRepository<Catalogue, Integer> {
    List<Catalogue> findByArchivesIdAndCatalogueType(int archivesId,CatalogueType catalogueType);
}
