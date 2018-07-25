package com.ztdx.eams.domain.system.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;

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
    //是否删除
    @Column(name = "gmt_deleted")
    private int gmtDeleted;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
