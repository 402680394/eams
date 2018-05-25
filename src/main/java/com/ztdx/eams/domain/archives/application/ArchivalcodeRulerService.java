package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.domain.archives.model.Entry;
import com.ztdx.eams.domain.archives.model.archivalcodeRuler.ArchivalcodeRuler;
import com.ztdx.eams.domain.archives.repository.ArchivalcodeRulerRepository;
import com.ztdx.eams.domain.archives.repository.mongo.EntryMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 档号生成规则业务
 */
@Service
public class ArchivalcodeRulerService {

    private final ArchivalcodeRulerRepository archivalcodeRulerRepository;
    private final EntryMongoRepository entryMongoRepository;

    /**
     * 构造函数
     */
    @Autowired
    public ArchivalcodeRulerService(ArchivalcodeRulerRepository archivalcodeRulerRepository,EntryMongoRepository entryMongoRepository) {
        this.archivalcodeRulerRepository = archivalcodeRulerRepository;
        this.entryMongoRepository = entryMongoRepository;
    }

    /**
     * 档号生成规则
     */
    public List<String> archivalCodeRuler(List<UUID> entryIds, int catalogueId) throws Exception {

        //通过目录id查询到的规则放入规则集合
        List<ArchivalcodeRuler> archivalcodeRulers = archivalcodeRulerRepository.findByCatalogueId(catalogueId);

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
                errors.add("档号存在");
                continue;
            }

            //档号
            StringBuilder archivalCode = new StringBuilder();
            //遍历规则集合
            for (ArchivalcodeRuler archivalcodeRuler : archivalcodeRulers) {

                String str = "";
                switch (archivalcodeRuler.getType()) {
                    case EntryValue:
                        String metadataName = archivalcodeRuler.getMetametadataName();
                        String entryValue = items.get(metadataName).toString();
                        str = entryValue.substring(0, archivalcodeRuler.getInterceptionLength());
                        if (str.equals("")) {
                            errors.add(metadataName + "不能为空");
                        }
                        break;
                    case ReferenceCode:
                        String metadataName1 = archivalcodeRuler.getMetametadataName();
                        String entryValue1 = (String) Entry.class.getDeclaredMethod(metadataName1).invoke(entry, null);
                        String regex = "\\[[\\s\\S]*\\]";
                        if (entryValue1.matches(regex)) {
                            str = entryValue1.split(regex)[0];
                        }
                        if (str.equals("")) {
                            errors.add(entryValue1 + "不能为空");
                        }
                        break;
                    case FondsCode:
                        str = archivalcodeRuler.getValue();
                        if (str.equals("")) {
                            errors.add("全宗号不能为空");
                        }
                        break;
                    case FixValue:
                        str = archivalcodeRuler.getValue();
                        if (str.equals("")) {
                            errors.add("固定值不能为空");
                        }
                        break;
                }
                archivalCode.append(str);
                items.put("archival", archivalCode);
                entry.setItems(items);
            }
            entryMongoRepository.saveAll(entrys);

        }

        if (errors.size() > 0) {
            return errors;
        }
        return null;
    }
}
