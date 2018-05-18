package com.ztdx.eams.domain.archives.model.entryItem;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.domain.archives.model.DescriptionItem;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class DateEntryItemValue extends AbstractEntryItemValue<Date> {
    public DateEntryItemValue(DescriptionItem descriptionItem, Object value) {
        super(descriptionItem, value);
    }

    @Override
    protected Date getDefault() {
        return null;
    }

    @Override
    protected Date parse(String value) {
        DateFormat format = new StdDateFormat();
        try {
            return format.parse(value);
        } catch (ParseException e) {
            throw new EntryValueConverException("值("+value+")无法转换为Date类型");
        }
    }
}
