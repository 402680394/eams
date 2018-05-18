package com.ztdx.eams.domain.archives.model.entryItem;

import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.archives.model.DescriptionItemDataType;

public class EntryItemConverter {

    public static Object from(Object value, DescriptionItem descriptionItem) {
        DescriptionItemDataType dataType = DescriptionItemDataType.create(descriptionItem.getDataType());
        switch (dataType) {
            case Integer:
                return new IntegerEntryItemValue(descriptionItem, value).get();
            case String:
            case Text:
                return new StringEntryItemValue(descriptionItem, value).get();
            case Array:
                return new ArrayEntryItemValue(descriptionItem, value).get();
            case Date:
                return new DateEntryItemValue(descriptionItem, value).get();
            case Double:
                return new DoubleEntryItemValue(descriptionItem, value).get();
            default:
                throw new EntryValueConverException("不识别的类型:"+dataType.getDescription());
        }

    }
}
