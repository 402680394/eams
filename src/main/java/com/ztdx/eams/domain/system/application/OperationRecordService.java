package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.domain.system.model.OperationRecord;
import com.ztdx.eams.domain.system.repository.OperationRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationRecordService {

    private final OperationRecordRepository operationRecordRepository;

    @Autowired
    public OperationRecordService(OperationRecordRepository operationRecordRepository) {
        this.operationRecordRepository = operationRecordRepository;
    }


    /**
     * 添加日志
     */
    public void add(OperationRecord operationRecord){

        this.operationRecordRepository.insert(operationRecord);
    }

    public OperationRecord get(String id){
       return this.operationRecordRepository.findById(id).get();
    }
}
