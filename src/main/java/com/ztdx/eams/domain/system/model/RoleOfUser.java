package com.ztdx.eams.domain.system.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "sys_role_user")
public class RoleOfUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    //角色id
    @Column(name = "role_id")
    private long roleId;

    //用户id
    @Column(name = "user_id")
    private int userId;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
