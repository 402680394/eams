package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.Archives;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;
import java.util.List;

@Repository
@Table(name = "archives")
@Qualifier("archivesRepository")
public interface ArchivesRepository extends JpaRepository<Archives, Integer> {
    List<Archives> findByType(int type);

    @Query("select a.name from Archives a,Catalogue c where c.id=:catalogueId and a.id=c.archivesId")
    String findArchivesNameByCatalogue_CatalogueId(@Param(value = "catalogueId") int catalogueId);
}
