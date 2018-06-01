package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.controller.operationLog.LogInfo;
import com.ztdx.eams.domain.system.model.OperationLog;
import com.ztdx.eams.domain.system.repository.OperationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationLogService {

    private OperationLogRepository operationLogRepository;

    @Autowired
    public OperationLogService(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    public void add(OperationLog operationLog){
        this.operationLogRepository.insert(operationLog);
    }
}
