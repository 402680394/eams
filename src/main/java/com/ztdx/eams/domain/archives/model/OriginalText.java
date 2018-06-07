package com.ztdx.eams.domain.archives.model;

import com.ztdx.eams.basic.repository.annotation.IndexNamePostfix;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

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
    private String entryId;
    //排序号
    private int orderNumber;
    //标题
    private String title;
    //文件类型id
    private int type;
    //文件全名
    private String name;
    //文件大小
    private String size;
    //创建时间
    private Date createTime;
    //版本
    private String version;
    //备注
    private String remark;
    //全文索引
    private String contentIndex;
    //全文索引状态(0-未生成  1-已生成)
    private int contentIndexStatus;
    //元数据信息
    private HashMap<String, Object> fileAttributesMap;
    //原始文件MD5值
    private String md5;

    //创建时间
    private Date gmtCreate;

    //修改时间
    private Date gmtModified;
}
