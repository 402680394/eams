package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.model.Dictionary;
import com.ztdx.eams.domain.archives.repository.DictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by li on 2018/4/18.
 */
@Service
public class DictionaryService {

    private final DictionaryRepository dictionaryRepository;

    @Autowired
    public DictionaryService(DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
    }

    /**
     * 新增词典
     */
    @Transactional
    public void save(Dictionary dictionary) {
        if (dictionaryRepository.existsByCode(dictionary.getCode())) {
            throw new InvalidArgumentException("词典编码已存在");
        }
        //设置排序优先级
        Integer orderNumber = dictionaryRepository.findMaxOrderNumber(dictionary.getClassificationId());
        if (orderNumber != null) {
            dictionary.setOrderNumber(orderNumber + 1);
        } else {
            dictionary.setOrderNumber(1);
        }
        //存储数据
        dictionaryRepository.save(dictionary);
    }
    /**
     * 删除词典
     */
    @Transactional
    public void delete(int id) {
        if (dictionaryRepository.existsById(id)) {
            //删除词典
            dictionaryRepository.deleteById(id);
        }
    }
    /**
     * 修改词典
     */
    @Transactional
    public void update(Dictionary dictionary) {
        if (!dictionaryRepository.existsByCodeAndId(dictionary.getCode(), dictionary.getId())) {
            if (dictionaryRepository.existsByCode(dictionary.getCode())) {
                throw new InvalidArgumentException("词典编码已存在");
            }
        }
        //修改数据
        if (dictionaryRepository.existsById(dictionary.getId())) {
            dictionaryRepository.updateById(dictionary);
        }
    }
    /**
     * 修改词典排序优先级
     */
    @Transactional
    public void priority(int upId, int downId) {

        Dictionary up=dictionaryRepository.findById(upId);
        Dictionary down=dictionaryRepository.findById(downId);
        if(up==null||down==null){
            throw new InvalidArgumentException("词典不存在或已被删除");
        }
        dictionaryRepository.updateOrderNumberById(upId,down.getOrderNumber());
        dictionaryRepository.updateOrderNumberById(downId,up.getOrderNumber());
    }

}
