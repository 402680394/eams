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
@Table(name = "archives_catalogue")
public class Catalogue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //目录类型(1 一文一件 2 案卷 3 项目）
    @Min(value = 0)
    @Max(value = 4)
    @Column(name = "catalogue_type")
    @Convert(converter = CatalogueType.EnumConverter.class)
    private CatalogueType catalogueType;

    //档案库标识
    @Column(name = "archives_id")
    private int archivesId;

    //物理表的名称
    @Size(max = 100)
    @Column(name = "table_name")
    private String tableName;

    //元数据规范标识
    @Column(name = "metadata_standards_id")
    private Integer metadataStandardsId;

    //序号长度
    @Column(name = "serial_length")
    private Integer serialLength;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
