package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.Archives;
import com.ztdx.eams.domain.archives.model.ArchivesGroup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;
import java.util.List;

@Repository
@Table(name = "archives")
@Qualifier("archivesRepository")
public interface ArchivesRepository extends JpaRepository<Archives, Integer> {
    List<Archives> findByTypeAndGmtDeleted(int type, int gmtDeleted);

    @Query("select a.name from Archives a,Catalogue c where c.id=:catalogueId and a.id=c.archivesId")
    String findArchivesNameByCatalogue_CatalogueId(@Param(value = "catalogueId") int catalogueId);

    //设置状态删除
    @Modifying
    @Query("update Archives a set a.gmtDeleted=:gmtDeleted where a.id=:id")
    void setDeleteById(@Param(value = "id") int id, @Param(value = "gmtDeleted") int gmtDeleted);

    //修改
    @Modifying
    @Query("update Archives a set a.name=:#{#archives.name},a.archivesGroupId=:#{#archives.archivesGroupId},a.remark=:#{#archives.remark} where a.id=:#{#archives.id}")
    void update(@Param(value = "archives") Archives archives);

    @Modifying
    @Query("update Archives a set a.gmtDeleted=:gmtDeleted where a.archivesGroupId=:archivesGroupId")
    void setDeleteByArchivesGroupId(@Param(value = "archivesGroupId") int archivesGroupId, @Param(value = "gmtDeleted") int gmtDeleted);
}
