package com.ztdx.eams.domain.business.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.business.model.DictionaryClassification;
import com.ztdx.eams.domain.business.repository.DictionaryClassificationRepository;
import com.ztdx.eams.domain.business.repository.DictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

/**
 * Created by li on 2018/4/18.
 */
@Service
public class DictionaryClassificationService {

    private final DictionaryClassificationRepository dictionaryClassificationRepository;

    private final DictionaryRepository dictionaryRepository;

    @Autowired
    public DictionaryClassificationService(DictionaryClassificationRepository dictionaryClassificationRepository, DictionaryRepository dictionaryRepository) {
        this.dictionaryClassificationRepository = dictionaryClassificationRepository;
        this.dictionaryRepository = dictionaryRepository;
    }
    /**
     * 新增词典分类
     */
    @Transactional
    public void save(DictionaryClassification dictionaryClassification) {
        if (dictionaryClassificationRepository.existsByCode(dictionaryClassification.getCode())) {
            throw new InvalidArgumentException("词典分类编码已存在");
        }
        //存储数据
        dictionaryClassificationRepository.save(dictionaryClassification);
    }
    /**
     * 删除词典分类
     */
    @Transactional
    public void delete(int id) {
        if (dictionaryClassificationRepository.existsById(id)) {
            //删除词典分类下词典数据
            dictionaryRepository.deleteByClassificationId(id);
            //删除词典分类
            dictionaryClassificationRepository.deleteById(id);
        }
    }
    /**
     * 修改词典分类
     */
    @Transactional
    public void update(DictionaryClassification dictionaryClassification) {
        if (!dictionaryClassificationRepository.existsByCodeAndId(dictionaryClassification.getCode(), dictionaryClassification.getId())) {
            if (dictionaryClassificationRepository.existsByCode(dictionaryClassification.getCode())) {
                throw new InvalidArgumentException("词典分类编码已存在");
            }
        }
        //修改数据
        if (dictionaryClassificationRepository.existsById(dictionaryClassification.getId())) {
            dictionaryClassificationRepository.updateById(dictionaryClassification);
        }
    }
}
