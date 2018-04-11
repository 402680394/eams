package com.ztdx.eams.domain.system.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;

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

    //机构编码
    @Max(value = 20)
    @Column(name = "fonds_name")
    private String name;

    //机构编码
    @Max(value = 20)
    @Column(name = "fonds_code")
    private String code;

    //机构编码
    @Max(value = 20)
    @Column(name = "fonds_type")
    private int type;

    //备注
    @Max(value = 100)
    @Column(name = "fonds_remark")
    private String remark;
}
