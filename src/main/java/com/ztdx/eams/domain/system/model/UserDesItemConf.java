package com.ztdx.eams.domain.system.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "sys_user_des_item_conf")
public class UserDesItemConf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "catalogue_id")
    private int catalogueId;

    @Column(name = "description_item_id")
    private int descriptionItemId;

    @Column(name = "order_number")
    private int orderNumber;

    @Column(name = "width")
    private Integer width;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
