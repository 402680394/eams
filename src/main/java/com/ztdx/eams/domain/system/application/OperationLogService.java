package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.domain.system.model.OperationLog;
import com.ztdx.eams.domain.system.repository.OperationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    @Autowired
    public OperationLogService(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }


    /**
     * 添加日志
     */
    public void add(OperationLog operationLog){

        this.operationLogRepository.insert(operationLog);
    }

    public OperationLog get(String id){
       return this.operationLogRepository.findById(id).get();
    }
}
