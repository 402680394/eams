package com.ztdx.eams.domain.archives.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ztdx.eams.basic.exception.BusinessException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by li on 2018/5/21.
 */
public enum DefaultValue {

    Null(1, "空"),

    LoginUserName(2, "当前登录人姓名"),

    SystemYear(3, "当前系统年度"),

    SystemDate_yyyy_MM_dd(4, "当前系统日期(yyyy-MM-dd)"),

    SystemDate_yyyyMMdd(5, "当前系统日期(yyyyMMdd)"),

    SystemDateTime(6, "当前系统日期时间"),

    SystemTime(7, "当前系统时间");

    private Integer code;
    private String descpriont;
    private static final Map<Integer, DefaultValue> enumMap = new HashMap<>();


    DefaultValue(Integer code, String descpriont) {
        this.code = code;
        this.descpriont = descpriont;
    }

    static {
        for (DefaultValue item : values()) {
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
    public static DefaultValue create(int code) {
        if (!enumMap.containsKey(code))
            throw new BusinessException("不存在类型为" + code + "的默认值类型");

        return enumMap.get(code);
    }

    @Converter(autoApply = true)
    public static class EnumConverter implements AttributeConverter<DefaultValue, Integer> {

        @Override
        public Integer convertToDatabaseColumn(DefaultValue attribute) {
            return attribute.getCode();
        }

        @Override
        public DefaultValue convertToEntityAttribute(Integer dbData) {
            return DefaultValue.create(dbData);
        }
    }

}
