package com.ztdx.eams.domain.archives.repository;

import com.ztdx.eams.domain.archives.model.Classification;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

/**
 * Created by li on 2018/4/18.
 */
@Repository
@Table(name = "business_classification")
@Qualifier("classificationRepository")
public interface ClassificationRepository extends JpaRepository<Classification, Integer> {

    //查询同级档案分类优先级最大值
    @Query("select max (c.orderNumber) from Classification c where c.parentId=:parentId")
    Integer findMaxOrderNumber(@Param(value = "parentId")int parentId);

    // 通过父ID查询子档案分类是否存在
    boolean existsByParentId(int id);

    //通过ID修改
    @Modifying
    @Query("update Classification c set c.parentId=:#{#classification.parentId},c.code=:#{#classification.code},c.name=:#{#classification.name},c.remark=:#{#classification.remark},c.retentionPeriod=:#{#classification.retentionPeriod} where c.id=:#{#classification.id}")
    void updateById(@Param(value = "classification")Classification classification);

    //设置优先级
    @Modifying
    @Query("update Classification c set c.orderNumber=:orderNumber where c.id=:id")
    void updateOrderNumberById(@Param(value = "id") int id, @Param(value = "orderNumber") int orderNumber);
}
