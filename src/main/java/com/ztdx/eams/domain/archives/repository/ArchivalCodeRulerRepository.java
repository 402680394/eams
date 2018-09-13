package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.archivalCodeRuler.ArchivalCodeRuler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivalCodeRulerRepository extends JpaRepository<ArchivalCodeRuler, Integer> {

    List<ArchivalCodeRuler> findByCatalogueIdOrderByOrderNumber(int catalogueId);

    String findByIsGroup(int isGroup);

    @Modifying
    @Query("update ArchivalCodeRuler a set a.orderNumber=:orderNumber where a.id=:id")
    void updateOrderNumberById(@Param(value = "id") int id, @Param(value = "orderNumber") int orderNumber);

    @Query("select max (a.orderNumber) from ArchivalCodeRuler a where a.catalogueId=:catalogueId")
    Integer findMaxOrderNumber(@Param(value = "catalogueId") int catalogueId);

    @Modifying
    @Query("update ArchivalCodeRuler a set a.type=:#{#archivalCodeRuler.type},a.value=:#{#archivalCodeRuler.value},a.descriptionItemId=:#{#archivalCodeRuler.descriptionItemId},a.interceptionLength=:#{#archivalCodeRuler.interceptionLength},a.remark=:#{#archivalCodeRuler.remark} where a.id=:#{#archivalCodeRuler.id}")
    void updateById(@Param("archivalCodeRuler") ArchivalCodeRuler archivalCodeRuler);
}
