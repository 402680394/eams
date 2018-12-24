package com.ztdx.eams.domain.system.repository;

import com.ztdx.eams.domain.system.model.UserDesItemConf;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;
import java.util.Collection;
import java.util.List;

@Repository
@Table(name = "sys_user_des_item_conf")
@Qualifier("userDesItemConfRepository")
public interface UserDesItemConfRepository extends JpaRepository<UserDesItemConf, Integer> {

    void deleteAllByDescriptionItemIdIn(Collection<Integer> descriptionItemId);

    //设置排序号
    @Modifying
    @Query("update UserDesItemConf u set u.orderNumber=:orderNumber where u.id=:id")
    void updateOrderNumberById(@Param(value = "id") int id, @Param(value = "orderNumber") int orderNumber);

    List<UserDesItemConf> findAllByIdIn(Collection<Integer> id);

    List<UserDesItemConf> findAllByUserIdAndCatalogueId(int userId,int catalogueId);

    @Query("select max (u.orderNumber) from UserDesItemConf u where u.catalogueId=:catalogueId and u.userId=:userId")
    Integer findMaxOrderNumber(@Param(value = "catalogueId") int catalogueId,@Param(value = "userId") int userId);

}
