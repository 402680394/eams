package com.ztdx.eams.domain.archives.model;

import com.ztdx.eams.basic.repository.annotation.IndexNamePostfix;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.Convert;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Document(indexName = "archive_record", type = "record", createIndex = false)
@org.springframework.data.mongodb.core.mapping.Document(collection = "archive_record")
public class Entry {
    @Id
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

    /*@Converter(autoApply = false)
    public static class EntryDateConverter implements AttributeConverter<Date, String> {

        @Override
        public String convertToDatabaseColumn(Date attribute) {
            return java.text.DateFormat.getDateInstance().format(attribute);
        }

        @Override
        public Date convertToEntityAttribute(String dbData) {
            try {
                return java.text.DateFormat.getDateInstance().parse(dbData);
            } catch (ParseException e) {
                throw new EntryValueConverException("值("+dbData+")无法转换为Date类型");
            }
        }
    }*/
}
