package com.ztdx.eams.domain.archives.model;

import com.ztdx.eams.basic.repository.annotation.IndexNamePostfix;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Convert;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Document(indexName = "archive_record", type = "record", createIndex = false)
@org.springframework.data.mongodb.core.mapping.Document(collection = "archive_record")
public class Entry {
    @Id
    @Field(type = FieldType.keyword)
    private String id;

    /**
     * 目录id
     */
    @IndexNamePostfix
    private int catalogueId;

    /**
     * 目录类型
     */
    @Convert(converter = CatalogueType.EnumConverter.class)
    private CatalogueType catalogueType;

    /**
     * 档案库id
     */
    private int archiveId;

    /**
     * 档案库类型
     */
    private int archiveType;

    /**
     * 档案库内容类型
     */
    private int archiveContentType;

    /**
     * 全宗id
     */
    private int fondsId;

    /**
     * 创建者
     */
    private int owner;

    /**
     * 上级档案目录id
     */
    private int parentCatalogueId;

    /**
     * 上级条目id
     */
    private String parentId;

    /**
     * 著录项明细
     */
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
    private int gmtDeleted;

    /**
     * 版本号
     */
    @Version
    private Long version;

    /**
     * 索引的版本号，要使用mongo原生api更新
     */
    private Long indexVersion;
}
