package com.ztdx.eams.domain.archives.application.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ArchivesGroupDeleteEvent extends ApplicationEvent {

    private int fondsId;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public ArchivesGroupDeleteEvent(Object source, int fondsId) {
        super(source);
        this.fondsId = fondsId;
    }
}