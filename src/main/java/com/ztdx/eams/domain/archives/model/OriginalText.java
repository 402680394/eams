package com.ztdx.eams.domain.archives.model;

import com.ztdx.eams.basic.repository.annotation.IndexNamePostfix;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by li on 2018/5/22.
 */
@Data
@Document(collection = "archive_record_originalText")
public class OriginalText {

    @Id
    private String id;

    @IndexNamePostfix
    private int catalogueId;

    private String entryId;

    private String title;

    private int type;

    private String name;

    private String createTime;

    private String size;

    private String version;

    private String remark;

    private HashMap<String, Object> fileAttributesMap;

    private String ftpFileMD5;

    //创建时间
    private Date gmtCreate;

    //修改时间
    private Date gmtModified;
}
