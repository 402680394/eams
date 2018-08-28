package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.Catalogue;
import com.ztdx.eams.domain.archives.model.CatalogueType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;
import java.util.List;
import java.util.Optional;

@Repository
@Table(name = "archives_catalogue")
@Qualifier("catalogueRepository")
public interface CatalogueRepository extends JpaRepository<Catalogue, Integer> {
    Optional<Catalogue> findByArchivesIdAndCatalogueType(int archiveId, CatalogueType catalogueType);

    Optional<Catalogue> findByArchivesIdAndCatalogueTypeNot(int archiveId, CatalogueType catalogueType);

    List<Catalogue> findAllByArchivesIdInAndCatalogueType(List<Integer> archivesId, CatalogueType catalogueType);

    List<Catalogue> findAllByArchivesIdIn(List<Integer> archivesId);

    //查询全宗下归档库除去案卷所有目录
    @Query("select c.id from Fonds f,ArchivesGroup ag,Archives a,Catalogue c where f.id=:fondsId and ag.fondsId=f.id and a.archivesGroupId=ag.id and a.type=2 and c.archivesId=a.id and c.catalogueType<>2")
    List<Integer> findCatalogueIdByfondsId(@Param(value = "fondsId") int fondsId);
}
