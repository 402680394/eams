package com.ztdx.eams.domain.archives.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ztdx.eams.basic.exception.BusinessException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by li on 2018/5/18.
 */
public enum PropertyType {

    Reference(1,"档号"),

    Title(2,"题名"),

    SerialNumber(3,"序号"),

    Year(4,"年度"),

    ClassificationNumber(5,"分类号"),

    BoxNumber(6,"盒号"),

    PageTotal(7,"页数"),

    PageNumber(8,"页次"),

    TimeLimitForStorage(9,"保管期限"),

    Rank(10,"密级"),

    ReferenceNumber(11,"文号"),

    RecordType(12,"文种"),

    department(13,"部门"),

    CarrierType(14,"载体类型"),

    EntitiesNumber(15,"实体份数"),

    EntryName(16,"项目名称"),

    ProjecNumber(17,"项目编号"),

    Other(18,"其他"),

    SeeNumber(19,"参见号"),

    barCode(20,"条形码"),

    EntityCurrentNumber(21,"实体当前份数");

    private Integer code;
    private String descpriont;
    private static final Map<Integer, PropertyType> enumMap = new HashMap<>();


    PropertyType(Integer code,String descpriont) {
        this.code = code;
        this.descpriont=descpriont;
    }

    static {
        for (PropertyType item : values()) {
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
    public String getDescpriont() {
        return descpriont;
    }

    /**
     * 创建枚举的工厂方法
     */
    @JsonCreator
    public static PropertyType create(int code) {
        if (!enumMap.containsKey(code))
            throw new BusinessException("不存在类型为" + code + "的字段属性类型");

        return enumMap.get(code);
    }

    @Converter(autoApply = true)
    public static class EnumConverter implements AttributeConverter<PropertyType, Integer> {

        @Override
        public Integer convertToDatabaseColumn(PropertyType attribute) {
            return attribute.getCode();
        }

        @Override
        public PropertyType convertToEntityAttribute(Integer dbData) {
            return PropertyType.create(dbData);
        }
    }

}
