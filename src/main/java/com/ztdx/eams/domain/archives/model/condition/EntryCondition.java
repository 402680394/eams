package com.ztdx.eams.domain.archives.model.condition;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Convert;
import java.util.List;

@Data
@Document(collection = "archive_entry_condition")
public class EntryCondition {

    @Id
    private String id;

    @JsonProperty(value = "cid")
    private int catalogueId;

    private String name;

    private List<Condition> conditions;

    /**
     * 条件的类型，用于区分系统和自定义。不需要前端传
     */
    @Convert(converter = EntryConditionType.class)
    private EntryConditionType entryConditionType;

    /**
     * 条件的主人，对于自定义条件只能修改自己创建的。
     */
    private int owner;
}