package com.ztdx.eams.domain.archives.model;

import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by li on 2018/5/2.
 */
@Data
@Entity
@Table(name = "archives")
public class Archives {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //档案库结构(1 一文一件；2 案卷；3 项目)
    @Min(value = 1)
    @Max(value = 3)
    @Column(name = "structure")
    @Convert(converter = Structure.EnumConverter.class)
    private Structure structure;

    //档案库名称
    @Size(max = 50)
    @Column(name = "name")
    private String name;

    //所属库分组ID
    @Column(name = "archives_group_id")
    private int archivesGroupId;

    //档案库内容类型
    @Column(name = "content_type")
    private int contentType;

    //档案库类型(1 登记库； 2 归档库)
    @Column(name = "type")
    private int type;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
