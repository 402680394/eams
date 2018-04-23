package com.ztdx.eams.domain.system.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "sys_resource")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    //资源名称
    @Size(max = 100)
    @Column(name = "resource_name")
    private String resourceName;

    //资源URL
    @Size(max = 255)
    @Column(name = "resource_url")
    private String resourceUrl;

    //上级资源id
    @Column(name = "parent_id")
    private Long parentId;

    //资源类型
    @Pattern(regexp = "^(dir|func)&",message = "格式不正确")
    @Column(name = "resource_type")
    private String resourceType;

    //顺序
    @Column(name = "order_number")
    private int orderNumber;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
