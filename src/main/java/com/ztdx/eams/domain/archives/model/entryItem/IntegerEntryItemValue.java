package com.ztdx.eams.domain.archives.model.entryItem;

import com.ztdx.eams.domain.archives.model.DescriptionItem;

public class IntegerEntryItemValue extends AbstractEntryItemValue<Integer> {

    public IntegerEntryItemValue(DescriptionItem descriptionItem, Object value) {
        super(descriptionItem, value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    protected Integer getDefault() {
        return null;
    }

    @Override
    protected Integer parse(String value) {
        return Integer.parseInt(value);
    }
}
