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
@Table(name = "business_metadata_standards")
public class MetadataStandards {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //名称
    @Size(max = 30)
    @Column(name = "metadata_standards_name")
    private String name;

    //编号
    @Size(max = 30)
    @Column(name = "metadata_standards_code")
    private String code;

    //字符集
    @Column(name = "character_set")
    private int characterSet;

    //发布机构
    @Size(max = 20)
    @Column(name = "release_organization")
    private String releaseOrganization;

    //描述文件
    @Size(max = 30)
    @Column(name = "description_file")
    private String descriptionFile;

    //版本
    @Size(max = 20)
    @Column(name = "edition")
    private String edition;

    //排序号
    @Column(name = "order_number")
    private int orderNumber;

    //是否启用
    @Column(name = "flag")
    private int flag;

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
