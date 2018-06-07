package com.ztdx.eams.controller.operationLog;

import com.ztdx.eams.basic.UserCredential;
import lombok.Data;

/**
 * 操作上下问
 */
@Data
class OperationContext {

    /**
     * 参数
     */
    private Object[] args;

    /**
     * 返回值
     */
    private Object result;

    /**
     * 用户
     */
    private UserCredential user;
}
