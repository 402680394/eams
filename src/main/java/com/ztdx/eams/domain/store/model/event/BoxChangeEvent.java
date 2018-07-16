package com.ztdx.eams.domain.store.model.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Collection;

@Getter
public class BoxChangeEvent extends ApplicationEvent {

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
    public BoxChangeEvent(Object source, int catalogueId, int archiveId, Collection<String> boxCodes) {
        super(source);
        this.catalogueId = catalogueId;
        this.archiveId = archiveId;
        this.boxCodes = boxCodes;
    }
}
