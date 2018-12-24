package com.ztdx.eams.domain.archives.application.event;

import com.ztdx.eams.domain.archives.model.DescriptionItem;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;
@Getter
public class DescriptionItemAddEvent extends ApplicationEvent {

    private List<DescriptionItem> descriptionItems;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public DescriptionItemAddEvent(Object source,  List<DescriptionItem> descriptionItems) {
        super(source);
        this.descriptionItems = descriptionItems;
    }
}
