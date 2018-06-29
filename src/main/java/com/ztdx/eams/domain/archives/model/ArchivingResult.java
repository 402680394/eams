package com.ztdx.eams.domain.archives.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ztdx.eams.basic.exception.BusinessException;
import lombok.Data;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ArchivingResult {
    private String id;
    private String parentId;
    private String title;
    private String errorMsg;
    @Convert(converter = Type.EnumConverter.class)
    private Type type;
    @Convert(converter = Status.EnumConverter.class)
    private Status status;
    private List<ArchivingResult> children;

    public ArchivingResult(){
        children = new ArrayList<>();
    }

    public ArchivingResult(String id, String parentId, String title, String errorMsg, Type type, Status status) {
        this();
        this.id = id;
        this.parentId = parentId;
        this.title = title;
        this.errorMsg = errorMsg;
        this.type = type;
        this.status = status;
    }

    public enum Status{
        /**
         * 成功
         */
        success(1,"成功"),

        /**
         * 失败
         */
        failure(2,"失败");

        private Integer code;
        private String description;
        private static final Map<Integer, Status> enumMap = new HashMap<>();


        Status(Integer code,String description) {
            this.code = code;
            this.description=description;
        }

        static {
            for (Status item : values()) {
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
        public static Status create(int code) {
            if (!enumMap.containsKey(code))
                throw new BusinessException("不存在类型为" + code + "的档案库类型");

            return enumMap.get(code);
        }

        @Converter(autoApply = true)
        public static class EnumConverter implements AttributeConverter<Status, String> {

            @Override
            public String convertToDatabaseColumn(Status attribute) {
                return attribute.name();
            }

            @Override
            public Status convertToEntityAttribute(String dbData) {
                return Status.valueOf(dbData);
            }
        }
    }

    public enum Type{
        /**
         * 条目
         */
        entry(1,"条目"),

        /**
         * 原文
         */
        file(2,"原文");

        private Integer code;
        private String description;
        private static final Map<Integer, Type> enumMap = new HashMap<>();


        Type(Integer code,String description) {
            this.code = code;
            this.description=description;
        }

        static {
            for (Type item : values()) {
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
        public static Type create(int code) {
            if (!enumMap.containsKey(code))
                throw new BusinessException("不存在类型为" + code + "的档案库类型");

            return enumMap.get(code);
        }

        @Converter(autoApply = true)
        public static class EnumConverter implements AttributeConverter<Type, String> {

            @Override
            public String convertToDatabaseColumn(Type attribute) {
                return attribute.name();
            }

            @Override
            public Type convertToEntityAttribute(String dbData) {
                return Type.valueOf(dbData);
            }
        }
    }
}
