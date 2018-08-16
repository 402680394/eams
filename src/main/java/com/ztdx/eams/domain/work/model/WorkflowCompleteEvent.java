package com.ztdx.eams.domain.work.model;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WorkflowCompleteEvent extends ApplicationEvent {
    private final Workflow workflow;
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param workflow 工作流处理对象
     */
    public WorkflowCompleteEvent(Object source, Workflow workflow) {
        super(source);
        this.workflow = workflow;
    }
}
