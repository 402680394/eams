package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.ArchivesGroup;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.archives.model.PropertyType;
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
 * Created by li on 2018/5/15.
 */
@Repository
@Table(name = "archives_description_item")
@Qualifier("descriptionItemRepository")
public interface DescriptionItemRepository extends JpaRepository<DescriptionItem, Integer> {
    List<DescriptionItem> findByCatalogueId(int catalogueId);

    /**
     * 获取目录下的对应的属性类型
     */
    DescriptionItem findByCatalogueIdAndPropertyType(int catalogueId, PropertyType propertyType);

    List<DescriptionItem> findByCatalogueIdIn(Collection<Integer> catalogueId);

    void deleteByIdIn(Collection<Integer> id);


    @Modifying
    @Query("update DescriptionItem d set d.displayName=:#{#descriptionItem.displayName},d.dictionaryValueType=:#{#descriptionItem.dictionaryValueType},d.dictionaryRootSelect=:#{#descriptionItem.dictionaryRootSelect},d.dictionaryNodeId=:#{#descriptionItem.dictionaryNodeId},d.dictionaryType=:#{#descriptionItem.dictionaryType},d.isDictionary=:#{#descriptionItem.isDictionary},d.isNull=:#{#descriptionItem.isNull},d.isRead=:#{#descriptionItem.isRead},d.isIncrement=:#{#descriptionItem.isIncrement},d.fieldPrecision=:#{#descriptionItem.fieldPrecision},d.fieldWidth=:#{#descriptionItem.fieldWidth},d.propertyType=:#{#descriptionItem.propertyType},d.defaultValue=:#{#descriptionItem.defaultValue},d.dataType=:#{#descriptionItem.dataType} where d.id=:#{#descriptionItem.id}")
    void updateById(@Param(value = "descriptionItem") DescriptionItem descriptionItem);
}
