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

    /**
     * 资源url
     * global(全局) 100
     * fonds(全宗) 200
     * archive(档案库)_[档案库结构] 3xx
     * archive_use_[档案库结构] 4xx
     * 库房还未定义
     */
    @Column(name = "resource_category")
    private ResourceCategory resourceCategory;

    @Size(max = 255)
    @Column(name = "resource_url")
    private String resourceUrl;

    //上级资源id
    @Column(name = "parent_id")
    private Long parentId;

    //顺序
    @Column(name = "order_number")
    private Integer orderNumber;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
