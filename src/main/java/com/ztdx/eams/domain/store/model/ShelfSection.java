package com.ztdx.eams.domain.store.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Date;

@Entity
@Data
@Table(name = "store_shelf_section")
public class ShelfSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * 密集架id
     */
    @NotNull
    private int shelfId;

    /**
     * 库房id
     */
    @NotNull
    private int storageId;

    /**
     * 全宗id
     */
    @NotNull
    private int fondsId;

    /**
     * 密集架列名称
     */
    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String name;

    /**
     * 密集架列编码
     */
    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String code;

    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String leftTag;

    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String rightTag;

    /**
     * 密集架列类型 1:左面活动架 2:右面活动架 3:双面固定架 4:双面活动架
     */
    @NotNull
    @Min(1)
    @Max(4)
    private int shelfSectionType;

    /**
     * 节数
     */
    @NotNull
    private int sectionColNum;

    /**
     * 层数
     */
    @NotNull
    private int sectionRowNum;

    /**
     * 节长度
     */
    @NotNull
    private int sectionCellLength;

    /**
     * 列宽度
     */
    @NotNull
    private int sectionCellWidth;

    /**
     * 层高度
     */
    @NotNull
    private int sectionCellHeight;

    /**
     * 备注
     */
    @Size(max = 255)
    private String remark;

    /**
     * 创建时间
     */
    @Column(name = "gmt_create", columnDefinition = "timestamp default current_timestamp() not null")
    private Date gmtCreate;

    /**
     * 修改时间
     */
    @Column(name = "gmt_modified", columnDefinition = "timestamp default current_timestamp() not null on update current_timestamp()")
    private Date gmtModified;

    /**
     * 是否删除
     */
    private int gmtDeleted;
}
