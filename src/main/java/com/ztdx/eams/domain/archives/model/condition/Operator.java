package com.ztdx.eams.domain.archives.model.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.domain.archives.model.DescriptionItemDataType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

public enum Operator {
    equal("等于"),
    notEqual("不等于"),
    greaterThan("大于"),
    greaterThanOrEqual("大于等于"),
    lessThan("小于"),
    lessThanOrEqual("小于等于"),
    contain("包含"),
    notContain("不包含");

    private String description;
    static final Map<String, Operator> enumMap = new HashMap<>();

    Operator(String description) {
        this.description = description;
    }

    static {
        for (Operator item : values()) {
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
    public static Operator create(String code) {
        if (!enumMap.containsKey(code))
            throw new BusinessException("不存在类型为" + code + "的运算符");

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

    public enum logical{
        and("且"),
        or("或");

        private String description;
        static final Map<String, logical> enumMap = new HashMap<>();

        logical(String description) {
            this.description = description;
        }

        static {
            for (logical item : values()) {
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
        public static logical create(String code) {
            if (!enumMap.containsKey(code))
                throw new BusinessException("不存在类型为" + code + "的运算符");

            return enumMap.get(code);
        }

        @Converter(autoApply = true)
        public static class EnumConverter implements AttributeConverter<logical, String> {

            @Override
            public String convertToDatabaseColumn(logical attribute) {
                return attribute.getCode();
            }

            @Override
            public logical convertToEntityAttribute(String dbData) {
                return logical.create(dbData);
            }
        }
    }
}
