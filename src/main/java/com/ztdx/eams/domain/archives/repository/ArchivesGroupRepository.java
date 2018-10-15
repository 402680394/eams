package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.ArchivesGroup;
import com.ztdx.eams.domain.system.model.Fonds;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;
import java.util.List;

/**
 * Created by li on 2018/5/3.
 */
@Repository
@Table(name = "archives_group")
@Qualifier("archivesGroupRepository")
public interface ArchivesGroupRepository extends JpaRepository<ArchivesGroup, Integer> {

    List<ArchivesGroup> findByParentIdAndGmtDeleted(int parentId,int gmtDeleted);

    @Query("select ag.fondsId from ArchivesGroup ag,Archives a,Catalogue c where c.id=:catalogueId and a.id=c.archivesId and ag.id=a.archivesGroupId")
    Integer findFondsIdByCatalogue_CatalogueId(@Param(value = "catalogueId") int catalogueId);

    //设置状态为删除
    @Modifying
    @Query("update ArchivesGroup ag set ag.gmtDeleted=:gmtDeleted where ag.id=:id")
    void setDeleteById(@Param(value = "id") int id, @Param(value = "gmtDeleted") int gmtDeleted);

    //修改
    @Modifying
    @Query("update ArchivesGroup ag set ag.parentId=:#{#archivesGroup.parentId},ag.name=:#{#archivesGroup.name},ag.remark=:#{#archivesGroup.remark} where ag.id=:#{#archivesGroup.id}")
    void update(@Param(value = "archivesGroup") ArchivesGroup archivesGroup);

    @Modifying
    @Query("update ArchivesGroup ag set ag.gmtDeleted=:gmtDeleted where ag.fondsId=:fondsId")
    void setDeleteByFondsId(@Param(value = "fondsId") int fondsId, @Param(value = "gmtDeleted") int gmtDeleted);
}
