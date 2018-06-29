package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.domain.archives.model.Entry;
import com.ztdx.eams.domain.archives.model.archivalCodeRuler.GeneratingBusiness;
import com.ztdx.eams.domain.archives.repository.ArchivalCodeRulerRepository;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import com.ztdx.eams.domain.archives.repository.DescriptionItemRepository;
import com.ztdx.eams.domain.archives.repository.mongo.EntryMongoRepository;
import com.ztdx.eams.domain.system.repository.FondsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 档号生成规则业务
 */
@Service
public class ArchivalCodeRulerService {

    private final GeneratingBusiness generatingBusiness;
    private final EntryMongoRepository entryMongoRepository;
    private final CatalogueRepository catalogueRepository;
    private final FondsRepository fondsRepository;
    private final EntryService entryService;
    private final DescriptionItemRepository descriptionItemRepository;

    /**
     * 构造函数
     */
    @Autowired
    public ArchivalCodeRulerService( ArchivalCodeRulerRepository archivalcodeRulerRepository, EntryMongoRepository entryMongoRepository, CatalogueRepository catalogueRepository,FondsRepository fondsRepository,EntryService entryService,DescriptionItemRepository descriptionItemRepository) {
        this.generatingBusiness = new GeneratingBusiness(archivalcodeRulerRepository,entryMongoRepository,catalogueRepository,fondsRepository,entryService,descriptionItemRepository);
        this.entryMongoRepository = entryMongoRepository;
        this.catalogueRepository = catalogueRepository;
        this.fondsRepository = fondsRepository;
        this.entryService = entryService;
        this.descriptionItemRepository = descriptionItemRepository;
    }

    /**
     * 生成一文一件及案卷档号
     * @param entryIds 条目id集合
     * @param catalogueId 目录id
     * @return 返回错误明细
     */
    public List<String> generatingFileAndFolder(List<String> entryIds, int catalogueId){
        if (entryIds==null){
            throw new BusinessException("未选中条目");
        }
        return  generatingBusiness.generatingFileAndFolder(entryIds,catalogueId);
    }

    /**
     * 生成卷内档号
     * @param folderId 案卷id
     * @param catalogueId 目录id
     * @return
     */
    public List<String> generatingFolderFile(String folderId,int catalogueId) {
        if (folderId==null){
            throw new BusinessException("未选中条目");
        }
        return generatingBusiness.generatingFolderFile(folderId,catalogueId);
    }

    /**
     * 清除档号
     * @param entryIds 条目id集合
     * @param catalogueId 目录id
     */
    public void clear(List<String> entryIds, int catalogueId){

        //查找条目，要传入条目id和目录id
        Iterable<Entry> entries = entryMongoRepository.findAllById(entryIds, "archive_record_" + catalogueId);
        //创建新条目集合存入MongoDB
        List<Entry> newEntries = new ArrayList<>();
        String name = "";
        name = generatingBusiness.getArchivalCodeMetadataName(name,catalogueId);

        if (name==null){
            throw  new BusinessException(generatingBusiness.getCatalogueType(catalogueId)+"找不到档号列");
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
        if (newEntries.size() > 0){
            entryMongoRepository.saveAll(newEntries);
            for (Entry entry : newEntries) {
                entryService.index(entry);
            }
        }

    }

}
