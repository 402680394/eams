package com.ztdx.eams.domain.archives.model.condition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ztdx.eams.domain.archives.model.DescriptionItemDataType;
import lombok.Data;

import javax.persistence.Convert;
import java.util.List;

@Data
public class EntryCondition {

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
