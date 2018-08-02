package com.ztdx.eams.domain.archives.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TermsAggregationParam {
    private String field;
    private String name;
    private TermsAggregationParamFieldType fieldType;
    private int size;
    private final List<TermsAggregationParam> children;

    public TermsAggregationParam() {
        fieldType = TermsAggregationParamFieldType.system;
        size = 50;
        children = new ArrayList<>();
    }

    public TermsAggregationParam(String field, String name) {
        this();
        this.field = field;
        this.name = name;
    }

    public TermsAggregationParam(String field, String name, TermsAggregationParamFieldType fieldType) {
        this(field, name);
        this.fieldType = fieldType;
    }

    public enum TermsAggregationParamFieldType {
        system,
        custom
    }
}
