package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.domain.archives.model.Entry;
import com.ztdx.eams.domain.archives.model.archivalCodeRuler.ArchivalCodeRuler;
import com.ztdx.eams.domain.archives.repository.ArchivalCodeRulerRepository;
import com.ztdx.eams.domain.archives.repository.mongo.EntryMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 档号生成规则业务
 */
@Service
public class ArchivalCodeRulerService {

    private final ArchivalCodeRulerRepository archivalcodeRulerRepository;
    private final EntryMongoRepository entryMongoRepository;

    /**
     * 构造函数
     */
    @Autowired
    public ArchivalCodeRulerService(ArchivalCodeRulerRepository archivalcodeRulerRepository, EntryMongoRepository entryMongoRepository) {
        this.archivalcodeRulerRepository = archivalcodeRulerRepository;
        this.entryMongoRepository = entryMongoRepository;
    }

    /**
     * 生成档号
     */
    public List<String> generating(List<String> entryIds, int catalogueId){

        UUID uuid1 = UUID.fromString("f04c094a-d1b4-3e62-02c4-e6d47da73aa2");
        UUID uuid2 = UUID.fromString("0b4fbf8a-6a7b-51b9-8039-52f61a37aa92");
        List<UUID> uuids =new ArrayList<>();
        uuids.add(uuid1);
        uuids.add(uuid2);
        //创建错误信息集合
        List<String> errors = new ArrayList<>();

        //通过目录id查询到的规则放入规则集合
        List<ArchivalCodeRuler> archivalCodeRulers = archivalcodeRulerRepository.findByCatalogueIdOrderByOrderNumber(catalogueId);

        if(archivalCodeRulers.size()==0){
            throw new BusinessException("该目录未设置档号生成规则");
        }

        //查找条目，要传入条目id和目录id
        Iterable<Entry> entries = entryMongoRepository.findAllById(uuids, "archive_record_" + catalogueId);

        //创建新条目集合存入MongoDB
        List<Entry> newEntries = new ArrayList<>();

        //遍历条目集合
        for (Entry entry : entries) {

            //取条目中的著录项集合
            Map<String, Object> items = entry.getItems();
            //如果档号已经存在，则返回错误信息
            if (items.get("archival") != null || !items.get("archival").equals("")) {
                errors.add("档号已存在");
                continue;
            }

            //档号
            StringBuilder archivalCode = new StringBuilder();
            //遍历规则集合
            for (ArchivalCodeRuler archivalCodeRuler : archivalCodeRulers) {

                String str = "";
                switch (archivalCodeRuler.getType()) {
                    case EntryValue:
                        String metadataName = archivalCodeRuler.getMetadataName();
                        String entryValue = items.get(metadataName).toString();
                        str = entryValue.substring(0, archivalCodeRuler.getInterceptionLength());
                        if (str.equals("")) {
                            errors.add(metadataName + "不能为空");
                        }
                        break;
                    case ReferenceCode:
                        String metadataName1 = archivalCodeRuler.getMetadataName();
                        String entryValue1 = items.get(metadataName1).toString();
                        String regex = "\\[[\\s\\S]*\\]";
                        if (entryValue1.matches(regex)) {
                            str = entryValue1.split(regex)[0];
                        }
                        if (str.equals("")) {
                            errors.add(entryValue1 + "不能为空");
                        }
                        break;
                    case FondsCode:
                        str = archivalCodeRuler.getValue();
                        if (str.equals("")) {
                            errors.add("全宗号不能为空");
                        }
                        break;
                    case FixValue:
                        str = archivalCodeRuler.getValue();
                        if (str.equals("")) {
                            errors.add("固定值不能为空");
                        }
                        break;
                }
                archivalCode.append(str);
            }
            items.put("archival", archivalCode);
            entry.setItems(items);
            newEntries.add(entry);
        }
        //把条目集合存入MongoDB
        if (newEntries.size() > 0){
            entryMongoRepository.saveAll(entries);
        }

        //返回错误信息集合
        if (errors.size() > 0) {
            return errors;
        }
        return null;
    }

    /**
     * 清除档号
     */
    public void clear(List<String> entryIds, int catalogueId){



        //查找条目，要传入条目id和目录id
        Iterable<Entry> entries = entryMongoRepository.findAllById(entryIds, "archive_record_" + catalogueId);
        //创建新条目集合存入MongoDB
        List<Entry> newEntries = new ArrayList<>();
        //遍历条目集合
        for (Entry entry : entries) {

            //取条目中的著录项集合
            Map<String, Object> items = entry.getItems();
            //如果档号已经存在，则返回错误信息
            if (items.get("archival") == null||items.get("archival").equals("")) {
                continue;
            }

            //档号置空
            items.put("archival", "");
            entry.setItems(items);
            newEntries.add(entry);

        }

        //存入MongoDB
        if (newEntries.size() > 0){
            entryMongoRepository.saveAll(newEntries);
        }


    }
}
