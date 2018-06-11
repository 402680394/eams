package com.ztdx.eams.domain.archives.model.entryItem;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import static org.apache.commons.lang.StringUtils.isNumeric;

public class DateEntryItemValue extends AbstractEntryItemValue<Date> {

    public DateEntryItemValue(DescriptionItem descriptionItem, Object value) {
        super(descriptionItem, value);
    }

    @Override
    public String toString() {
        String format = descriptionItem.getFieldFormat();
        if (StringUtils.isEmpty(format)){
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(value);
    }

    @Override
    protected Date getDefault() {
        return null;
    }

    @Override
    protected Date parse(String value) {

        //如果是时间戳
        if (StringUtils.isNumeric(value)) {
            return Date.from(Instant.ofEpochSecond(Long.parseLong(value)));
        }

        try {
            return StdDateFormat.getDateInstance().parse(value);
        } catch (ParseException e) {
            throw new EntryValueConverException("值("+value+")无法转换为Date类型");
        }
    }
}
