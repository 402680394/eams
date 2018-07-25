package com.ztdx.eams.domain.store.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.store.model.Shelf;
import com.ztdx.eams.domain.store.model.ShelfCell;
import com.ztdx.eams.domain.store.model.Storage;
import com.ztdx.eams.domain.store.model.event.ShelfCellDeletedEvent;
import com.ztdx.eams.domain.store.repository.BoxRepository;
import com.ztdx.eams.domain.store.repository.ShelfCellRepository;
import com.ztdx.eams.domain.store.repository.ShelfRepository;
import com.ztdx.eams.domain.store.repository.StorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
public class StorageService {

    private final StorageRepository storageRepository;

    private final ShelfRepository shelfRepository;

    private final ShelfCellRepository shelfCellRepository;

    private final BoxRepository boxRepository;

    /**
     * 构造函数
     */
    @Autowired
    public StorageService(StorageRepository storageRepository, ShelfRepository shelfRepository, ShelfCellRepository shelfCellRepository, BoxRepository boxRepository) {
        this.storageRepository = storageRepository;
        this.shelfRepository = shelfRepository;
        this.shelfCellRepository = shelfCellRepository;
        this.boxRepository = boxRepository;
    }

    /**
     * 新增库房
     */
    @Transactional
    public void save(Storage storage) {

        if (storageRepository.existsByNumberAndGmtDeleted(storage.getNumber(), 0)) {
            throw new InvalidArgumentException("库房编号已经存在");
        }
        storageRepository.save(storage);

    }

    /**
     * 修改库房
     */
    @Transactional
    public void update(Storage storage) {

        Storage oldStorage = storageRepository.findById(storage.getId()).orElse(null);
        if (oldStorage.getNumber().equals(storage.getNumber()) || !storageRepository.existsByNumberAndGmtDeleted(storage.getNumber(), 0)) {
            if (storageRepository.existsById(storage.getId())) {
                storageRepository.updateById(storage);
            }
        } else {
            throw new InvalidArgumentException("库房编号已经存在");
        }

    }

    /**
     * 删除库房
     */
    @Transactional
    public void delete(List<Integer> storageIds) {
        //删除库
        List<Storage> storageList = storageRepository.findAllById(storageIds);

        storageList.forEach(a -> {
            a.setGmtDeleted(1);
        });
        storageRepository.saveAll(storageList);
        //删除密集架
        List<Integer> shelfIds = new ArrayList<>();

        List<Shelf> shelfList = shelfRepository.findByStorageIdIn(storageIds);
        shelfList.forEach(a -> {
            shelfIds.add(a.getId());
            a.setGmtDeleted(1);
        });

        shelfRepository.saveAll(shelfList);
        //下架盒
        List<String> cellPointCodes = new ArrayList<>();

        shelfCellRepository.findByShelfIdIn(shelfIds).forEach(a -> cellPointCodes.add(a.getPointCode()));

        cellPointCodes.forEach(point -> {
            boxRepository.updateOnFrameByPoint(0, "", point);
        });
    }

    public Storage get(int id) {
        return storageRepository.findById(id).orElse(null);
    }

    public boolean existsById(int id) {
        return storageRepository.existsById(id);
    }
}
