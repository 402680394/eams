package com.ztdx.eams.domain.store.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Data
@Table(name = "store_shelf_cell")
public class ShelfCell {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * 密集架列id
     */
    @NotNull
    private int shelfSectionId;

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
     * 库位码
     */
    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String pointCode;

    /**
     * 密集架格编码
     */
    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String code;

    /**
     * 左右面 1:左面 2:右面
     */
    @NotNull
    private int side;

    /**
     * 条形码
     */
    @Size(max = 50)
    @Column(length = 50)
    private String barCode;

    /**
     * 节号
     */
    @NotNull
    private int columnNo;

    /**
     * 层号
     */
    @NotNull
    private int rowNo;

    /**
     * 节长度
     */
    @NotNull
    private int sectionCellLength;

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
}
