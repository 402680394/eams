package com.ztdx.eams.domain.archives.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by li on 2018/5/3.
 */
@Data
@Entity
@Table(name = "archives_description_item")
public class DescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //档案目录标识
    @Column(name = "catalogue_id")
    private int catalogueId;

    //元数据标识
    @Column(name = "metadata_id")
    private int metadataId;

    //元数据名称
    @Size(max = 30)
    @Column(name = "metadata_name")
    private String metadataName;

    //显示名称
    @Size(max = 50)
    @Column(name = "display_name")
    private String displayName;

    //属性类型标识
    @Convert(converter = PropertyType.EnumConverter.class)
    @Column(name = "property_type")
    private PropertyType propertyType;

    //默认值
    @Convert(converter = DefaultValue.EnumConverter.class)
    @Column(name = "default_value")
    private DefaultValue defaultValue;

    //数据类型(1 数值 2 字符串 3 日期 4 浮点)
    @Min(value = 1)
    @Max(value = 4)
    @Column(name = "data_type")
    private int dataType;

    //是否建立索引
    @Min(value = 0)
    @Max(value = 1)
    @Column(name = "is_index")
    private int isIndex;

    //是否只读
    @Min(value = 0)
    @Max(value = 1)
    @Column(name = "is_read")
    private int isRead;

    //是否可空
    @Min(value = 0)
    @Max(value = 1)
    @Column(name = "is_null")
    private int isNull;

    //是否使用字典
    @Min(value = 0)
    @Max(value = 1)
    @Column(name = "is_dictionary")
    private int isDictionary;

    //字典类型(1 目录字典 2 档案分类 3 组织机构）
    @Min(value = 1)
    @Max(value = 3)
    @Column(name = "dictionary_type")
    private int dictionaryType;

    //字典节点标识
    @Column(name = "dictionary_node_id")
    private int dictionaryNodeId;

    //字典获取值的方式(1 编码 2 名称 3 编码与名称 4 名称与编码）
    @Min(value = 1)
    @Max(value = 4)
    @Column(name = "dictionary_value_type")
    private int dictionaryValueType;

    //字典中，是否根节点可选(当字典类型为“档案分类”、“组织结构”时有效)
    @Min(value = 0)
    @Max(value = 1)
    @Column(name = "dictionary_root_select")
    private int dictionaryRootSelect;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
