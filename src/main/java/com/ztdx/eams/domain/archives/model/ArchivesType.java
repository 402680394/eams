package com.ztdx.eams.domain.archives.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ztdx.eams.basic.exception.BusinessException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

public enum ArchivesType {

    /**
     * 一文一件
     */
    ArticleOne(1,"一文一件"),

    /**
     * 传统立卷
     */
    TraditionalArchives(2,"传统立卷"),

    /**
     * 项目
     */
    Project(3,"项目");

    private Integer code;
    private String descpriont;
    private static final Map<Integer, ArchivesType> enumMap = new HashMap<>();

    ArchivesType(Integer code,String descpriont) {
        this.code = code;
        this.descpriont=descpriont;
    }

    static {
        for (ArchivesType item : values()) {
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
        return descpriont;
    }

    /**
     * 创建枚举的工厂方法
     */
    @JsonCreator
    public static ArchivesType create(int code) {
        if (!enumMap.containsKey(code))
            throw new BusinessException("不存在类型为" + code + "的档案库类型");

        return enumMap.get(code);
}

    @Converter(autoApply = true)
    public static class EnumConverter implements AttributeConverter<ArchivesType, Integer> {

        @Override
        public Integer convertToDatabaseColumn(ArchivesType attribute) {
            return attribute.getCode();
        }

        @Override
        public ArchivesType convertToEntityAttribute(Integer dbData) {
            return ArchivesType.create(dbData);
        }
    }

}
