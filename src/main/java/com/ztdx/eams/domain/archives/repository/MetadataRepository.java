package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.Metadata;
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
@Table(name = "business_metadata")
@Qualifier("metadataRepository")
public interface MetadataRepository extends JpaRepository<Metadata, Integer> {

    void deleteByMetadataStandardsId(int metadataStandardsId);

    Metadata findById(int id);

    boolean existsByName(String name);

    //查询排序号最大值
    @Query("select max (m.orderNumber) from Metadata m")
    Integer findMaxOrderNumber();

    boolean existsByNameAndId(String name, int id);

    //通过ID修改信息
    @Modifying
    @Query("update Metadata m set m.displayName=:#{#metadata.displayName},m.name=:#{#metadata.name},m.fieldProperties=:#{#metadata.fieldProperties}" +
            ",m.dataType=:#{#metadata.dataType},m.fieldWidth=:#{#metadata.fieldWidth},m.fieldPrecision=:#{#metadata.fieldPrecision}" +
            ",m.defaultValue=:#{#metadata.defaultValue},m.definition=:#{#metadata.definition},m.objective=:#{#metadata.objective},m.constraint=:#{#metadata.constraint}" +
            ",m.elementType=:#{#metadata.elementType},m.codingModification=:#{#metadata.codingModification},m.relatedElements=:#{#metadata.relatedElements}" +
            ",m.range=:#{#metadata.range},m.informationSources=:#{#metadata.informationSources},m.remark=:#{#metadata.remark} where m.id=:#{#metadata.id}")
    void updateById(@Param(value = "metadata") Metadata metadata);

    //设置排序号
    @Modifying
    @Query("update Metadata m set m.orderNumber=:orderNumber where m.id=:id")
    void updateOrderNumberById(@Param(value = "id") int id, @Param(value = "orderNumber") int orderNumber);
}