package com.ztdx.eams.domain.archives.model;

import com.ztdx.eams.basic.exception.BusinessException;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

public enum ArchivesType {

    ArticleOne(1),
    TraditionalArchives(2),
    Project(3);

    private Integer value;
    ArchivesType(Integer i){
        value=i;
    }

    @Converter
    public class ArchivesTypeConverter implements AttributeConverter<ArchivesType, Integer> {

        @Override
        public Integer convertToDatabaseColumn(ArchivesType attribute) {
            return attribute.value;
        }

        @Override
        public ArchivesType convertToEntityAttribute(Integer dbData) {

            switch (dbData){
                case 1 : return ArchivesType.ArticleOne;
                case 2 : return ArchivesType.TraditionalArchives;
                case 3 : return ArchivesType.Project;
                default:
                    throw new BusinessException("不存在类型为"+dbData+"的档案库类型");
            }

        }
    }
}
