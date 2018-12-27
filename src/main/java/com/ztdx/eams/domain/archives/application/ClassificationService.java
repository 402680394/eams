package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.model.Classification;
import com.ztdx.eams.domain.archives.repository.ClassificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Created by li on 2018/4/18.
 */
@Service
public class ClassificationService {

    private final ClassificationRepository classificationRepository;

    @Autowired
    public ClassificationService(ClassificationRepository classificationRepository) {
        this.classificationRepository = classificationRepository;
    }

    /**
     * 新增档案分类
     */
    @Transactional
    public void save(Classification classification) {

        //设置同级机构排序号
        Integer orderNumber = classificationRepository.findMaxOrderNumber(classification.getParentId());
        if (orderNumber != null) {
            classification.setOrderNumber(orderNumber + 1);
        } else {
            classification.setOrderNumber(1);
        }
        //存储数据
        classificationRepository.save(classification);
    }

    /**
     * 删除档案分类
     */
    @Transactional
    public void delete(int id) {
        Optional<Classification> optional = classificationRepository.findById(id);
        if(!optional.isPresent()){
            throw new InvalidArgumentException("该档案分类不存在或已被删除");
        }
        //是否存在子档案分类
        if (classificationRepository.existsByParentId(id)) {
            throw new InvalidArgumentException("该档案分类下存在子档案分类");
        }
        //删除本档案分类
        classificationRepository.deleteById(id);
    }

    /**
     * 修改档案分类
     */
    @Transactional
    public void update(Classification classification) {
        Optional<Classification> optional = classificationRepository.findById(classification.getId());
        if(!optional.isPresent()){
            throw new InvalidArgumentException("该档案分类不存在或已被删除");
        }
        classificationRepository.updateById(classification);
    }

    /**
     * 修改档案分类排序优先级
     */
    @Transactional
    public void priority(int upId, int downId) {
        Optional<Classification> up = classificationRepository.findById(upId);
        Optional<Classification> down = classificationRepository.findById(downId);
        if (!up.isPresent() || !down.isPresent()) {
            throw new InvalidArgumentException("档案分类不存在或已被删除");
        }
        if (up.get().getParentId() != down.get().getParentId()) {
            throw new InvalidArgumentException("档案分类类型或上级档案分类不一致");
        }
        classificationRepository.updateOrderNumberById(upId, down.get().getOrderNumber());
        classificationRepository.updateOrderNumberById(downId, up.get().getOrderNumber());
    }

    public Classification get(int id) {
        return classificationRepository.findById(id).orElse(null);
    }
}
