package com.ztdx.eams.controller.work;

import com.ztdx.eams.domain.work.model.Workflow;
import com.ztdx.eams.domain.work.model.WorkflowCompleteEvent;
import org.apache.commons.lang.StringUtils;
import org.flowable.common.engine.api.delegate.event.AbstractFlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.variable.api.persistence.entity.VariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.flowable.common.engine.api.delegate.event.FlowableEngineEventType.PROCESS_COMPLETED;

@Service
public class WorkflowListener extends AbstractFlowableEventListener{

    private final RuntimeService runtimeService;

    private final ApplicationContext applicationContext;

    @Autowired
    public WorkflowListener(RuntimeService runtimeService, ApplicationContext applicationContext) {
        this.runtimeService = runtimeService;
        this.applicationContext = applicationContext;
        runtimeService.addEventListener(this);
    }

    @Override
    public void onEvent(FlowableEvent rawEvent) {
        if (rawEvent.getType() != PROCESS_COMPLETED){
            return;
        }
        FlowableEntityEvent event = (FlowableEntityEvent)rawEvent;
        if (event.getType() == PROCESS_COMPLETED && event.getEntity() instanceof ExecutionEntity){
            ExecutionEntity processInstance = (ExecutionEntity)event.getEntity();
            Map<String, VariableInstance> vars = processInstance.getVariableInstances();

            if (vars.get("result") == null){
                return;
            }

            Workflow workflow = new Workflow();
            workflow.setApplicantId(getIntegerValue(vars, "applicantId"));
            workflow.setOid(getStringValue(vars, "id"));
            workflow.setOrderCode(getStringValue(vars, "orderCode"));
            workflow.setOrderId(getStringValue(vars, "orderId"));
            workflow.setTitle(getStringValue(vars, "title"));

            String result = getStringValue(vars, "result");
            if (StringUtils.isEmpty(result)){
                workflow.setStatus(Workflow.WorkflowResult.pending);
                workflow.setResult(Workflow.WorkflowResult.pending);
            }else {
                workflow.setStatus(Workflow.WorkflowResult.valueOf(result));
                workflow.setResult(Workflow.WorkflowResult.valueOf(result));

                processInstance.setVariable("status", result);
            }

            String type = getStringValue(vars, "type");
            if (StringUtils.isEmpty(type)){
                workflow.setType(null);
            }else {
                workflow.setType(Workflow.WorkflowType.valueOf(type));
            }

            applicationContext.publishEvent(new WorkflowCompleteEvent(this, workflow));
        }
    }

    private Integer getIntegerValue(Map<String, VariableInstance> vars , String name){
        if(vars.get(name) != null && StringUtils.isNumeric(vars.get(name).getValue().toString())) {
            return Integer.parseInt(vars.get(name).getValue().toString());
        }else{
            return null;
        }
    }

    private String getStringValue(Map<String, VariableInstance> vars , String name){
        if(vars.get(name) != null) {
            return vars.get(name).getValue().toString();
        }else{
            return null;
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
