package com.ztdx.eams.domain.store.application;

import com.ztdx.eams.domain.store.model.MonitoringRecord;
import com.ztdx.eams.domain.store.repository.MonitoringRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class MonitoringRecordService {

    private final MonitoringRecordRepository monitoringRecordRepository;

    /**
     * 构造函数
     */
    @Autowired
    public MonitoringRecordService(MonitoringRecordRepository monitoringRecordRepository) {
        this.monitoringRecordRepository = monitoringRecordRepository;
    }

    /**
     * 新增监测点
     */
    @Transactional
    public void save(MonitoringRecord monitoringRecord) {
        monitoringRecordRepository.save(monitoringRecord);
    }


    /**
     * 修改监测点
     */
    @Transactional
    public void update(MonitoringRecord monitoringRecord) {
        monitoringRecordRepository.updateById(monitoringRecord);
    }

    /**
     * 删除监测记录
     */
    @Transactional
    public void delete (List<Integer> monitoringRecordIds){
        List<MonitoringRecord> monitoringRecordList = monitoringRecordRepository.findAllById(monitoringRecordIds);
        monitoringRecordRepository.deleteInBatch(monitoringRecordList);
    }

}
