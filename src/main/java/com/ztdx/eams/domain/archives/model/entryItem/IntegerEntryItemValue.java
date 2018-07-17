package com.ztdx.eams.domain.archives.model.entryItem;

import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.archives.model.Entry;

public class IntegerEntryItemValue extends AbstractEntryItemValue<Integer> {

    IntegerEntryItemValue(Entry entry, DescriptionItem descriptionItem, Object value, boolean isValidate) {
        super(entry, descriptionItem, value, isValidate);
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
