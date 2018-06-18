package com.ztdx.eams.domain.archives.model.condition;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class EntryCondition {

    @JsonProperty(value = "cid")
    private int catalogueId;

    private String name;

    private List<Condition> conditions;
}
