package com.ztdx.eams.domain.system.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "sys_role", schema = "eams", catalog = "")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    //角色名称
    @Column(name = "role_name")
    private String roleName;

    //全宗id
    @Column(name = "fonds_id")
    private Integer fondsId;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
