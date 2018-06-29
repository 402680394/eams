package com.ztdx.eams.domain.archives.repository.mongo;

public interface IdGeneratorValue {
    /**
     * %d:目录id %s:字段名
     */
    String ENTRY_ITEM_INCREMENT_FORMAT = "archive_entry_%d_%s";

    Long get(String id);
    Long get(String id, int inc);
}
