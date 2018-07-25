package com.ztdx.eams.domain.store.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "store_storage")
public class Storage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    //库房名称
    @Size(max = 20)
    @Column(name = "name")
    private String name;

    //库房编号
    @Size(max = 10)
    @Column(name = "number")
    private String number;

    //库房描述
    @Size(max = 100)
    @Column(name = "description")
    private String description;

    //所属全宗id
    @Column(name = "fonds_id")
    private int fondsId;

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
