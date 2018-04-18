package com.ztdx.eams.domain.business.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by li on 2018/4/18.
 */
@Data
@Entity
@Table(name = "business_dictionary")
public class Dictionary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //词典名称
    @Size(max = 30)
    @Column(name = "dictionary_name")
    private String name;

    //词典编码
    @Size(max = 30)
    @Column(name = "dictionary_code")
    private String code;

    //词典分类id
    @Column(name = "dictionary_classification_id")
    private int classificationId;

    //业务级别
    @Column(name = "business_level")
    private int businessLevel;

    //备注
    @Size(max = 100)
    @Column(name = "remark")
    private String remark;

    //业务扩展
    @Size(max = 30)
    @Column(name = "business_expansion")
    private String businessExpansion;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
