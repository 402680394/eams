package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.domain.archives.model.CatalogueType;
import com.ztdx.eams.domain.archives.model.DescriptionItemDataType;
import com.ztdx.eams.domain.archives.model.Entry;
import com.ztdx.eams.domain.archives.model.archivalCodeRuler.ArchivalCodeRuler;
import com.ztdx.eams.domain.archives.model.archivalCodeRuler.RulerType;
import com.ztdx.eams.domain.archives.repository.ArchivalCodeRulerRepository;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
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
    private final CatalogueRepository catalogueRepository;

    /**
     * 构造函数
     */
    @Autowired
    public ArchivalCodeRulerService(ArchivalCodeRulerRepository archivalcodeRulerRepository, EntryMongoRepository entryMongoRepository, CatalogueRepository catalogueRepository) {
        this.archivalcodeRulerRepository = archivalcodeRulerRepository;
        this.entryMongoRepository = entryMongoRepository;
        this.catalogueRepository = catalogueRepository;
    }

    /**
     * 生成档号
     */
    public List<String> generating(List<String> entryIds, int catalogueId){

        //创建错误信息集合
        List<String> errors = new ArrayList<>();
        //创建新条目集合存入MongoDB
        List<Entry> newEntries = new ArrayList<>();

        //得到目录类型
        CatalogueType catalogueType = CatalogueType.create(catalogueRepository.getOne(catalogueId).getCatalogueType());
        //判断各个类型，走不同的档号生成规则
        switch (catalogueType){
            case File: //一文一件
                newEntries = generatingFileAndFolder(entryIds,catalogueId,errors,newEntries,catalogueType);
                break;
            case Folder: //案卷
                newEntries = generatingFileAndFolder(entryIds,catalogueId,errors,newEntries,catalogueType);
                break;
            case FolderFile: //卷内
                generatingFolderFileArchivalCode(entryIds,catalogueId,newEntries);
                break;
        }

        //把条目集合存入MongoDB
        if (newEntries.size() > 0){
            entryMongoRepository.saveAll(newEntries);
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
            //如果档号不存在，则跳过
            if (items.get("archivalCode") == null && items.get("archivalCode").equals("")) {
                continue;
            }

            //档号置空
            items.put("archivalCode", "");
            entry.setItems(items);
            newEntries.add(entry);

        }

        //存入MongoDB
        if (newEntries.size() > 0){
            entryMongoRepository.saveAll(newEntries);
        }

    }


    /**
     * 案卷和一文一件的档号生成
     */
    private List<Entry> generatingFileAndFolder(List<String> entryIds, int catalogueId,List<String> errors,List<Entry> newEntries,CatalogueType catalogueType){

        //定义档号
        StringBuilder archivalCode = new StringBuilder();
        //拼接内容
        String splicingContent="";
        //定义序号
        String serialNumber = "";



        //通过目录id查询到的规则放入规则集合
        List<ArchivalCodeRuler> archivalCodeRulers = archivalcodeRulerRepository.findByCatalogueIdOrderByOrderNumber(catalogueId);
        //如果规则集合为空，则抛异常
        if (archivalCodeRulers.size() == 0) {
            throw new BusinessException("该目录未设置档号生成规则");
        }

        //新建分组集合(著录项)
        List<String> groupList = new ArrayList<>();
        for (ArchivalCodeRuler r:archivalCodeRulers) {
            if (r.getIsGroup()==1){
                groupList.add(archivalcodeRulerRepository.findByIsGroup(r.getIsGroup()));
            }
        }

        //查找条目，要传入条目id和目录id
        Iterable<Entry> entryList = entryMongoRepository.findAllById(entryIds, "archive_record_" + catalogueId);

        //遍历条目集合
        for (Entry entry : entryList) {

            //取条目中的著录项集合
            Map<String, Object> items = entry.getItems();
            //如果档号已经存在，则返回错误信息
            if (items.get("archivalCode") != null && !items.get("archivalCode").equals("")) {
                errors.add("档号已存在");
                continue;
            }

            /*//通过案卷条目id和目录id获取卷内条目集合
            List<Entry> folderFileEntryList = entryMongoRepository.findById(entry.getId(),"archive_record_" + catalogueId);
            //获取卷内条目集合
            for (Entry folderFileEntry : folderFileEntryList) {
                Map<String,Object> folderFileItems = folderFileEntry.getItems();
                //如果档号已经存在，则执行下一条
                if (folderFileItems.get("archivalCode") != null && !folderFileItems.get("archivalCode").equals("")) {
                    continue;
                }
            }*/

            //遍历规则集合
            for (ArchivalCodeRuler archivalCodeRuler : archivalCodeRulers) {

                //如果规则集合有序号
                if (archivalCodeRuler.getType()==RulerType.SerialNumber){
                    //如果条目中有序号
                    if(items.get("serialNumber")!= null && !items.get("serialNumber").equals("")){
                        //直接档号生成规则
                        splicingContent = archivalCodeRuler(archivalCodeRuler,items,errors,entry);
                    }else{  //如果条目中没有序号
                        //生成序号
                        serialNumber = generatingSerialNumber(groupList,items);
                        //生成档号（多加了序号）
                        splicingContent = archivalCodeRuler(archivalCodeRuler,items,errors,entry)+serialNumber;
                    }
                }else{//如果规则没有序号
                    //直接档号生成规则
                    splicingContent = archivalCodeRuler(archivalCodeRuler,items,errors,entry);

                }

                //生成卷内档号
                //folderAndFolderFileArchivalCode(folderFileEntryList,errors,splicingContent,newEntries);

                archivalCode.append(splicingContent);
                items.put("archivalCode", archivalCode.toString());
                entry.setItems(items);
                newEntries.add(entry);

            }

        }

        return newEntries;

    }

    /**
     * 卷内档号生成
     */
    private List<Entry> generatingFolderFileArchivalCode (List<String> entryIds, int catalogueId,List<Entry> newEntries){

        //创建错误信息集合
        List<String> errors = new ArrayList<>();

        //1.获取所属案卷档号
        String folderArchivalCode = "cc";

        //通过卷内id和目录id获得卷内条目
        Iterable<Entry> entryList = entryMongoRepository.findAllById(entryIds, "archive_record_" + catalogueId);

        //生成档号
        newEntries = folderAndFolderFileArchivalCode(entryList,errors,folderArchivalCode,newEntries);

        return newEntries;

    }

    /**
     * 案卷及卷内档号生成相同部分
     */
    private List<Entry>  folderAndFolderFileArchivalCode(Iterable<Entry> folderFileEntryList, List<String> errors,String folderArchivalCode,List<Entry> newEntries){

        //定义档号
        String archivalCode = "";
        //定义卷内顺序号
        Integer serialNumber = -1;

        for (Entry entry : folderFileEntryList) {

            //定义条目
            Map<String,Object> items = entry.getItems();
            //如果档号已经存在，则执行下一条
            if (items.get("archivalCode") != null && !items.get("archivalCode").equals("")) {
                errors.add("档号已存在");
                continue;
            }

            serialNumber = (Integer) items.get("serialNumber");

            //如果有卷内顺序号
            if(serialNumber != null && !serialNumber.equals("")){
                //直接生成卷内档号 (案卷档号+卷内顺序号)
                archivalCode = folderArchivalCode + serialNumber;
            }else {
                //生成卷内顺序号
                //调用仓储层得到最大序号
                String maxSerialNumber = "1";
                //得到顺序号
                serialNumber = Integer.parseInt(generatingAllTypeSerialNumber(maxSerialNumber,serialNumber,items));
                //生成档号
                archivalCode = folderArchivalCode + serialNumber;
            }

            //存入MongoDB
            items.put("archivalCode", archivalCode);
            entry.setItems(items);
            newEntries.add(entry);

        }
        return newEntries;

    }


    /**
     * 生成档号规则
     */
    private String archivalCodeRuler (ArchivalCodeRuler archivalCodeRuler,Map<String, Object> items,List<String> errors,Entry entry){

        //拼接内容
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
                    String referenceCode = entryValue1.split(regex)[0];
                    str = referenceCode.substring(0, archivalCodeRuler.getInterceptionLength());
                }
                if (str.equals("")) {
                    errors.add(entryValue1 + "不能为空");
                }
                break;
            case FondsCode:
                str = entry.getFondsId()+"";
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
            case SerialNumber:
                str = archivalCodeRuler.getValue();
                if (str.equals("")) {
                    errors.add("序号不能为空");
                }
                break;
        }
        return str;
    }

    /**
     *生成案卷及一文一件序号
     */
    private String generatingSerialNumber(List<String> groupList,Map<String, Object> items){

        //著录项值
        String value = "";
        //序号
        Integer serialNumber = -1;

        //where后语句的查询条件
        StringBuilder queryCondition = new StringBuilder();

        //遍历分组集合
        for (String entryValue : groupList) {

            //根据著录项去items取对应的值
            value = (String)items.get(entryValue);

            //开始拼接
            queryCondition.append(entryValue + "=\"" + value +"\""+" " + "and");

        }
        queryCondition.toString().substring(0,queryCondition.lastIndexOf("and"));

        //调用仓储层得到最大序号
        String maxSerialNumber = "1";

        //返回序号
        return generatingAllTypeSerialNumber(maxSerialNumber,serialNumber,items);

    }

    private String generatingAllTypeSerialNumber(String maxSerialNumber,Integer serialNumber,Map<String, Object> items){
        //如果这一组分类中要生成第一个序号  (也就是最大序号为空)
        if (maxSerialNumber.equals("")){
            serialNumber = 1;
        }else {//如果有则执行以下的
            //得到序号
            serialNumber = Integer.parseInt(maxSerialNumber);
            serialNumber++;
        }

        //调用仓储层得到序号长度
        Integer i = 4;
        //补零  (%表示结果为字面值 0 代表前面补充0 4 代表长度为4 d 代表参数为正数型)
        String newSerialNumber = String.format("%0"+i+"d", serialNumber);

        //放入条目
        items.put("serialNumber",newSerialNumber);

        return newSerialNumber;
    }

}
