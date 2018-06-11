package com.ztdx.eams.domain.archives.model.entryItem;

import com.ztdx.eams.domain.archives.model.DescriptionItem;

public class StringEntryItemValue extends AbstractEntryItemValue<String> {

    public StringEntryItemValue(DescriptionItem descriptionItem, Object value) {
        super(descriptionItem, value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    protected String getDefault() {
        return null;
    }

    @Override
    protected String parse(String value) {
        return value;
    }
}
