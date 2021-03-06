package com.ztdx.eams.domain.store.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.store.model.MonitoringPoint;
import com.ztdx.eams.domain.store.model.Storage;
import com.ztdx.eams.domain.store.repository.MonitoringPointRepository;
import com.ztdx.eams.domain.store.repository.StorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MonitoringPointService {

    private final MonitoringPointRepository monitoringPointRepository;

    private final StorageRepository storageRepository;

    /**
     * 构造函数
     */
    @Autowired
    public MonitoringPointService(MonitoringPointRepository monitoringPointRepository,StorageRepository storageRepository) {
        this.monitoringPointRepository = monitoringPointRepository;
        this.storageRepository = storageRepository;
    }

    /**
     * 新增监测点
     */
    @Transactional
    public void save(MonitoringPoint monitoringPoint) {
        if (monitoringPointRepository.existsByNumber(monitoringPoint.getNumber())){
            throw new InvalidArgumentException("监测点编号已存在");
        }
        if (monitoringPoint.getStatus() == null) {
            monitoringPoint.setStatus(1);
        }
        Storage storage = storageRepository.findById(monitoringPoint.getStorageId()).orElse(null);
        monitoringPoint.setFondsId(storage.getFondsId());
        monitoringPointRepository.save(monitoringPoint);
    }

    /**
     * 修改监测点
     */
    @Transactional
    public void update(MonitoringPoint monitoringPoint) {
        MonitoringPoint oldMonitoringPoint = monitoringPointRepository.findById(monitoringPoint.getId()).orElse(null);
        if (oldMonitoringPoint.getNumber().equals(monitoringPoint.getNumber()) || !monitoringPointRepository.existsByNumber(monitoringPoint.getNumber())){
            monitoringPointRepository.updateById(monitoringPoint);
        }else{
            throw new InvalidArgumentException("监测点编号已存在");
        }
    }

    /**
     * 删除监测点
     */
    @Transactional
    public void delete(List<Integer> monitoringPointIds) {
        List<MonitoringPoint> monitoringPointList = monitoringPointRepository.findAllById(monitoringPointIds);
        monitoringPointRepository.deleteInBatch(monitoringPointList);
    }

}
