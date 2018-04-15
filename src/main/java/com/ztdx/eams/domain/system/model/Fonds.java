package com.ztdx.eams.domain.system.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * Created by li on 2018/4/11.
 */
@Data
@Entity
@Table(name = "sys_fonds")
public class Fonds {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //全宗名称
    @Size(max = 20)
    @Column(name = "fonds_name")
    private String name;

    //全宗号
    @Size(max = 20)
    @Column(name = "fonds_code")
    private String code;

    //全宗类型
    @Max(value = 20)
    @Column(name = "fonds_type")
    private int type;

    //上级全宗ID
    @Column(name = "parent_id")
    private int parentId;

    //同级排序优先级
    @Min(value = 1)
    @Column(name = "order_number")
    private int orderNumber;

    //备注
    @Size(max = 100)
    @Column(name = "remark")
    private String remark;
}
