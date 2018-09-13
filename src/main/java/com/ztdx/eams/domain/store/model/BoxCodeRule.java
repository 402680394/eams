package com.ztdx.eams.domain.store.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
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
    //盒规则类型  1-著录项值 2-档案库所属全宗号 3-固定值 4-流水号
    @Min(value = 1)
    @Max(value = 4)
    @Column(name = "type")
    private int type;
    //值
    @Size(max = 10)
    @Column(name = "value")
    private String value;
    //著录项id
    @Min(value = 0)
    @Column(name = "description_item_id")
    private Integer descriptionItemId;
    //截取长度
    @Min(value = 0)
    @Column(name = "interception_length")
    private byte interceptionLength;
    //流水号长度
    @Min(value = 0)
    @Column(name = "flow_number_length")
    private byte flowNumberLength;
    //备注
    @Size(max = 50)
    @Column(name = "remark")
    private String remark;
    //排序号
    @Min(value = 1)
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
