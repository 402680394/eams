package com.ztdx.eams.domain.store.model;

import com.ztdx.eams.domain.archives.model.archivalCodeRuler.RulerType;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "store_monitoring_point")
public class MonitoringPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    //所属库房id
    @Column(name = "storage_id")
    private Integer storageId;

    //监测点编号
    @Size(max = 10)
    @Column(name = "number")
    private String number;

    //监测点类型（1 温度记录仪 2 湿度记录仪 3 温湿度记录仪）
    @Column(name = "type")
    @Convert(converter = MonitoringPointType.EnumConverter.class)
    private MonitoringPointType type;

    //监测点状态
    @Min(value = 0)
    @Max(value = 1)
    @Column(name = "status")
    private Integer status;

    //备注
    @Size(max = 100)
    @Column(name = "remark")
    private String remark;

    //所属全宗id
    @Column(name = "fonds_id")
    private Integer fondsId;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
