package com.ztdx.eams.domain.archives.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by li on 2018/5/3.
 */
@Data
@Entity
@Table(name = "archives_group")
public class ArchivesGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //分组名称
    @Size(max = 50)
    @Column(name = "name")
    private String name;

    //所属全宗
    @Column(name = "fonds_id")
    private int fondsId;

    //上级分组
    @Column(name = "parent_id")
    private int parentId;

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
