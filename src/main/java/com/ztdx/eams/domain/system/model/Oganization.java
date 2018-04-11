package com.ztdx.eams.domain.system.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;

/**
 * Created by li on 2018/4/11.
 */
@Data
@Entity
@Table(name = "sys_organization")
public class Oganization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //机构编码
    @Max(value = 20)
    @Column(name = "org_code")
    private String code;

    //机构名称
    @Max(value = 50)
    @Column(name = "org_name")
    private String name;

    //上级机构
    @Max(value = 11)
    @Column(name = "org_parent_id")
    private int parentId;

    //同级机构优先级
    @Max(value = 3)
    @Column(name = "org_order_number")
    private int orderNumber;

    //机构类型
    @Max(value = 2)
    @Column(name = "org_type")
    private int type;

    //所属全宗
    @Max(value = 20)
    @Column(name = "fonds_id")
    private String fondsId;
    //机构描述
    @Max(value = 50)
    @Column(name = "org_depict")
    private String depict;

    //备注
    @Max(value = 100)
    @Column(name = "org_remark")
    private String remark;
}
