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
    public List<String> generating(List<UUID> entryIds, int catalogueId){

        //通过目录id查询到的规则放入规则集合
        List<ArchivalCodeRuler> archivalCodeRulers = archivalcodeRulerRepository.findByCatalogueIdOrderByOrderNumber(catalogueId);

        //查找条目，要传入条目id和目录id
        Iterable<Entry> entrys = entryMongoRepository.findAllById(entryIds, "archive_record_" + catalogueId);

        //新建错误信息集合
        List<String> errors = new ArrayList<>();

        //遍历条目集合
        for (Entry entry : entrys) {

            //取条目中的著录项集合
            Map<String, Object> items = entry.getItems();

            //如果档号已经存在，则返回错误信息
            if (items.get("archival") != null) {
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
        }
        entryMongoRepository.saveAll(entrys);
        if (errors.size() > 0) {
            return errors;
        }
        return null;
    }

    /**
     * 清除档号
     */
    public void clear(List<UUID> entryIds, int catalogueId){

        //查找条目，要传入条目id和目录id
        Iterable<Entry> entrys = entryMongoRepository.findAllById(entryIds, "archive_record_" + catalogueId);
        List<Entry> entries = new ArrayList<>();
        //遍历条目集合
        for (Entry entry : entrys) {

            //取条目中的著录项集合
            Map<String, Object> items = entry.getItems();

            //如果档号已经存在，则返回错误信息
            if (items.get("archival") == null||items.get("archival").equals("")) {
                continue;
            }

            //档号置空
            items.put("archival", "");
            entry.setItems(items);
            entries.add(entry);
        }
        entryMongoRepository.saveAll(entries);
    }
}
