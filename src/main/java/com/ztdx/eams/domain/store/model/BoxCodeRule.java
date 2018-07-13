package com.ztdx.eams.domain.store.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by li on 2018/7/11.
 */
@Data
@Entity
@Table(name = "store_box_code_rule")
public class BoxCodeRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    //盒规则类型  1-著录项值 2-著录项值对应编码 3-档案库所属全宗号 4-固定值 5-流水号
    @Column(name = "type")
    private int type;
    //名称
    @Column(name = "name")
    private String name;
    //值
    @Column(name = "value")
    private String value;
    //著录项id
    @Column(name = "description_item_id")
    private int descriptionItemId;
    //截取长度
    @Column(name = "interception_length")
    private byte interceptionLength;
    //流水号长度
    @Column(name = "flow_number_length")
    private byte flowNumberLength;
    //备注
    @Column(name = "remark")
    private String remark;
    //排序号
    @Column(name = "order_number")
    private int orderNumber;
    //档案库id
    @Column(name = "archives_id")
    private int archivesId;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
