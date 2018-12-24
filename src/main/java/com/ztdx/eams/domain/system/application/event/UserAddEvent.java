package com.ztdx.eams.domain.system.application.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class UserAddEvent extends ApplicationEvent {

    private int userId;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public UserAddEvent(Object source, int userId) {
        super(source);
        this.userId = userId;
    }
}
