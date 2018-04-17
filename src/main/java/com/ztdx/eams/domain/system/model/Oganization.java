package com.ztdx.eams.domain.system.model;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;

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
//    @Max(value = 20)
    @Size(max = 20)
    @Column(name = "org_code")
    private String code;

    //机构名称
//    @Length(value = 50)
    @Size(max = 50)
    @Column(name = "org_name")
    private String name;

    //上级机构
    @Column(name = "parent_id")
    private int parentId;

    //同级排序优先级
    @Min(value = 1)
    @Column(name = "order_number")
    private int orderNumber;

    //机构类型
    @Min(value = 1)
    @Max(value = 3)
    @Column(name = "org_type")
    private int type;

    //所属全宗
    @Size(max = 20)
    @Column(name = "fonds_id")
    private String fondsId;

    //机构描述
    @Size(max = 50)
    @Column(name = "org_describe")
    private String describe;

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
