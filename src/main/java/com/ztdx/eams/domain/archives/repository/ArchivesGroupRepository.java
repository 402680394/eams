package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.ArchivesGroup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

/**
 * Created by li on 2018/5/3.
 */
@Repository
@Table(name = "archives_group")
@Qualifier("archivesGroupRepository")
public interface ArchivesGroupRepository extends JpaRepository<ArchivesGroup, Integer> {

    @Query("select ag.fondsId from ArchivesGroup ag,Archives a,Catalogue c where c.id=:catalogueId and a.id=c.archivesId and ag.id=a.archivesGroupId")
    Integer findFondsIdByCatalogue_CatalogueId(@Param(value = "catalogueId") int catalogueId);
}
