package com.ztdx.eams.domain.system.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ztdx.eams.domain.system.model.Resource;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;
import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    //查询是url否存在
    boolean existsByResourceUrl(String resourceUrl);

    //查询是ID否存在
    boolean existsById(long id);

    //查询同级全宗优先级最大值
    @Query("select max (f.orderNumber) from Resource f where f.parentId=:parentId and f.resourceType=:type")
    Integer findMaxOrderNumber(@Param(value = "parentId")long parentId, @Param(value = "type") String type);

    // 通过父ID查询子全宗
    List<Resource> findAllByParentId(Long id);

    //通过ID查询
    Resource findById(long id);

    //通过ID查询
    Resource findByResourceUrl(String resourceUrl);

    //通过ID修改
    @Modifying
    @Query("update Resource f set f.parentId=:#{#resource.parentId},f.resourceName=:#{#resource.resourceName},f.resourceUrl=:#{#resource.resourceUrl},f.resourceType=:#{#resource.resourceType} where f.id=:#{#resource.id}")
    void updateById(@Param(value = "resource")Resource resource);

    //设置优先级
    @Modifying
    @Query("update Resource f set f.orderNumber=:orderNumber where f.id=:id")
    void updateOrderNumberById(@Param(value = "id") long id, @Param(value = "orderNumber") int orderNumber);
}
