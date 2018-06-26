package com.ztdx.eams.domain.archives.model.condition;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "archive_entry_search_group")
public class EntrySearchGroup {

    @Id
    private String id;

    private List<EntrySearchGroupItem> entrySearchGroupItem;

}
