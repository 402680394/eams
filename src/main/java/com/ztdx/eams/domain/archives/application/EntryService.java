package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.domain.archives.model.Entry;
import com.ztdx.eams.domain.archives.repository.elasticsearch.EntryElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.mongo.EntryMongoRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class EntryService {

    private EntryElasticsearchRepository entryElasticsearchRepository;

    private EntryMongoRepository entryMongoRepository;

    public EntryService(EntryElasticsearchRepository entryElasticsearchRepository, EntryMongoRepository entryMongoRepository) {
        this.entryElasticsearchRepository = entryElasticsearchRepository;
        this.entryMongoRepository = entryMongoRepository;
    }

    public Entry save(Entry entry){
        //TODO 缺少档案目录id的判断
        //TODO 缺少条目数据的验证
        entry.setId(UUID.randomUUID());
        entry.setGmtCreate(new Date());
        entryMongoRepository.save(entry);
        return entryElasticsearchRepository.save(entry);
    }


}
