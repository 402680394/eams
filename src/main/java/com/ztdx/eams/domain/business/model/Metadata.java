package com.ztdx.eams.domain.business.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by li on 2018/4/22.
 */
@Data
@Entity
@Table(name = "business_metadata")
public class Metadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //字段名称
    @Size(max = 30)
    @Column(name = "metadata_name")
    private String name;

    //字段显示名
    @Size(max = 30)
    @Column(name = "display_name")
    private String displayName;

    //字段属性
    @Column(name = "field_properties")
    private int fieldProperties;

    //数据类型
    @Column(name = "data_type")
    private int dataType;

    //字段宽度
    @Size(max = 20)
    @Column(name = "field_width")
    private String fieldWidth;

    //字段精度
    @Size(max = 20)
    @Column(name = "field_precision")
    private String fieldPrecision;

    //父字段ID
    @Column(name = "parent_id")
    private int parentId;

    //元数据规范ID
    @Column(name = "metadata_standards_id")
    private int metadataStandardsId;

    //默认值
    @Column(name = "default_value")
    private int defaultValue;

    //定义
    @Size(max = 50)
    @Column(name = "metadata_definition")
    private String definition;

    //目的
    @Size(max = 50)
    @Column(name = "objective")
    private String objective;

    //约束性
    @Column(name = "metadata_constraint")
    private int constraint;

    //元素类型
    @Column(name = "element_type")
    private int elementType;

    //编码修饰体系
    @Size(max = 30)
    @Column(name = "coding_modification")
    private String codingModification;

    //相关元素
    @Size(max = 30)
    @Column(name = "related_elements")
    private String relatedElements;

    //值域
    @Size(max = 30)
    @Column(name = "metadata_range")
    private String range;

    //信息来源
    @Size(max = 30)
    @Column(name = "information_sources")
    private String informationSources;

    //排序号
    @Column(name = "order_number")
    private int orderNumber;

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
