package com.ztdx.eams.domain.archives.model.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ztdx.eams.basic.exception.BusinessException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

public enum EntrySearchGroupItemSort {
    ascend("升序"),
    descend("降序");

    private String description;
    static final Map<String, EntrySearchGroupItemSort> enumMap = new HashMap<>();

    EntrySearchGroupItemSort(String description) {
        this.description = description;
    }

    static {
        for (EntrySearchGroupItemSort item : values()) {
            enumMap.put(item.name(), item);
        }
    }

    /**
     * 获取值
     */
    @JsonValue
    public String getCode() {
        return name();
    }

    /**
     * 获取值
     */
    public String getDescription() {
        return description;
    }

    /**
     * 创建枚举的工厂方法
     */
    @JsonCreator
    public static EntrySearchGroupItemSort create(String code) {
        if (!enumMap.containsKey(code))
            throw new BusinessException("不存在类型为" + code + "的排序");

        return enumMap.get(code);
    }

    @Converter(autoApply = true)
    public static class EnumConverter implements AttributeConverter<Operator, String> {

        @Override
        public String convertToDatabaseColumn(Operator attribute) {
            return attribute.getCode();
        }

        @Override
        public Operator convertToEntityAttribute(String dbData) {
            return Operator.create(dbData);
        }
    }

}
