package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.domain.archives.model.condition.EntryCondition;
import com.ztdx.eams.domain.archives.repository.mongo.ConditionMongoRepository;
import com.ztdx.eams.domain.store.model.MonitoringRecord;
import com.ztdx.eams.domain.store.repository.MonitoringRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
    @Transactional
    public void save(EntryCondition condition) {
        conditionMongoRepository.save(condition);
    }


    /**
     * 修改档案库查询条件
     */
    @Transactional
    public void update(EntryCondition condition) {
        /*if (entry.getCatalogueId() == 0){
            throw new InvalidArgumentException("目录id不存在");
        }

        if (StringUtils.isEmpty(entry.getId())){
            throw new InvalidArgumentException("id字段不存在");
        }

        Optional<Entry> find = entryMongoRepository.findById(entry.getId(), getIndexName(entry.getCatalogueId()));
        if (!find.isPresent()) {
            return save(entry);
        }
        Entry update = find.get();
        update.setItems(entry.getItems());
        update.setGmtModified(new Date());
        update.setVersion(entry.getVersion());
        this.convertEntryItems(entry, EntryItemConverter::from);
        update = entryMongoRepository.save(update);

        index(update);
        return update;*/
    }


}
