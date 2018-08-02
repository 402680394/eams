package com.ztdx.eams.domain.archives.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TermsAggregationResult {
    private String id;
    private String name;
    private int count;
    private final List<TermsAggregationResult> children;


    public TermsAggregationResult() {
        children = new ArrayList<>();
    }

    public TermsAggregationResult(String id, String name, int count) {
        this();
        this.id = id;
        this.name = name;
        this.count = count;
    }

    public TermsAggregationResult(String id, String name, int count, List<TermsAggregationResult> children) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.children = children;
    }
}
