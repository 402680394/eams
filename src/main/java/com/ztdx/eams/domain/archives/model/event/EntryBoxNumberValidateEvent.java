package com.ztdx.eams.domain.archives.model.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EntryBoxNumberValidateEvent extends ApplicationEvent {

    private int archiveId;
    private String boxCode;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public EntryBoxNumberValidateEvent(Object source, int archiveId, String boxCode) {
        super(source);
        this.archiveId = archiveId;
        this.boxCode = boxCode;
    }
}
