package com.ztdx.eams.domain.archives.model.entryItem;

import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.archives.model.DescriptionItemDataType;

public class EntryItemConverter {

    public static Object from(Object value, DescriptionItem descriptionItem) {
        return parse(value, descriptionItem).get();
    }

    public static String format(Object value, DescriptionItem descriptionItem) {
        return parse(value, descriptionItem, false).toString();
    }

    private static IEntryItemValue parse(Object value, DescriptionItem descriptionItem){
        return parse(value, descriptionItem, true);
    }

    private static IEntryItemValue parse(Object value, DescriptionItem descriptionItem, boolean isValidate){
        DescriptionItemDataType dataType = descriptionItem.getDataType();
        switch (dataType) {
            case Integer:
                return new IntegerEntryItemValue(descriptionItem, value, isValidate);
            case String:
            case Text:
                return new StringEntryItemValue(descriptionItem, value, isValidate);
            case Array:
                return new ArrayEntryItemValue(descriptionItem, value, isValidate);
            case Date:
                return new DateEntryItemValue(descriptionItem, value, isValidate);
            case Double:
                return new DoubleEntryItemValue(descriptionItem, value, isValidate);
            default:
                throw new EntryValueConverException("不识别的类型:"+dataType.getDescription());
        }
    }
}
