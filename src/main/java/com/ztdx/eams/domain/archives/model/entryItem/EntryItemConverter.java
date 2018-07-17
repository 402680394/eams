package com.ztdx.eams.domain.archives.model.entryItem;

import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.archives.model.DescriptionItemDataType;
import com.ztdx.eams.domain.archives.model.Entry;

public class EntryItemConverter {

    public static Object from(Entry entry, DescriptionItem descriptionItem) {
        Object value = entry.getItems().getOrDefault(descriptionItem.getMetadataName(), null);
        return parse(entry, value, descriptionItem).get();
    }

    public static String format(Entry entry, DescriptionItem descriptionItem) {
        Object value = entry.getItems().getOrDefault(descriptionItem.getMetadataName(), null);
        return parse(entry, value, descriptionItem, false).toString();
    }

    private static IEntryItemValue parse(Entry entry, Object value, DescriptionItem descriptionItem){
        return parse(entry, value, descriptionItem, true);
    }

    private static IEntryItemValue parse(Entry entry, Object value, DescriptionItem descriptionItem, boolean isValidate){
        DescriptionItemDataType dataType = descriptionItem.getDataType();
        switch (dataType) {
            case Integer:
                return new IntegerEntryItemValue(entry, descriptionItem, value, isValidate);
            case String:
            case Text:
                return new StringEntryItemValue(entry, descriptionItem, value, isValidate);
            case Array:
                return new ArrayEntryItemValue(entry, descriptionItem, value, isValidate);
            case Date:
                return new DateEntryItemValue(entry, descriptionItem, value, isValidate);
            case Double:
                return new DoubleEntryItemValue(entry, descriptionItem, value, isValidate);
            default:
                throw new EntryValueConverException("不识别的类型:"+dataType.getDescription());
        }
    }
}
