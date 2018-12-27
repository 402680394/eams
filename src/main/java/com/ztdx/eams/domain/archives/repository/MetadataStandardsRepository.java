package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.MetadataStandards;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

/**
 * Created by li on 2018/4/22.
 */
@Repository
@Table(name = "business_metadata_standards")
@Qualifier("metadataStandardsRepository")
public interface MetadataStandardsRepository extends JpaRepository<MetadataStandards, Integer> {
    boolean existsByCode(String code);

    //最大排序号
    @Query("select max (m.orderNumber) from MetadataStandards m")
    Integer findMaxOrderNumber();

    //修改信息
    @Modifying
    @Query("update MetadataStandards m set m.code=:#{#metadataStandards.code},m.name=:#{#metadataStandards.name},m.characterSet=:#{#metadataStandards.characterSet},m.releaseOrganization=:#{#metadataStandards.releaseOrganization},m.descriptionFile=:#{#metadataStandards.descriptionFile},m.edition=:#{#metadataStandards.edition},m.flag=:#{#metadataStandards.flag},m.remark=:#{#metadataStandards.remark} where m.id=:#{#metadataStandards.id}")
    void updateById(@Param(value = "metadataStandards")MetadataStandards metadataStandards);

    //排序
    @Modifying
    @Query("update MetadataStandards m set m.orderNumber=:orderNumber where m.id=:id")
    void updateOrderNumberById(@Param(value = "id") int id, @Param(value = "orderNumber") int orderNumber);
}
