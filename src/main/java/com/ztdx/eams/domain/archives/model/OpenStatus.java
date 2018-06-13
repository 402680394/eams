package com.ztdx.eams.domain.archives.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ztdx.eams.basic.exception.BusinessException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

public enum OpenStatus {

    /**
     *无
     */
    Nothing(1,"无"),

    /**
     *开放
     */
    Opening(2,"开放"),

    /**
     *受控
     */
    Controlled(3,"受控");


    private Integer code;
    private String descpriont;
    private static final Map<Integer, OpenStatus> enumMap = new HashMap<>();


    OpenStatus(Integer code, String descpriont) {
        this.code = code;
        this.descpriont=descpriont;
    }

    static {
        for (OpenStatus item : values()) {
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
    public static OpenStatus create(int code) {
        if (!enumMap.containsKey(code))
            throw new BusinessException("不存在类型为" + code + "的鉴定状态");

        return enumMap.get(code);
    }

    @Converter(autoApply = true)
    public static class EnumConverter implements AttributeConverter<OpenStatus, Integer> {

        @Override
        public Integer convertToDatabaseColumn(OpenStatus attribute) {
            return attribute.getCode();
        }

        @Override
        public OpenStatus convertToEntityAttribute(Integer dbData) {
            return OpenStatus.create(dbData);
        }
    }

}
