package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.model.condition.EntryCondition;
import com.ztdx.eams.domain.archives.repository.mongo.ConditionMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
public class ConditionService {

    private final ConditionMongoRepository conditionMongoRepository;

    /**
     * 构造函数
     */
    @Autowired
    public ConditionService(ConditionMongoRepository conditionMongoRepository) {
        this.conditionMongoRepository = conditionMongoRepository;
    }

    /**
     * 增加档案库查询条件
     */
    public Object save(EntryCondition condition) {
        if (conditionMongoRepository.existsByName(condition.getName())){
            throw new BusinessException("名称已存在");
        }
        return conditionMongoRepository.save(condition);
    }

    /**
     * 修改档案库查询条件
     */
    public void update(EntryCondition condition) {

        EntryCondition entryCondition = conditionMongoRepository.findById(condition.getId()).orElse(null);
        if (entryCondition.getName().equals(entryCondition.getName()) || !conditionMongoRepository.existsByName(condition.getName())){

            Optional<EntryCondition> find = conditionMongoRepository.findById(condition.getId());
            if (!find.isPresent()) {
                save(condition);
            }

            EntryCondition update = find.get();
            update.setId(condition.getId());
            update.setName(condition.getName());
            update.setConditions(condition.getConditions());
            conditionMongoRepository.save(update);

        }else {
           throw new BusinessException("名称已存在");
       }

    }

    /**
     * 获取档案库的查询条件
     */
    public EntryCondition getEntryCondition(Integer conditionId){
        return conditionMongoRepository.findById(conditionId.toString()).orElse(null);
    }

}
