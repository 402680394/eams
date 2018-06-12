package com.ztdx.eams.domain.store.repository;

import com.ztdx.eams.domain.archives.model.MetadataStandards;
import com.ztdx.eams.domain.store.model.MonitoringPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

/**
 * Created by li on 2018/4/22.
 */
@Repository
@Table(name = "store_monitoring_point")
@Qualifier("monitoringPointRepository")
public interface MonitoringPointRepository extends JpaRepository<MonitoringPoint, Integer> {

    boolean existsByNumber(String number);


    //通过ID修改信息
    @Modifying
    @Query("update MonitoringPoint m set m.number=:#{#monitoringPoint.number},m.storageId=:#{#monitoringPoint.storageId},m.type=:#{#monitoringPoint.type},m.status=:#{#monitoringPoint.status},m.remark=:#{#monitoringPoint.remark},m.fondsId=:#{#monitoringPoint.fondsId} where m.id=:#{#monitoringPoint.id}")
    void updateById(@Param(value = "monitoringPoint") MonitoringPoint monitoringPoint);




}
