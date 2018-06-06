package com.ztdx.eams.domain.archives.model.archivalCodeRuler;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "archives_archivalcode_ruler")
public class ArchivalCodeRuler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int  id;

    //选择类型(1 著入项值；2 著录项所对应的参照编码；3 档案库所属单位全宗号 4 固定值 5流水号)
    @Min(value = 1)
    @Max(value = 4)
    @Convert(converter = RulerType.EnumConverter.class)
    private RulerType type;

    //元数据
    @Size(max = 20)
    @Column(name = "metadata_name")
    private String metadataName;

    //值
    @Size(max = 10)
    @Column(name = "value")
    private String value;

    //截取长度
    @Column(name = "interception_length")
    private int interceptionLength;

    //备注
    @Size(max = 50)
    @Column(name = "remark")
    private String remark;

    //排序号
    @Min(value = 1)
    @Column(name = "order_number")
    private int orderNumber;

    //目录id
    @Column(name = "catalogue_id")
    private int catalogueId;

    //是否建立索引
    @Min(value = 0)
    @Max(value = 1)
    @Column(name = "is_group")
    private int isGroup;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;

}
