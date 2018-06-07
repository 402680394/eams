package com.ztdx.eams.domain.archives.application;

import com.fasterxml.jackson.databind.util.ArrayIterator;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
     * @param entryIds 条目id集合
     * @param catalogueId 目录id
     * @return 返回错误明细
     */
    public List<String> generating(List<String> entryIds, int catalogueId){

        //创建错误信息集合
        List<String> errors = new ArrayList<>();
        //创建新条目集合存入MongoDB
        List<Entry> entriesForSave = new ArrayList<>();
        //创建新卷内条目集合存入MongoDB
        List<Entry> folderFileEntriesForSave = new ArrayList<>();

        //得到目录类型
        CatalogueType catalogueType = catalogueRepository.getOne(catalogueId).getCatalogueType();

        //判断各个类型，走不同的档号生成规则
        if(catalogueType == CatalogueType.FolderFile){
            throw new BusinessException("错误的接口");
        }

        generatingFileAndFolder(entryIds,catalogueId,errors,entriesForSave,catalogueType,folderFileEntriesForSave);

        //把条目集合存入MongoDB
        if (entriesForSave.size() > 0){
            entryMongoRepository.saveAll(entriesForSave);
        }

        //把卷内条目集合存入MongoDB
        if (folderFileEntriesForSave.size() > 0){
            entryMongoRepository.saveAll(folderFileEntriesForSave);
        }

        //返回错误信息集合
        if (errors.size() > 0) {
            return errors;
        }

        return null;
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
     * @param entryIds  案卷/一文一件 id集合
     * @param catalogueId 目录id
     * @param errors 错误信息集合
     * @param entriesForSave 案卷/一文一件条目放入新集合存到MongoDb
     * @param catalogueType 目录类型
     * @param folderFileEntriesForSave 卷卷内条目放入新集合存到MongoDb
     */
    private void generatingFileAndFolder(List<String> entryIds, int catalogueId,List<String> errors,List<Entry> entriesForSave,CatalogueType catalogueType,List<Entry> folderFileEntriesForSave){

        StringBuilder archivalCodeVal = new StringBuilder(); //定义档号
        String splicingContent=""; //拼接内容
        Iterable<Entry> folderFileEntryAll = new ArrayList<>(); //创建卷内条目集合
        String archivalCodeName ="archivalCode";
        String serialNumberName="serialNumber";
        String archivalCodeNameOfFolderFile ="archivalCode";
        String serialNumberNameOfFolderFile="serialNumber";

        //通过目录id查询到的规则放入规则集合
        List<ArchivalCodeRuler> archivalCodeRulers = archivalcodeRulerRepository.findByCatalogueIdOrderByOrderNumber(catalogueId);
        //如果规则集合为空，则抛异常
        if (archivalCodeRulers.size() == 0) {
            throw new BusinessException("该目录未设置档号生成规则");
        }

        //新建 分组集合(著录项)
        List<String> groupList = archivalCodeRulers.stream().filter(item->item.getIsGroup()==1).map(ArchivalCodeRuler::getMetadataName).collect(Collectors.toList());

        //TODO @leo 获取 archivalCodeMetaDataName 和 serialNumberMetaDataName

        //查找条目，要传入条目id和目录id
        Iterable<Entry> entryList = entryMongoRepository.findAllById(entryIds, "archive_record_" + catalogueId);

        //如果是案卷则查找所有卷内条目集合
        if (catalogueType == CatalogueType.Folder){

            //通过流遍历获取案卷id
            List<String> folderIds =StreamSupport.stream(entryList.spliterator(),false).map(Entry::getId).collect(Collectors.toList());

            //通过案卷条目id和目录id获取所有卷内条目集合
            //TODO @leo 查找所有的卷内集合；
            folderFileEntryAll = entryMongoRepository.findAllById(folderIds, "archive_record_" + catalogueId);

            archivalCodeNameOfFolderFile ="archivalCode";
            serialNumberNameOfFolderFile="serialNumber";
        }

        //遍历条目集合
        for (Entry entry : entryList) {

            //取条目中的著录项集合
            Map<String, Object> items = entry.getItems();
            //如果档号已经存在，则返回错误信息
            if (items.get(archivalCodeName) != null && !items.get(archivalCodeName).equals("")) {
                errors.add("档号已存在");
                continue;
            }

            //遍历规则集合
            for (ArchivalCodeRuler archivalCodeRuler : archivalCodeRulers) {

                //如果规则集合有序号
                if (archivalCodeRuler.getType()==RulerType.SerialNumber){
                    //如果条目中没有序号
                    if(items.get(serialNumberName)== null && items.get(serialNumberName).equals("")){
                        //生成序号
                        String serialNumberVal = generatingSerialNumber(groupList,items);
                        //生成档号（多加了序号）
                        splicingContent = archivalCodeRuler(archivalCodeRuler,items,errors,entry)+serialNumberVal;
                    }
                }

                //如果规则集合没有序号或者条目中有序号都会走这一步
                splicingContent = archivalCodeRuler(archivalCodeRuler,items,errors,entry);

                //生成卷内档号
                if (folderFileEntryAll!=null) {//如果所有卷内集合不为空
                    //if (entry.getId()==folderFileEntryList.){//如果案卷的id等于卷内的案卷id
                    //通过此案卷id和目录id获取到所属卷内集合
                    //循环卷内集合
                    List<Entry> folderFileEntryList = StreamSupport.stream(folderFileEntryAll.spliterator(), false).filter(item -> item.getParentId().equals(entry.getId())).collect(Collectors.toList());
                    folderAndFolderFileArchivalCode(folderFileEntryList, splicingContent,archivalCodeNameOfFolderFile,serialNumberNameOfFolderFile, folderFileEntriesForSave);
                    //}
                }

                archivalCodeVal.append(splicingContent);
                items.put("archivalCode", archivalCodeVal.toString());
                entry.setItems(items);
                entriesForSave.add(entry);
            }
        }
    }

    /**
     * 卷内档号生成
     * @param entryIds 卷内id集合
     * @param catalogueId 目录id
     * @param newFolderAndFileEntries 卷内条目放入新集合存到MongoDb
     */
    private void generatingFolderFileArchivalCode (List<String> entryIds, int catalogueId,List<Entry> newFolderAndFileEntries){

        String archivalCodeNameOfFolderFile ="archivalCode";
        String serialNumberNameOfFolderFile="serialNumber";

        //创建错误信息集合
        List<String> errors = new ArrayList<>();

        //1.获取所属案卷档号
        String folderArchivalCode = "cc";

        //通过卷内id和目录id获得卷内条目
        Iterable<Entry> entryList = entryMongoRepository.findAllById(entryIds, "archive_record_" + catalogueId);

        //生成档号
        folderAndFolderFileArchivalCode(entryList,folderArchivalCode,archivalCodeNameOfFolderFile,serialNumberNameOfFolderFile,newFolderAndFileEntries);
    }

    /**
     * 案卷卷内档号及卷内档号生成相同部分
     * @param folderFileEntryList 卷内条目集合
     * @param folderArchivalCode 案卷档号
     * @param folderFileEntriesForSave 卷内条目放入新集合存到MongoDb
     */
    private void folderAndFolderFileArchivalCode(Iterable<Entry> folderFileEntryList,String folderArchivalCode,String archivalCodeName,String serialNumberName,List<Entry> folderFileEntriesForSave){

        //定义档号
        String archivalCode = "";
        //定义卷内顺序号
        Integer serialNumber = 0;

        //循环卷内
        for (Entry entry : folderFileEntryList) {

            //获取卷内条目
            Map<String, Object> items = entry.getItems();

            serialNumber++;
            //得到顺序号
            serialNumber = Integer.parseInt(generatingAllTypeSerialNumber(serialNumber));
            //生成档号
            archivalCode = folderArchivalCode + serialNumber;

            //存入MongoDB
            items.put(archivalCodeName, archivalCode);
            items.put(serialNumberName,serialNumber);
            entry.setItems(items);
            folderFileEntriesForSave.add(entry);
        }

    }

    /**
     * 生成档号规则
     * @param archivalCodeRuler 档号规则
     * @param items 条目
     * @param errors 错误信息
     * @param entry 条目
     * @return 返回根据档号规则获取的要拼接的内容
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
     * 生成案卷及一文一件序号
     * @param groupList 归档分组中的被选中的著录项集合
     * @param items 条目
     * @return 返回序号
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

        //如果这一组分类中要生成第一个序号  (也就是最大序号为空)
        if (maxSerialNumber.equals("")){
            serialNumber = 1;
        }else {//如果有则执行以下的
            //得到序号
            serialNumber = Integer.parseInt(maxSerialNumber);
            serialNumber++;
        }

        serialNumber = Integer.parseInt(generatingAllTypeSerialNumber(serialNumber));

        //放入条目
        items.put("serialNumber",serialNumber);

        //返回序号
        return serialNumber.toString();

    }

    /**
     * 案卷、一文一件及卷内序号的位数补零
     * @param serialNumber 序号
     * @return 补零后的序号
     */
    private String generatingAllTypeSerialNumber(Integer serialNumber){

        //调用仓储层得到序号长度
        Integer i = 4;
        //补零  (%表示结果为字面值 0 代表前面补充0 4 代表长度为4 d 代表参数为正数型)
        String newSerialNumber = String.format("%0"+i+"d", serialNumber);

        return newSerialNumber;
    }

}
