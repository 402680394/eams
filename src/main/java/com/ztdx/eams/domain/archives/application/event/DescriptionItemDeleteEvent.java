package com.ztdx.eams.domain.archives.application.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;
@Getter
public class DescriptionItemDeleteEvent extends ApplicationEvent {

    private List<Integer> descriptionItemIds;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public DescriptionItemDeleteEvent(Object source,  List<Integer> descriptionItemIds) {
        super(source);
        this.descriptionItemIds = descriptionItemIds;
    }
}
