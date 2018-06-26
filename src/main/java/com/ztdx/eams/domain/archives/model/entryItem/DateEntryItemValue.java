package com.ztdx.eams.domain.archives.model.entryItem;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.domain.archives.model.DefaultValue;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import static org.apache.commons.lang.StringUtils.isNumeric;

public class DateEntryItemValue extends AbstractEntryItemValue<Date> {

    DateEntryItemValue(DescriptionItem descriptionItem, Object value, boolean isValidate) {
        super(descriptionItem, value, isValidate);
    }

    @Override
    public String toString() {
        if (value == null){
            return "";
        }
        String format = descriptionItem.getFieldFormat();
        if (StringUtils.isEmpty(format)){
            if (descriptionItem.getDefaultValue() == null){
                descriptionItem.setDefaultValue(DefaultValue.Null);
            }
            switch (descriptionItem.getDefaultValue()){
                case SystemTime:
                    format = "HH:mm:ss";
                    break;
                case SystemDate_yyyyMMdd:
                    format = "yyyyMMdd";
                    break;
                case SystemDate_yyyy_MM_dd:
                    format = "yyyy-MM-dd";
                    break;
                case SystemYear:
                    format = "yyyy";
                    break;
                default:
                    format = "yyyy-MM-dd HH:mm:ss";
            }
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(value);
    }

    @Override
    protected Date getDefault() {
        if (descriptionItem.getDefaultValue() == null){
            descriptionItem.setDefaultValue(DefaultValue.Null);
        }
        switch (descriptionItem.getDefaultValue()){
            case SystemTime:
            case SystemDate_yyyyMMdd:
            case SystemDate_yyyy_MM_dd:
            case SystemYear:
            case SystemDateTime:
                return new Date();
            default:
                return null;
        }
    }

    @Override
    protected Date parse(String value) {

        //如果是时间戳
        if (StringUtils.isNumeric(value)) {
            Long timeStamp = Long.parseLong(value);
            if (value.length() > 12){
                return Date.from(Instant.ofEpochMilli(timeStamp));
            }else {
                return Date.from(Instant.ofEpochSecond(timeStamp));
            }
        }

        try {
            return StdDateFormat.getDateInstance().parse(value);
        } catch (ParseException e) {
            String message = String.format("字段(%s)的值(%s)无法转换为日期类型", descriptionItem.getDisplayName(), value);
            throw new EntryValueConverException(message);
        }
    }
}
