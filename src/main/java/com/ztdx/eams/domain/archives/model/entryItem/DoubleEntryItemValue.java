package com.ztdx.eams.domain.archives.model.entryItem;

import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.domain.archives.model.DescriptionItem;

public class DoubleEntryItemValue extends AbstractEntryItemValue<Double> {
    public DoubleEntryItemValue(DescriptionItem descriptionItem, Object value) {
        super(descriptionItem, value);
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
