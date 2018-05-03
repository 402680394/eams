package com.ztdx.eams.domain.archives.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ztdx.eams.basic.exception.BusinessException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

public enum ArchivesType {

    /** 一文一件
     */
    ArticleOne(1),

    /** 传统立卷
     */
    TraditionalArchives(2),

    /** 项目
     */
    Project(3);

    private Integer code;
    ArchivesType(Integer i){
        code =i;
    }

    /**
     * 获取值
     */
    @JsonValue
    public Integer getCode(){
        return code;
    }

    @JsonCreator
    public static ArchivesType create(int code){
        switch (code){
            case 1 : return ArchivesType.ArticleOne;
            case 2 : return ArchivesType.TraditionalArchives;
            case 3 : return ArchivesType.Project;
            default:
                throw new BusinessException("不存在类型为"+code+"的档案库类型");
        }
    }

    @Converter(autoApply=true)
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
