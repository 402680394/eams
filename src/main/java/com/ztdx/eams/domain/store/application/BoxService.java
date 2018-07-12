package com.ztdx.eams.domain.store.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.repository.elasticsearch.EntryElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.mongo.EntryMongoRepository;
import com.ztdx.eams.domain.store.model.Box;
import com.ztdx.eams.domain.store.repository.BoxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by li on 2018/7/10.
 */
@Service
public class BoxService {

    private final BoxRepository boxRepository;

    private final EntryElasticsearchRepository entryElasticsearchRepository;

    private final EntryMongoRepository entryMongoRepository;

    @Autowired
    public BoxService(BoxRepository boxRepository, EntryElasticsearchRepository entryElasticsearchRepository, EntryMongoRepository entryMongoRepository) {
        this.boxRepository = boxRepository;
        this.entryElasticsearchRepository = entryElasticsearchRepository;
        this.entryMongoRepository = entryMongoRepository;
    }

    /*
    *修改盒信息
    */
    public void update(Box box) {
        Box b = boxRepository.findById(box.getId()).get();

        if (!b.getCode().equals(box.getCode())) {
            if (boxRepository.existsByCode(box.getCode())) {
                throw new InvalidArgumentException("盒号已存在");
            }
        }
        if (box.getMaxPagesTotal() < b.getPagesTotal()) {
            throw new InvalidArgumentException("已有容量超出，请先拆盒");
        }
        //修改数据
        boxRepository.updateById(box);
    }

    /*
    *删除盒
    */
    public void delete(List<Integer> ids) {
        List<Box> list = boxRepository.findAllById(ids);
        boxRepository.deleteAll(list);
    }

    /*
    *上架盒
    */
    public void onFrame(String cellCode, List<Integer> ids) {
        for (int id : ids) {
            boxRepository.updateOnFrameById(id, true, cellCode);
        }
    }

    /*
    *下架盒
    */
    public void downFrame(List<Integer> ids) {
        for (int id : ids) {
            boxRepository.updateOnFrameById(id, true, "");
        }
    }
}
