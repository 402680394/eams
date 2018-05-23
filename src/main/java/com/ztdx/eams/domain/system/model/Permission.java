package com.ztdx.eams.domain.system.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "sys_permission")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    //角色id
    @Column(name = "role_id")
    private long roleId;

    //资源id
    @Column(name = "resource_id")
    private long resourceId;

    //全宗id
    @Column(name = "fonds_id")
    private Integer fondsId;

    //档案库id
    @Column(name = "archive_id")
    private Integer archiveId;

    //资源URL
    @Size(max = 255)
    @Column(name = "resource_url")
    private String resourceUrl;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
