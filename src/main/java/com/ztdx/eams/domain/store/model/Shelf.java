package com.ztdx.eams.domain.store.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Date;

@Entity
@Data
@Table(name = "store_shelf")
public class Shelf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * 库房id
     */
    @NotNull
    private int storageId;

    /**
     * 全宗id
     */
    private int fondsId;

    /**
     * 密集架名称
     */
    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String name;

    /**
     * 密集架编码
     */
    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String code;

    /**
     * 密集架类型 1:自动 2:手动
     */
    @NotNull
    @Min(1)
    @Max(2)
    private int shelfType;

    /**
     * 备注
     */
    @Size(max = 255)
    private String remark;

    /**
     * 创建时间
     */
    @Column(columnDefinition = "timestamp default current_timestamp() not null")
    private Date gmtCreate;

    /**
     * 修改时间
     */
    @Column(columnDefinition = "timestamp default current_timestamp() not null on update current_timestamp()")
    private Date gmtModified;

    /**
     * 是否删除
     */
    private int gmtDeleted;

    /**
     * 列数
     */
    @Min(1)
    @Max(99999)
    private int sectionNum;

    /**
     * 节数
     */
    @Min(1)
    @Max(99999)
    private int sectionColNum;

    /**
     * 层数
     */
    @Min(1)
    @Max(99999)
    private int sectionRowNum;

    /**
     * 节长度
     */
    @Min(1)
    @Max(99999)
    private int sectionCellLength;

    /**
     * 列宽度
     */
    @Min(1)
    @Max(99999)
    private int sectionCellWidth;

    /**
     * 层高度
     */
    @Min(1)
    @Max(99999)
    private int sectionCellHeight;

    /**
     * 列的名称前缀
     */
    @Column(length = 50)
    private String sectionNamePrefix;

    /**
     * 列的编码前缀
     */
    @Column(length = 50)
    private String sectionCodePrefix;

    /**
     * 列的起始编号
     */
    @Min(1)
    @Max(99999)
    private Integer sectionStartSn;
}
