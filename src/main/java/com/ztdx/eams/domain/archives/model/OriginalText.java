package com.ztdx.eams.domain.archives.model;

import com.ztdx.eams.basic.repository.annotation.IndexNamePostfix;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by li on 2018/5/22.
 */
@Data
@Document(collection = "archive_record_originalText")
public class OriginalText {

    @Id
    private UUID id;

    @IndexNamePostfix
    private int catalogueId;

    private UUID entryId;

    private String title;

    private String fileType;

    private String version;

    private String remark;

    private String extension;

    private String fileName;

    private HashMap<String, Object> fileAttributesMap;
}
