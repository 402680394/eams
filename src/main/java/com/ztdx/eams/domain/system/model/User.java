package com.ztdx.eams.domain.system.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;

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
    @Max(value = 10)
    @Column(name = "user_name")
    private String name;

    //工号
    @Max(value = 20)
    @Column(name = "user_workers")
    private String workers;

    //用户名
    @Max(value = 20)
    @Column(name = "user_username")
    private String username;

    //密码
    @Max(value = 30)
    @Column(name = "user_password")
    private String password;

    //电话
    @Max(value = 20)
    @Column(name = "user_phone")
    private String phone;

    //邮箱
    @Max(value = 50)
    @Column(name = "user_email")
    private String email;

    //职位
    @Max(value = 20)
    @Column(name = "user_position")
    private String position;

    //备注
    @Max(value = 100)
    @Column(name = "user_remark")
    private String remark;

    //状态
    @Max(value = 2)
    @Column(name = "user_status")
    private int status;
}
