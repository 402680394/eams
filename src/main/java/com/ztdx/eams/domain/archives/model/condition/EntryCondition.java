package com.ztdx.eams.domain.archives.model.condition;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
//TODO lijie 可以增加字段的类型（著录项的元数据名称/著录项属性类型）
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
    private EntryConditionType entryConditionType;

    /**
     * 条件的主人，对于自定义条件只能修改自己创建的。
     */
    private int owner;
}
