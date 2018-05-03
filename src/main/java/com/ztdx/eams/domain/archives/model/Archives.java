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

    //档案库类型(1 一文一件；2 案卷；3 项目)
    @Min(value = 1)
    @Max(value = 3)
    @Column(name = "archives_type")
    @Convert(converter = ArchivesType.EnumConverter.class)
    private ArchivesType archivesType;

    //档案库名称
    @Size(max = 50)
    @Column(name = "name")
    private String name;

    //全宗标识
    @Column(name = "fonds_id")
    private int fondsId;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
