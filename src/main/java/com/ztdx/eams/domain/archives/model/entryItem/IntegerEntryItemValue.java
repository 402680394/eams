package com.ztdx.eams.domain.archives.model.entryItem;

import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.domain.archives.model.DescriptionItem;

public class IntegerEntryItemValue extends AbstractEntryItemValue<Integer> {

    IntegerEntryItemValue(DescriptionItem descriptionItem, Object value, boolean isValidate) {
        super(descriptionItem, value, isValidate);
    }

    @Override
    public String toString() {
        if (value == null){
            return "";
        }
        return value.toString();
    }

    @Override
    protected Integer getDefault() {
        return null;
    }

    @Override
    protected Integer parse(String value) {
        try {
            return Integer.parseInt(value);
        }catch (NumberFormatException e){
            String message = String.format("字段(%s)的值(%s)无法转换为整数类型", descriptionItem.getDisplayName(), value);
            throw new EntryValueConverException(message);
        }
    }
}
