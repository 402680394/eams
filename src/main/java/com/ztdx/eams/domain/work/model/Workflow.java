package com.ztdx.eams.domain.work.model;

import lombok.Data;

import java.util.Map;

@Data
public class Workflow {

    /**
     * 对象id
     */
    private String oid;

    /**
     * 工作流类型
     */
    private WorkflowType type;

    /**
     * 标题
     */
    private String title;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 订单编号
     */
    private String orderCode;

    /**
     * 任务最近一步的处理结果
     */
    private WorkflowResult result;

    /**
     * 工作流状态
     */
    private WorkflowResult status;

    /**
     * 申请人id
     */
    private Integer applicantId;

    private Map<String, Object> rawVars;

    public enum WorkflowType{
        borrow
    }

    public enum WorkflowResult{
        pending,
        agree,
        refuse
    }
}
