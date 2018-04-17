package com.ztdx.eams.domain.system.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by li on 2018/3/22.
 */
@Data
@Entity
@Table(name = "sys_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //姓名
    @Size(max = 10)
    @Column(name = "real_name")
    private String name;

    //工号
    @Size(max = 20)
    @Column(name = "workers")
    private String workers;

    //用户名
    @Size(max = 20)
    @Column(name = "username")
    private String username;

    //密码
    @Size(max = 30)
    @Column(name = "pwd")
    private String password;

    //所属机构
    @Min(value = 1)
    @Column(name = "organization_id")
    private int organizationId;

    //电话
    @Size(max = 20)
    @Column(name = "phone")
    private String phone;

    //邮箱
    @Size(max = 50)
    @Column(name = "email")
    private String email;

    //职位
    @Size(max = 20)
    @Column(name = "job")
    private String job;

    //备注
    @Size(max = 100)
    @Column(name = "remark")
    private String remark;

    //状态
    @Min(value = 0)
    @Max(value = 10)
    @Column(name = "flag")
    private int flag;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
