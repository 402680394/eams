package com.ztdx.eams.domain.archives.model;

import com.ztdx.eams.basic.repository.annotation.IndexNamePostfix;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Data
@Document(indexName = "archive_record", type = "record", createIndex = false)
@org.springframework.data.mongodb.core.mapping.Document(collection = "archive_record")
public class Entry {
    @Id
    private UUID id;

    @IndexNamePostfix
    private long catalogueId;

    private Map<String, Object> items;

    //创建时间
    private Date gmtCreate;

    //修改时间
    private Date gmtModified;
}
