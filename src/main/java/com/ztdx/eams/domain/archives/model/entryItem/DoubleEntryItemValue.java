package com.ztdx.eams.domain.archives.model.entryItem;

import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;

public class DoubleEntryItemValue extends AbstractEntryItemValue<Double> {

    public DoubleEntryItemValue(DescriptionItem descriptionItem, Object value) {
        super(descriptionItem, value);
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
        return 0D;
    }

    @Override
    protected Double parse(String value) {
        try {
            return Double.parseDouble(value);
        }catch (NumberFormatException e){
            throw new EntryValueConverException("值("+value+")无法转换为Double类型");
        }
    }
}
