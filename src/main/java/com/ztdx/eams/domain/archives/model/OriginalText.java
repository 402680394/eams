package com.ztdx.eams.domain.archives.model;

import com.ztdx.eams.basic.repository.annotation.IndexNamePostfix;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Parent;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by li on 2018/5/22.
 */
@Data
@Document(indexName = "archive_record", type = "originalText", createIndex = false)
@org.springframework.data.mongodb.core.mapping.Document(collection = "archive_record_originalText")
public class OriginalText {

    @Id
    private String id;
    //所属目录
    @IndexNamePostfix
    private int catalogueId;
    //所属条目
    @Parent(type = "record")
    private String entryId;
    //排序号
    private int orderNumber;
    //标题
    @Field(type = FieldType.text)
    private String title;
    //文件类型id
    private int type;
    //文件全名
    @Field(type = FieldType.text)
    private String name;
    //文件大小
    @Field(type = FieldType.keyword)
    private String size;
    //创建时间
    @Field(type = FieldType.Date)
    private Date createTime;
    //版本
    @Field(type = FieldType.keyword)
    private String version;
    //备注
    @Field(type = FieldType.text)
    private String remark;
    //元数据信息
    private HashMap<String, Object> fileAttributesMap;
    //原始文件MD5值
    @Field(type = FieldType.keyword)
    private String md5;
    @Field(type = FieldType.text)
    //全文索引
    private String contentIndex;
    //全文索引状态(0-未生成  1-已生成 2-生成失败 3-文件类型不可用)
    private int contentIndexStatus;
    //pdf转换文件MD5值
    private String pdfMd5;
    //pdf转换状态(0-未转换 1-已转换 2-转换失败 3-文件类型不可用)
    private int pdfConverStatus;
    //创建时间
    @Field(type = FieldType.Date)
    private Date gmtCreate;
    //修改时间
    @Field(type = FieldType.Date)
    private Date gmtModified;
}
