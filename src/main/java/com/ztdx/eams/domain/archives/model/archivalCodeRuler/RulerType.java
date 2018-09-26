package com.ztdx.eams.domain.archives.model.archivalCodeRuler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ztdx.eams.basic.exception.BusinessException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

public enum RulerType {

    /**
     * 著入项值
     */
    EntryValue(1,"著录项值"),

    /**
     * 著录项所对应的参照编码
     */
    ReferenceCode (2,"著录项所对应的参照编码"),

    /**
     * 档案库所属单位全宗号
     */
    FondsCode(3,"档案库所属单位全宗号"),

    /**
     * 固定值
     */
    FixValue(4,"固定值"),

    /**
     * 序号
     */
    SerialNumber (5,"序号");

    private Integer code;
    private String descpriont;
    private static final Map<Integer, RulerType> enumMap = new HashMap<>();


    RulerType(Integer code, String descpriont) {
        this.code = code;
        this.descpriont=descpriont;
    }

    static {
        for (RulerType item : values()) {
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
    public static RulerType create(int code) {
        if (!enumMap.containsKey(code))
            throw new BusinessException("不存在类型为" + code + "的档案库类型");

        return enumMap.get(code);
    }

    @Converter(autoApply = true)
    public static class EnumConverter implements AttributeConverter<RulerType, Integer> {

        @Override
        public Integer convertToDatabaseColumn(RulerType attribute) {
            return attribute.getCode();
        }

        @Override
        public RulerType convertToEntityAttribute(Integer dbData) {
            return RulerType.create(dbData);
        }
    }

}
