package com.ztdx.eams.domain.store.repository;

import com.ztdx.eams.domain.store.model.BoxCodeRule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;
import java.util.Optional;

/**
 * Created by li on 2018/7/13.
 */
@Repository
@Table(name = "store_box_code_rule")
@Qualifier("boxRepository")
public interface BoxCodeRuleRepository extends JpaRepository<BoxCodeRule, Integer> {

    Optional<BoxCodeRule> findByArchivesIdAndType(int archivesId, int type);

    @Modifying
    @Query("update BoxCodeRule b set b.orderNumber=:orderNumber where b.id=:id")
    void updateOrderNumberById(@Param(value = "id") int id, @Param(value = "orderNumber") int orderNumber);

    @Query("select max (b.orderNumber) from BoxCodeRule b where b.archivesId=:archivesId")
    Integer findMaxOrderNumber(@Param(value = "archivesId") int archivesId);

    @Modifying
    @Query("update BoxCodeRule b set b.type=:#{#boxCodeRule.type},b.value=:#{#boxCodeRule.value},b.descriptionItemId=:#{#boxCodeRule.descriptionItemId},b.interceptionLength=:#{#boxCodeRule.interceptionLength},b.flowNumberLength=:#{#boxCodeRule.flowNumberLength},b.remark=:#{#boxCodeRule.remark} where b.id=:#{#boxCodeRule.id}")
    void updateById(@Param("boxCodeRule") BoxCodeRule boxCodeRule);

    boolean existsByDescriptionItemId(int descriptionItemId);
}
