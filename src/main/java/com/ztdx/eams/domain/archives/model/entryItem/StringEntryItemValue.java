package com.ztdx.eams.domain.archives.model.entryItem;

import com.ztdx.eams.basic.WorkContext;
import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.domain.archives.model.DefaultValue;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringEntryItemValue extends AbstractEntryItemValue<String> {

    StringEntryItemValue(DescriptionItem descriptionItem, Object value) {
        super(descriptionItem, value);
    }

    @Override
    public String toString() {
        if (value == null){
            return "";
        }
        return value;
    }

    @Override
    protected String getDefault() {
        if (descriptionItem.getDefaultValue() == null){
            descriptionItem.setDefaultValue(DefaultValue.Null);
        }
        String format;
        switch (descriptionItem.getDefaultValue()) {
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
            case SystemDateTime:
                format = "yyyy-MM-dd HH:mm:ss";
                break;
            case LoginUserName:
                return "";
            default:
                return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(new Date());
    }

    @Override
    protected String parse(String value) {
        return value;
    }

    @Override
    protected void validateNull() {
        if (StringUtils.isEmpty(value)){
            String message = String.format("字段(%s)不允许为空", descriptionItem.getDisplayName());
            throw new EntryValueConverException(message);
        }
    }
}
