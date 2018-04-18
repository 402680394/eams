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
@Table(name = "business_dictionary_classification")
public class DictionaryClassification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //词典分类名称
    @Size(max = 30)
    @Column(name = "classification_name")
    private String name;

    //词典分类编码
    @Size(max = 30)
    @Column(name = "classification_code")
    private String code;

    //所属全宗id
    @Column(name = "fonds_id")
    private int fondsId;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
