package com.ztdx.eams.domain.archives.model;

import com.ztdx.eams.basic.repository.annotation.IndexNamePostfix;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Convert;
import java.util.Date;
import java.util.Map;

@Data
@Document(indexName = "archive_record", type = "record", createIndex = false)
@org.springframework.data.mongodb.core.mapping.Document(collection = "archive_record")
public class Entry {
    @Id
    @Field(type = FieldType.Auto)
    private String id;

    /**
     * 目录id
     */
    @IndexNamePostfix
    @Field(type = FieldType.Integer)
    private int catalogueId;

    /**
     * 目录类型
     */
    @Convert(converter = CatalogueType.EnumConverter.class)
    private CatalogueType catalogueType;

    /**
     * 档案库id
     */
    @Field(type = FieldType.Integer)
    private int archiveId;

    /**
     * 档案库类型
     */
    @Field(type = FieldType.Integer)
    private int archiveType;

    /**
     * 档案库内容类型
     */
    @Field(type = FieldType.Integer)
    private int archiveContentType;

    /**
     * 全宗id
     */
    @Field(type = FieldType.Integer)
    private int fondsId;

    /**
     * 创建者
     */
    @Field(type = FieldType.Integer)
    private int owner;

    /**
     * 上级档案目录id
     */
    @Field(type = FieldType.Integer)
    private Integer parentCatalogueId;

    /**
     * 上级条目id
     */
    @Field(type = FieldType.keyword)
    private String parentId;

    /**
     * 著录项明细
     */
    @Field(type = FieldType.Object)
    private Map<String, Object> items;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date)
    private Date gmtCreate;

    /**
     * 修改时间
     */
    @Field(type = FieldType.Date)
    private Date gmtModified;

    /**
     * 是否删除
     */
    @Field(type = FieldType.Integer)
    private int gmtDeleted;

    /**
     * 版本号
     */
    //@Version
    @Field(type = FieldType.keyword)
    private Long version;

    /**
     * 索引的版本号，要使用mongo原生api更新
     */
    @Field(type = FieldType.Long)
    private Long indexVersion;

    /**
     * 开放|受控鉴定
     */
    @Field(type = FieldType.Integer)
    private OpenStatus isOpen;

    /**
     * 到期鉴定(0 未到期(false)  1 已到期(true))
     */
    @Field(type = FieldType.Boolean)
    private boolean isExpired;

    /**
     * 濒危鉴定(0 正常(false)  1 濒危(true))
     */
    @Field(type = FieldType.Boolean)
    private boolean isEndangered;

    /**
     * 遗失鉴定(0 未遗失(false)  1 已遗失(true))
     */
    @Field(type = FieldType.Boolean)
    private boolean isLose;
}
