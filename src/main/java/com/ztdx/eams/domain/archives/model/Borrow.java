package com.ztdx.eams.domain.archives.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "archives_borrow")
public class Borrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //申请单号
    @Column(name = "code")
    private String code;

    //申请人ID
    @Column(name = "applicant_id")
    private int applicantId;
    //申请人名称
    @Size(max = 50)
    @Column(name = "applicant_name")
    private String applicantName;
    //申请日期
    @Column(name = "application_date")
    private Date applicationDate;
    //部门
    @Size(max = 50)
    @Column(name = "department")
    private String department;
    //电子邮件
    @Size(max = 50)
    @Column(name = "email")
    private String email;
    //电话
    @Size(max = 50)
    @Column(name = "tel")
    private String tel;
    //借阅天数
    @Min(value = 0)
    @Column(name = "days")
    private int days;
    //借阅目的
    @Column(name = "objective")
    private String objective;
    //借阅类型（1-电子利用 2-实体外借 3-实体查阅）
    @Min(value = 1)
    @Max(value = 3)
    @Column(name = "type")
    private int type;
    //是否查看（0-否 1-是）
    @Min(value = 0)
    @Max(value = 1)
    @Column(name = "is_see")
    private int isSee;
    //是否打印
    @Min(value = 0)
    @Max(value = 1)
    @Column(name = "is_print")
    private int isPrint;
    //是否下载
    @Min(value = 0)
    @Max(value = 1)
    @Column(name = "is_download")
    private int isDownload;
    //是否复制
    @Min(value = 0)
    @Max(value = 1)
    @Column(name = "is_copy")
    private int isCopy;
    //是否手抄
    @Min(value = 0)
    @Max(value = 1)
    @Column(name = "is_handwrite")
    private int isHandwrite;
    //利用效果
    @Column(name = "effect")
    private String effect;
    //简要说明
    @Column(name = "descript")
    private String descript;
    //审批状态（0-待审批 1-已完成 2-已拒绝）
    @Min(value = 0)
    @Max(value = 2)
    @Column(name = "state")
    private int state;
    //创建时间
    @Column(name = "gmt_create")
    private Date gmtCreate;
    //修改时间
    @Column(name = "gmt_modified")
    private Date gmtModified;
}
