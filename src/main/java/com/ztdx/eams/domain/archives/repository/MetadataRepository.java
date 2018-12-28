package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.Metadata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;
import java.util.Collection;
import java.util.List;

/**
 * Created by li on 2018/4/22.
 */
@Repository
@Table(name = "business_metadata")
@Qualifier("metadataRepository")
public interface MetadataRepository extends JpaRepository<Metadata, Integer> {

    void deleteByMetadataStandardsId(int metadataStandardsId);

    List<Metadata> findByIdIn(Collection<Integer> ids);

    boolean existsByName(String name);

    boolean existsByDisplayName(String displayName);

    //最大排序号
    @Query("select max (m.orderNumber) from Metadata m")
    Integer findMaxOrderNumber();

    boolean existsByDisplayNameAndId(String displayName, int id);

    boolean existsByNameAndId(String name, int id);

    //修改
    @Modifying
    @Query("update Metadata m set m.displayName=:#{#metadata.displayName},m.name=:#{#metadata.name},m.fieldProperties=:#{#metadata.fieldProperties}" +
            ",m.dataType=:#{#metadata.dataType},m.fieldWidth=:#{#metadata.fieldWidth},m.fieldPrecision=:#{#metadata.fieldPrecision}" +
            ",m.defaultValue=:#{#metadata.defaultValue},m.definition=:#{#metadata.definition},m.objective=:#{#metadata.objective},m.constraint=:#{#metadata.constraint}" +
            ",m.elementType=:#{#metadata.elementType},m.codingModification=:#{#metadata.codingModification},m.relatedElements=:#{#metadata.relatedElements}" +
            ",m.range=:#{#metadata.range},m.informationSources=:#{#metadata.informationSources},m.remark=:#{#metadata.remark} where m.id=:#{#metadata.id}")
    void updateById(@Param(value = "metadata") Metadata metadata);

    //排序
    @Modifying
    @Query("update Metadata m set m.orderNumber=:orderNumber where m.id=:id")
    void updateOrderNumberById(@Param(value = "id") int id, @Param(value = "orderNumber") int orderNumber);
}
