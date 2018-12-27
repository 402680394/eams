package com.ztdx.eams.domain.archives.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ztdx.eams.basic.exception.BusinessException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

/**
 * 1.数值 2.文本 3.日期 4.浮点 5.字符串 6.数组
 */
public enum DescriptionItemDataType {

    /**
     * 数值
     */
    Integer(1, "整数"),

    /**
     * 文本
     */
    Text(2, "文本"),

    /**
     * 日期
     */
    Date(3, "日期"),

    /**
     * 浮点
     */
    Double(4, "浮点"),

    /**
     * 字符串
     */
    String(5, "字符串"),

    /**
     * 数组
     */
    Array(6, "数组");

    private Integer code;
    private String description;
    private static final Map<Integer, DescriptionItemDataType> enumMap = new HashMap<>();

    DescriptionItemDataType(Integer code,String description) {
        this.code = code;
        this.description = description;
    }

    static {
        for (DescriptionItemDataType item : values()) {
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
    public static DescriptionItemDataType create(int code) {
        if (!enumMap.containsKey(code))
            throw new BusinessException("不存在类型为" + code + "的档案库类型");

        return enumMap.get(code);
    }

    @Converter(autoApply = true)
    public static class EnumConverter implements AttributeConverter<DescriptionItemDataType, Integer> {

        @Override
        public Integer convertToDatabaseColumn(DescriptionItemDataType attribute) {
            return attribute.getCode();
        }

        @Override
        public DescriptionItemDataType convertToEntityAttribute(Integer dbData) {
            return DescriptionItemDataType.create(dbData);
        }
    }
}
