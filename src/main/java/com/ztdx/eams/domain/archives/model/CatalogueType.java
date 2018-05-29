package com.ztdx.eams.domain.archives.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ztdx.eams.basic.exception.BusinessException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

public enum CatalogueType {
    Undefined(0, "未定义"),
    File(1, "一文一件"),
    Folder(2, "传统立卷案卷"),
    FolderFile(3, "传统立卷卷内"),
    Subject(4, "项目");


    private Integer code;
    private String description;
    private static final Map<Integer, CatalogueType> enumMap = new HashMap<>();

    CatalogueType(Integer code,String description) {
        this.code = code;
        this.description = description;
    }

    static {
        for (CatalogueType item : values()) {
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
    public static CatalogueType create(int code) {
        if (!enumMap.containsKey(code))
            throw new BusinessException("不存在类型为" + code + "的档案目录类型");

        return enumMap.get(code);
    }

    @Converter(autoApply = true)
    public static class EnumConverter implements AttributeConverter<CatalogueType, Integer> {

        @Override
        public Integer convertToDatabaseColumn(CatalogueType attribute) {
            return attribute.getCode();
        }

        @Override
        public CatalogueType convertToEntityAttribute(Integer dbData) {
            return CatalogueType.create(dbData);
        }
    }
}
