package com.ztdx.eams.domain.system.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "sys_user_permission")
public class UserPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    //用户id
    @NotNull
    @Column(name = "user_id")
    private int userId;

    //过期时间
    @Column(name = "expiry_time")
    private Date expiryTime;

    //资源URL
    @Size(max = 255)
    @Column(name = "resource_url")
    @NotBlank
    private String resourceUrl;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;
}
