package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.application.task.EntryAsyncTask;
import com.ztdx.eams.domain.archives.model.Entry;
import com.ztdx.eams.domain.archives.model.archivalCodeRuler.ArchivalCodeRuler;
import com.ztdx.eams.domain.archives.model.archivalCodeRuler.GeneratingBusiness;
import com.ztdx.eams.domain.archives.model.archivalCodeRuler.RulerType;
import com.ztdx.eams.domain.archives.repository.ArchivalCodeRulerRepository;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import com.ztdx.eams.domain.archives.repository.DescriptionItemRepository;
import com.ztdx.eams.domain.archives.repository.mongo.EntryMongoRepository;
import com.ztdx.eams.domain.store.model.BoxCodeRule;
import com.ztdx.eams.domain.system.repository.FondsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 档号生成规则业务
 */
@Service
public class ArchivalCodeRulerService {

    private final GeneratingBusiness generatingBusiness;
    private final EntryMongoRepository entryMongoRepository;
    private final EntryAsyncTask entryAsyncTask;

    private final ArchivalCodeRulerRepository archivalCodeRulerRepository;

    /**
     * 构造函数
     */
    @Autowired
    public ArchivalCodeRulerService(GeneratingBusiness generatingBusiness, EntryMongoRepository entryMongoRepository, EntryAsyncTask entryAsyncTask, ArchivalCodeRulerRepository archivalCodeRulerRepository) {
        this.entryAsyncTask = entryAsyncTask;
        this.archivalCodeRulerRepository = archivalCodeRulerRepository;
        this.generatingBusiness = generatingBusiness;
        this.entryMongoRepository = entryMongoRepository;
    }

    /**
     * 生成一文一件及案卷档号
     *
     * @param entryIds    条目id集合
     * @param catalogueId 目录id
     * @return 返回错误明细
     */
    public List<Map<String, String>> generatingFileAndFolder(List<String> entryIds, int catalogueId) {
        if (entryIds == null) {
            throw new BusinessException("未选中条目");
        }
        return generatingBusiness.generatingFileAndFolder(entryIds, catalogueId);
    }

    /**
     * 生成卷内档号
     *
     * @param folderId    案卷id
     * @param catalogueId 目录id
     * @return
     */
    public List<Map<String, String>> generatingFolderFile(String folderId, int catalogueId) {
        if (folderId == null) {
            throw new BusinessException("未选中条目");
        }
        return generatingBusiness.generatingFolderFile(folderId, catalogueId);
    }

    /**
     * 清除档号
     *
     * @param entryIds    条目id集合
     * @param catalogueId 目录id
     */
    public void clear(List<String> entryIds, int catalogueId) {

        //查找条目，要传入条目id和目录id
        Iterable<Entry> entries = entryMongoRepository.findAllById(entryIds, "archive_record_" + catalogueId);
        //创建新条目集合存入MongoDB
        List<Entry> newEntries = new ArrayList<>();
        String name = generatingBusiness.getArchivalCodeMetadataName(catalogueId);

        if (name == null) {
            throw new BusinessException(generatingBusiness.getCatalogueType(catalogueId) + "找不到档号列");
        }

        //遍历条目集合
        for (Entry entry : entries) {

            //取条目中的著录项集合
            Map<String, Object> items = entry.getItems();
            //如果档号不存在，则跳过
            if (items.get(name) == null || items.get(name).equals("")) {
                continue;
            }

            //档号置空
            items.put(name, "");
            entry.setItems(items);
            newEntries.add(entry);

        }

        //存入MongoDB
        if (newEntries.size() > 0) {
            entryMongoRepository.saveAll(newEntries);
            entryAsyncTask.indexAll(newEntries, catalogueId);
        }

    }

    @Transactional
    public void priority(int upId, int downId) {
        Optional<ArchivalCodeRuler> up = archivalCodeRulerRepository.findById(upId);
        Optional<ArchivalCodeRuler> down = archivalCodeRulerRepository.findById(downId);
        if (!up.isPresent() || !down.isPresent()) {
            throw new InvalidArgumentException("数据不存在或已被删除");
        }
        archivalCodeRulerRepository.updateOrderNumberById(upId, down.get().getOrderNumber());
        archivalCodeRulerRepository.updateOrderNumberById(downId, up.get().getOrderNumber());
    }

    @Transactional
    public void delete(int id) {
        Optional<ArchivalCodeRuler> optional = archivalCodeRulerRepository.findById(id);
        if (!optional.isPresent()) {
            throw new InvalidArgumentException("数据不存在或已被删除");
        }
        archivalCodeRulerRepository.deleteById(id);
    }

    @Transactional
    public void update(ArchivalCodeRuler archivalCodeRuler) {
        Optional<ArchivalCodeRuler> optional = archivalCodeRulerRepository.findById(archivalCodeRuler.getId());
        if (!optional.isPresent()) {
            throw new InvalidArgumentException("数据不存在或已被删除");
        }
        archivalCodeRulerRepository.updateById(archivalCodeRuler);
    }

    @Transactional
    public void save(ArchivalCodeRuler archivalCodeRuler) {

        ArchivalCodeRuler a = archivalCodeRulerRepository.findByCatalogueIdAndType(archivalCodeRuler.getCatalogueId(), RulerType.SerialNumber).orElse(null);
        //只能有一个流水号
        if (null != a && archivalCodeRuler.getType().equals(RulerType.SerialNumber)) {
            throw new BusinessException("只能有一个流水号");
        }
        //设置排序号
        Integer orderNumber = archivalCodeRulerRepository.findMaxOrderNumber(archivalCodeRuler.getCatalogueId());
        if (orderNumber != null) {
            archivalCodeRuler.setOrderNumber(orderNumber + 1);
        } else {
            archivalCodeRuler.setOrderNumber(1);
        }
        archivalCodeRulerRepository.save(archivalCodeRuler);

        //流水号只能位于最后
        if (null != a) {
            orderNumber++;
            a.setOrderNumber(orderNumber);
            archivalCodeRulerRepository.save(a);
        }
    }

    public boolean existsByDescriptionItemId(int descriptionItemId){
        return archivalCodeRulerRepository.existsByDescriptionItemId(descriptionItemId);
    }
}
