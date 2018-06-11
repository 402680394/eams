package com.ztdx.eams.domain.store.repository;

import com.ztdx.eams.domain.store.model.MonitoringRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

/**
 * Created by Sarah 2018/6/8
 */
@Repository
@Table(name = "store_monitoring_record")
@Qualifier("monitoringRecordRepository")
public interface MonitoringRecordRepository extends JpaRepository<MonitoringRecord, Integer> {


    //通过ID修改信息
    @Modifying
    @Query("update MonitoringRecord m set m.monitoringPointId=:#{#monitoringRecord.monitoringPointId},m.monitoringTime=:#{#monitoringRecord.monitoringTime},m.temperatureValue=:#{#monitoringRecord.temperatureValue},m.humidityValue=:#{#monitoringRecord.humidityValue},m.takeSteps=:#{#monitoringRecord.takeSteps} where m.id=:#{#monitoringRecord.id}")
    void updateById(@Param(value = "monitoringRecord") MonitoringRecord monitoringRecord);




}
