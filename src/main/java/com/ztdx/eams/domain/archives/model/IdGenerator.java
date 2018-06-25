package com.ztdx.eams.domain.archives.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "archive_id_generator")
@Data
public class IdGenerator {

    @Id
    private String Id;

    private Long value;

}
