package com.ztdx.eams.domain.archives.model.entryItem;

import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.archives.model.Entry;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;

public class DoubleEntryItemValue extends AbstractEntryItemValue<Double> {

    DoubleEntryItemValue(Entry entry, DescriptionItem descriptionItem, Object value, boolean isValidate) {
        super(entry, descriptionItem, value, isValidate);
    }

    @Override
    public String toString() {
        if (value == null){
            return "";
        }
        String format = descriptionItem.getFieldFormat();
        if (StringUtils.isEmpty(format)){
            format = "%f";
        }

        return String.format(format, value);
    }

    @Override
    protected Double getDefault() {
        return null;
    }

    @Override
    protected Double parse(String value) {
        try {
            return Double.parseDouble(value);
        }catch (NumberFormatException e){
            String message = String.format("字段(%s)的值(%s)无法转换为小数类型", descriptionItem.getDisplayName(), value);
            throw new EntryValueConverException(message);
        }
    }
}
