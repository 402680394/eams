package com.ztdx.eams.domain.archives.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by li on 2018/4/18.
 */
@Data
@Entity
@Table(name = "business_classification")
public class Classification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //档案分类名称
    @Size(max = 30)
    @Column(name = "classification_name")
    private String name;

    //档案分类编码
    @Size(max = 30)
    @Column(name = "classification_code")
    private String code;

    //保管期限
    @Size(max = 20)
    @Column(name = "retention_period")
    private String retentionPeriod;

    //上级分类id
    @Column(name = "parent_id")
    private int parentId;

    //同级排序优先级
    @Min(value = 1)
    @Column(name = "order_number")
    private int orderNumber;

    //所属全宗id
    @Column(name = "fonds_id")
    private int fondsId;

    //备注
    @Size(max = 100)
    @Column(name = "remark")
    private String remark;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
