package com.ztdx.eams.domain.archives.model.entryItem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ArrayEntryItemValue extends AbstractEntryItemValue<ArrayList> {
    private ObjectMapper jsonMapper;

    ArrayEntryItemValue(DescriptionItem descriptionItem, Object value) {
        super(descriptionItem, value);
        jsonMapper = new ObjectMapper();
    }

    @Override
    public String toString() {
        if (value == null){
            return "";
        }
        return value.toString();
    }

    @Override
    protected ArrayList getDefault() {
        return null;
    }

    @Override
    protected ArrayList parse(String value) {
        try {
            return jsonMapper.readValue(value, ArrayList.class);
        } catch (IOException e) {
            return new ArrayList<>(Arrays.asList(",".split(value)));
        }
    }

    @Override
    protected void validateNull() {
        if (value == null || value.size() == 0){
            String message = String.format("字段(%s)不允许为空", descriptionItem.getDisplayName());
            throw new EntryValueConverException(message);
        }
    }
}
