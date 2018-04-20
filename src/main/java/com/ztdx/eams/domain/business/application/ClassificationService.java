package com.ztdx.eams.domain.business.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.business.model.Classification;
import com.ztdx.eams.domain.business.repository.ClassificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

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
        if (orderNumber!=null){
            classification.setOrderNumber(orderNumber + 1);
        }else{
            classification.setOrderNumber(1);
        }
        //设置创建时间
        classification.setGmtCreate(Calendar.getInstance().getTime());
        //存储数据
        classificationRepository.save(classification);
    }

    /**
     * 删除档案分类
     */
    @Transactional
    public void delete(int id) {
        if(classificationRepository.existsById(id)){
            //删除子档案分类
            List<Classification> list = classificationRepository.findAllByParentId(id);
            if (!list.isEmpty()) {
                for (Classification c : list) {
                    delete(c.getId());
                }
            }
            //删除本档案分类
            classificationRepository.deleteById(id);
        }
    }

    /**
     * 修改档案分类
     */
    @Transactional
    public void update(Classification classification) {
        //修改数据
        classificationRepository.updateById(classification);
    }

    /**
     * 修改档案分类排序优先级
     */
    @Transactional
    public void priority(int upId, int downId) {
        Classification up=classificationRepository.findById(upId);
        Classification down=classificationRepository.findById(downId);

        if(up.getParentId()!=down.getParentId()){
            throw new InvalidArgumentException("全宗类型或上级全宗不一致");
        }
        classificationRepository.updateOrderNumberById(upId,down.getOrderNumber());
        classificationRepository.updateOrderNumberById(downId,up.getOrderNumber());
    }
}
