package com.ztdx.eams.domain.store.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ztdx.eams.basic.exception.BusinessException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

public enum MonitoringPointType {

    /**
     * 温度记录仪
     */
    TemperatureRecorder(1,"温度记录仪"),

    /**
     * 湿度记录仪
     */
    HumidityRecorder(2,"湿度记录仪"),

    /**
     * 温湿度记录仪
     */
    TemperatureAndHumidityRecorder(3,"温湿度记录仪");

    private Integer code;
    private String descpriont;
    private static final Map<Integer, MonitoringPointType> enumMap = new HashMap<>();


    MonitoringPointType(Integer code, String descpriont) {
        this.code = code;
        this.descpriont=descpriont;
    }

    static {
        for (MonitoringPointType item : values()) {
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
    public static MonitoringPointType create(int code) {
        if (!enumMap.containsKey(code))
            throw new BusinessException("不存在类型为" + code + "的档案库类型");

        return enumMap.get(code);
    }

    @Converter(autoApply = true)
    public static class EnumConverter implements AttributeConverter<MonitoringPointType, Integer> {

        @Override
        public Integer convertToDatabaseColumn(MonitoringPointType attribute) {
            return attribute.getCode();
        }

        @Override
        public MonitoringPointType convertToEntityAttribute(Integer dbData) {
            return MonitoringPointType.create(dbData);
        }
    }

}
