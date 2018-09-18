package com.ztdx.eams.domain.system.repository;

import com.ztdx.eams.domain.system.model.Organization;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;
import java.util.List;

/**
 * Created by li on 2018/4/11.
 */
@Repository
@Table(name = "sys_organization")
@Qualifier("organizationRepository")
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {

    //查询机构编码是否存在
    boolean existsByCode(String code);

    //通过ID查询机构是否存在
    boolean existsById(int id);

    // 通过父机构ID查询子机构是否存在
    boolean existsByParentId(int id);

    //查询同级机构优先级最大值
    @Query("select max (o.orderNumber) from Organization o where o.parentId=:parentId and o.type=:type")
    Integer findMaxOrderNumber(@Param(value = "parentId") int parentId, @Param(value = "type") int type);

    //通过ID修改信息
    @Modifying
    @Query("update Organization o set o.parentId=:#{#organization.parentId},o.code=:#{#organization.code},o.name=:#{#organization.name},o.describe=:#{#organization.describe},o.remark=:#{#organization.remark},o.type=:#{#organization.type} where o.id=:#{#organization.id}")
    void updateById(@Param("organization") Organization organization);

    //设置机构优先级
    @Modifying
    @Query("update Organization o set o.orderNumber=:orderNumber where o.id=:id")
    void updateOrderNumberById(@Param(value = "id") int id, @Param(value = "orderNumber") int orderNumber);

    boolean existsByCodeAndId(String code, int id);

    //通过ID修改关联全宗
    @Modifying
    @Query("update Organization o set o.fondsId=:fondsId where o.id=:id")
    void updatefondsIdById(@Param(value = "id") int id, @Param(value = "fondsId") int fondsId);

    //通过全宗ID修改关联全宗为空
    @Modifying
    @Query("update Organization o set o.fondsId=null where o.fondsId=:fondsId")
    void updatefondsIdByfondsId(@Param(value = "fondsId") int fondsId);
}
