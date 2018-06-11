package com.ztdx.eams.domain.store.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "store_monitoring_record")
public class MonitoringRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    //所属监测点id
    @Column(name = "monitoring_point_id")
    private Integer monitoringPointId;

    //监测时间
    @Column(name = "monitoring_time")
    private Date monitoringTime;

    //温度值
    @Min(value = -100)
    @Max(value = 100)
    @Column(name = "temperature_value")
    private Integer temperatureValue;

    //湿度值
    @Min(value = -100)
    @Max(value = 100)
    @Column(name = "humidity_value")
    private Integer humidityValue;

    //采取措施
    @Size(max = 100)
    @Column(name = "take_steps")
    private String takeSteps;

    //所属库房id
    @Column(name = "storage_id")
    private Integer storageId;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
