package com.ztdx.eams.domain.archives.model.entryItem;

import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.archives.model.DescriptionItemDataType;

public class EntryItemConverter {

    public static Object from(Object value, DescriptionItem descriptionItem) {
        return parse(value, descriptionItem).get();
    }

    public static String format(Object value, DescriptionItem descriptionItem) {
        return parse(value, descriptionItem).toString();
    }

    private static IEntryItemValue parse(Object value, DescriptionItem descriptionItem){
        DescriptionItemDataType dataType = descriptionItem.getDataType();
        switch (dataType) {
            case Integer:
                return new IntegerEntryItemValue(descriptionItem, value);
            case String:
            case Text:
                return new StringEntryItemValue(descriptionItem, value);
            case Array:
                return new ArrayEntryItemValue(descriptionItem, value);
            case Date:
                return new DateEntryItemValue(descriptionItem, value);
            case Double:
                return new DoubleEntryItemValue(descriptionItem, value);
            default:
                throw new EntryValueConverException("不识别的类型:"+dataType.getDescription());
        }
    }
}
