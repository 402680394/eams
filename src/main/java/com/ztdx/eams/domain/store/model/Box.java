package com.ztdx.eams.domain.store.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by li on 2018/7/6.
 */
@Data
@Entity
@Table(name = "store_box")
public class Box {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    //盒号
    @Size(max = 100)
    @Column(name = "code")
    private String code;
    //库ID
    @Column(name = "archives_id")
    private int archivesId;
    //盒号生成规则
    @Size(max = 50)
    @Column(name = "code_rule")
    private String codeRule;
    //流水号
    @Size(max = 50)
    @Column(name = "flow_number")
    private String flowNumber;
    //文件份数
    @Min(value = 0)
    @Column(name = "files_total")
    private int filesTotal;
    //文件页数
    @Min(value = 0)
    @Column(name = "pages_total")
    private int pagesTotal;
    //文件存储最大页数
    @Min(value = 0)
    @Column(name = "max_pages_total")
    private int maxPagesTotal;
    //盒子宽度
    @Min(value = 0)
    @Column(name = "width")
    private int width;
    //备注
    @Size(max = 100)
    @Column(name = "remark")
    private String remark;
    //是否在架
    @Column(name = "on_frame")
    private int onFrame;
    //放置位置编码
    @Size(max = 50)
    @Column(name = "point")
    private String point;

    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;

    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
