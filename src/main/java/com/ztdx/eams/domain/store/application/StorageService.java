package com.ztdx.eams.domain.store.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.store.model.Storage;
import com.ztdx.eams.domain.store.repository.StorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
public class StorageService {

    private final StorageRepository storageRepository;

    /**
     * 构造函数
     */
    @Autowired
    public StorageService(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    /**
     * 新增库房
     */
    @Transactional
    public void save(Storage storage){

        if (storageRepository.existsByNumber(storage.getNumber())){
            throw new InvalidArgumentException("库房编号已经存在");
        }
        storageRepository.save(storage);

    }

    /**
     * 修改库房
     */
    @Transactional
    public void update(Storage storage){

        if (storageRepository.existsByNumber(storage.getNumber())){
            throw new InvalidArgumentException("库房编号已经存在");
        }
        if (storageRepository.existsById(storage.getId())) {
            storageRepository.updateById(storage);
        }

    }

    /**
     * 删除库房
     */
    @Transactional
    public void delete(List<Integer> storageIds){
        List<Storage> storageList = storageRepository.findAllById(storageIds);
        storageRepository.deleteInBatch(storageList);
    }


}
