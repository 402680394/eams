package com.ztdx.eams.domain.system.repository;

import com.ztdx.eams.domain.system.model.Oganization;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Created by li on 2018/4/11.
 */
@Repository
@Table(name = "sys_organization")
@Qualifier("organizationRepository")
public interface OganizationRepository extends JpaRepository<Oganization, Integer> {

    //查询机构编码是否存在
    boolean existsByCode(String code);

    //通过ID查询机构
    Oganization findById(int id);

    //通过ID查询机构是否存在
    boolean existsById(int id);

    // 通过父机构ID查询子机构
    List<Oganization> findAllByParentId(int id);

    //查询同级机构优先级最大值
    @Query("select max (o.orderNumber) from Oganization o where o.parentId=:parentId and o.type=:type")
    int findMaxOrderNumber(@Param(value = "parentId") int parentId, @Param(value = "type") int type);

    //通过ID修改信息
    @Modifying
    @Query("update Oganization o set o.parentId=:#{#oganization.parentId},o.code=:#{#oganization.code},o.name=:#{#oganization.name},o.describe=:#{#oganization.describe},o.remark=:#{#oganization.remark},o.type=:#{#oganization.type} where o.id=:#{#oganization.id}")
    void updateById(@Param("oganization")Oganization oganization);

    //设置机构优先级
    @Modifying
    @Query("update Oganization o set o.orderNumber=:orderNumber where o.id=:id")
    void updateOrderNumberById(@Param(value = "id") int id, @Param(value = "orderNumber") int orderNumber);

    boolean existsByCodeAndId(String code, int id);
}
