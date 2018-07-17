package com.ztdx.eams.domain.store.model.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Collection;

/**
 * 盒内文件变更发出得事件
 */
@Getter
public class BoxInsideChangeEvent extends ApplicationEvent {

    private int catalogueId;
    private int archiveId;
    private Collection<String> boxCodes;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param catalogueId 目录id
     * @param archiveId 档案库id
     * @param boxCodes 盒号集合
     */
    public BoxInsideChangeEvent(Object source, int catalogueId, int archiveId, Collection<String> boxCodes) {
        super(source);
        this.catalogueId = catalogueId;
        this.archiveId = archiveId;
        this.boxCodes = boxCodes;
    }
}
