package com.ztdx.eams.domain.archives.model;

import com.ztdx.eams.basic.repository.annotation.IndexNamePostfix;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Document(indexName = "archive_record", type = "record", createIndex = false)
@org.springframework.data.mongodb.core.mapping.Document(collection = "archive_record")
public class Entry {
    @Id
    private UUID id;

    /**
     * 目录id
     */
    @IndexNamePostfix
    private int catalogueId;

    /**
     * 目录类型
     */
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
     * 著录项明细
     */
    private Map<String, Object> items;

    /**
     * 原文
     */
    private List<OriginalText> originalText;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 修改时间
     */
    private Date gmtModified;
}
