package com.ztdx.eams.domain.archives.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ztdx.eams.basic.exception.BusinessException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

public enum SearchFulltextOption {
    /**
     * 全词匹配
     */
    words(1,"全词匹配"),

    /**
     * 条目
     */
    entry(2,"条目"),

    /**
     * 全文
     */
    file(3,"全文");

    private Integer code;
    private String description;
    private static final Map<Integer, SearchFulltextOption> enumMap = new HashMap<>();


    SearchFulltextOption(Integer code,String description) {
        this.code = code;
        this.description=description;
    }

    static {
        for (SearchFulltextOption item : values()) {
            enumMap.put(item.getCode(), item);
        }
    }

    /**
     * 获取值
     */
    @JsonValue
    public Integer getCode() {
        return code;
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
    public static SearchFulltextOption create(int code) {
        if (!enumMap.containsKey(code))
            throw new BusinessException("不存在类型为" + code + "的档案库类型");

        return enumMap.get(code);
    }

    @Converter(autoApply = true)
    public static class EnumConverter implements AttributeConverter<SearchFulltextOption, String> {

        @Override
        public String convertToDatabaseColumn(SearchFulltextOption attribute) {
            return attribute.name();
        }

        @Override
        public SearchFulltextOption convertToEntityAttribute(String dbData) {
            return SearchFulltextOption.valueOf(dbData);
        }
    }
}
