package com.ztdx.eams.domain.system.repository;

import com.ztdx.eams.domain.system.model.Fonds;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Created by li on 2018/4/11.
 */
@Repository
@Table(name = "sys_fonds")
@Qualifier("fondsRepository")
public interface FondsRepository extends JpaRepository<Fonds, Integer> {
    //查询code是否存在
    boolean existsByCode(String code);
    //查询ID是否存在
    boolean existsById(int id);

    //查询同级全宗优先级最大值
    @Query("select max (f.orderNumber) from Fonds f where f.parentId=:parentId and f.type=:type")
    Integer findMaxOrderNumber(@Param(value = "parentId")int parentId, @Param(value = "type") int type);

    // 通过父ID查询是否存在子全宗
    boolean existsByParentId(int id);

    //通过ID查询
    Fonds findById(int id);

    //通过ID修改
    @Modifying
    @Query("update Fonds f set f.parentId=:#{#fonds.parentId},f.code=:#{#fonds.code},f.name=:#{#fonds.name},f.remark=:#{#fonds.remark},f.type=:#{#fonds.type} where f.id=:#{#fonds.id}")
    void updateById(@Param(value = "fonds")Fonds fonds);

    //设置优先级
    @Modifying
    @Query("update Fonds f set f.orderNumber=:orderNumber where f.id=:id")
    void updateOrderNumberById(@Param(value = "id") int id, @Param(value = "orderNumber") int orderNumber);
}
