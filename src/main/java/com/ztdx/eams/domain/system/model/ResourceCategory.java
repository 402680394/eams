package com.ztdx.eams.domain.system.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ztdx.eams.basic.exception.BusinessException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

public enum ResourceCategory {
    Node(0, "节点"),
    FunctionGroup(1, "功能分组"),
    Function(2, "功能"),
    Global(100, "全局"),
    Fonds(200, "全宗"),
    CatalogueFile(301, "一文一件"),
    CatalogueFolder(302, "传统立卷案卷"),
    CatalogueFolderInside(303, "传统立卷卷内"),
    CatalogueSubject(304, "项目档案"),
    CatalogueUseFile(401, "档案利用-一文一件档案"),
    CatalogueUseFolder(402, "档案利用-传统立卷案卷"),
    CatalogueUseFolderInside(403, "档案利用-传统立卷卷内"),
    CatalogueUseSubject(403, "档案利用-项目档案");

    private Integer code;
    private String description;
    private static final Map<Integer, ResourceCategory> enumMap = new HashMap<>();

    ResourceCategory(Integer code,String description) {
        this.code = code;
        this.description = description;
    }

    static {
        for (ResourceCategory item : values()) {
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
    public static ResourceCategory create(int code) {
        if (!enumMap.containsKey(code))
            throw new BusinessException("不存在类型为" + code + "的档案库类型");

        return enumMap.get(code);
    }

    @Converter(autoApply = true)
    public static class EnumConverter implements AttributeConverter<ResourceCategory, Integer> {

        @Override
        public Integer convertToDatabaseColumn(ResourceCategory attribute) {
            return attribute.getCode();
        }

        @Override
        public ResourceCategory convertToEntityAttribute(Integer dbData) {
            return ResourceCategory.create(dbData);
        }
    }
}
