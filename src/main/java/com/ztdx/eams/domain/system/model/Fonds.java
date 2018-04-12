package com.ztdx.eams.domain.system.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;
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

    //备注
    @Size(max = 100)
    @Column(name = "fonds_remark")
    private String remark;
}
