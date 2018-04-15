package com.ztdx.eams.domain.system.repository;

import com.ztdx.eams.domain.system.model.Fonds;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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

    int countByCode(@Size(max = 20) String code);

    //查询同级全宗优先级最大值
    @Query("select max (f.orderNumber) from Fonds f where f.parentId=:parentId and f.type=:type")
    int findMaxOrderNumber(int parentId, @Max(value = 20) int type);

    // 通过父ID查询子全宗
    List<Fonds> findAllByParentId(int id);

    //通过ID查询
    Fonds findById(int id);

    @Modifying
    @Query("update Fonds f set f.parentId=:#{#fonds.parentId},f.code=:#{#fonds.code},f.name=:#{#fonds.name},f.remark=:#{#fonds.remark},f.type=:#{#fonds.type} where f.id=:#{#fonds.id}")
    void updateById(Fonds fonds);

    //设置优先级
    @Modifying
    @Query("update Fonds f set f.orderNumber=:orderNumber where f.id=:id")
    void updateOrderNumberById(int upId, @Min(value = 1) int orderNumber);
}
