package com.ztdx.eams.domain.archives.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ztdx.eams.basic.exception.BusinessException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

public enum Structure {

    /**
     * 一文一件
     */
    ArticleOne(1),

    /**
     * 传统立卷
     */
    TraditionalArchives(2),

    /**
     * 项目
     */
    Project(3);

    private Integer code;
    private static final Map<Integer, Structure> enumMap = new HashMap<>();

    Structure(Integer i) {
        code = i;
    }

    static {
        for (Structure item : values()) {
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
     * 创建枚举的工厂方法
     */
    @JsonCreator
    public static Structure create(int code) {
        if (!enumMap.containsKey(code))
            throw new BusinessException("不存在类型为" + code + "的档案库类型");

        return enumMap.get(code);
    }

    @Converter(autoApply = true)
    public static class EnumConverter implements AttributeConverter<Structure, Integer> {

        @Override
        public Integer convertToDatabaseColumn(Structure attribute) {
            return attribute.getCode();
        }

        @Override
        public Structure convertToEntityAttribute(Integer dbData) {
            return Structure.create(dbData);
        }
    }

}
