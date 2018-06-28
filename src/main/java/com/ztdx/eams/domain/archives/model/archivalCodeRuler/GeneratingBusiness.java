package com.ztdx.eams.domain.archives.model.archivalCodeRuler;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.domain.archives.model.Catalogue;
import com.ztdx.eams.domain.archives.model.CatalogueType;
import com.ztdx.eams.domain.archives.model.Entry;
import com.ztdx.eams.domain.archives.repository.ArchivalCodeRulerRepository;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import com.ztdx.eams.domain.archives.repository.mongo.EntryMongoRepository;
import com.ztdx.eams.domain.system.repository.FondsRepository;
import com.mongodb.DBCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 档号生成与清除
 */
public class GeneratingBusiness {

    private final ArchivalCodeRulerRepository archivalcodeRulerRepository;
    private final EntryMongoRepository entryMongoRepository;
    private final CatalogueRepository catalogueRepository;
    private final FondsRepository fondsRepository;

    public GeneratingBusiness(ArchivalCodeRulerRepository archivalcodeRulerRepository, EntryMongoRepository entryMongoRepository, CatalogueRepository catalogueRepository,FondsRepository fondsRepository) {
        this.archivalcodeRulerRepository = archivalcodeRulerRepository;
        this.entryMongoRepository = entryMongoRepository;
        this.catalogueRepository = catalogueRepository;
        this.fondsRepository = fondsRepository;
    }

    /**
     * 生成一文一件及案卷档号
     * @param entryIds 条目id集合
     * @param catalogueId 目录id
     * @return 返回错误明细
     */
    public List<String> generatingFileAndFolder(List<String> entryIds, int catalogueId){

        //创建错误信息集合
        List<String> errors = new ArrayList<>();
        //创建新条目集合存入MongoDB
        List<Entry> entriesForSave = new ArrayList<>();
        //创建新卷内条目集合存入MongoDB
        List<Entry> folderFileEntriesForSave = new ArrayList<>();

        //得到目录类型
        Catalogue catalogue = catalogueRepository.findById(catalogueId).orElse(null);
        CatalogueType catalogueType = catalogue.getCatalogueType();

        if(CatalogueType.FolderFile.equals(catalogueType)){
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
     * 生成卷内档号
     * @param folderId 案卷id
     * @param catalogueId 目录id
     * @return
     */
    public List<String> generatingFolderFile(String folderId,int catalogueId) {
        //创建错误信息集合
        List<String> errors = new ArrayList<>();
        //创建新条目集合存入MongoDB
        List<Entry> entriesForSave = new ArrayList<>();
        //卷内条目集合
        List<Entry> folderFileList = new ArrayList<>();

        //得到目录类型
        Catalogue catalogue = catalogueRepository.findById(catalogueId).orElse(null);
        CatalogueType catalogueType = catalogue.getCatalogueType();

        if(catalogueType != CatalogueType.FolderFile){
            throw new BusinessException("错误的接口");
        }

        //通过卷内目录id获得所有卷内条目集合
        List<Entry> entryList = entryMongoRepository.findAll("archive_record_"+catalogueId);

        //筛选出来上级id是传进来的案卷id的条目集合进行生成档号
        for (Entry entry : entryList) {
            if (folderId.equals(entry.getParentId())){
                folderFileList.add(entry);
            }
        }

        //TODO 得到案卷档号
        //Entry folderEntry = entryMongoRepository.findById(folderId).orElse(null);
        //String folderArchivalCode = folderEntry.getItems().get("archivalCode")+"";
        //String folderArchivalCode = entryMongoRepository.findById(folderId).get().getItems().get("archivalCode").toString();
        String folderArchivalCode ="";
        Optional<Entry> folderEntry = entryMongoRepository.findById(folderId);
        if (folderEntry.isPresent()){
            Entry entry = folderEntry.get();
            folderArchivalCode = entry.getItems().get("archivalCode").toString();
        }
        //生成档号
        generatingFolderFileArchivalCode(folderFileList,entriesForSave,errors,folderArchivalCode);

        //把条目集合存入MongoDB
        if (entriesForSave.size() > 0){
            entryMongoRepository.saveAll(entriesForSave);
        }

        //返回错误信息集合
        if (errors.size() > 0) {
            return errors;
        }

        return null;
    }

    /**
     *生成卷内档号
     */
    public void generatingFolderFileArchivalCode(List<Entry> folderFileList,List<Entry> entriesForSave,List<String> errors,String folderArchivalCode){

        String archivalCodeNameOfFolderFile ="archivalCode";
        String serialNumberNameOfFolderFile="serialNumber";

        //定义档号
        String archivalCode = "";
        //定义卷内顺序号
        Integer serialNumber = 0;
        String newSerialNumber = "";

        //循环卷内
        for (Entry entry : folderFileList) {

            //获取卷内条目
            Map<String, Object> items = entry.getItems();

            serialNumber++;
            //得到顺序号
            newSerialNumber = generatingAllTypeSerialNumber(serialNumber,entry.getCatalogueId());
            //生成档号
            archivalCode = folderArchivalCode + newSerialNumber;

            //存入MongoDB
            items.put(archivalCodeNameOfFolderFile, archivalCode);
            items.put(serialNumberNameOfFolderFile,newSerialNumber);
            entry.setItems(items);
            entriesForSave.add(entry);
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
        List<List<Entry>> folderFileEntryAll = new ArrayList<>(); //创建卷内条目集合
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

        //TODO @leo 获取 archivalCodeMetaDataName 和 serialNumberMetaDataName

        //查找条目，要传入条目id和目录id
        Iterable<Entry> entryList = entryMongoRepository.findAllById(entryIds, "archive_record_" + catalogueId);

        //如果是案卷则查找所有卷内条目集合
        if (catalogueType == CatalogueType.Folder){

            //通过流遍历获取案卷id集合
            List<String> folderIdList =StreamSupport.stream(entryList.spliterator(),false).map(Entry::getId).collect(Collectors.toList());

            //1.通过案卷集合得到案卷目录id集合
            List<Integer> catalogueIdList = StreamSupport.stream(entryList.spliterator(),false).map(Entry::getCatalogueId).collect(Collectors.toList());
            //2.通过案卷目录id集合得到案卷目录集合
            List<Catalogue> folderCatalogueList = catalogueRepository.findAllById(catalogueIdList);
            //3.通过目录中的档案库id和目录类型为卷内得到卷内目录集合
            List<Integer> archivesId = folderCatalogueList.stream().map(Catalogue::getArchivesId).collect(Collectors.toList());
            List<Catalogue> folderFileCatalogueList = catalogueRepository.findAllByArchivesIdAndCatalogueType(archivesId,CatalogueType.FolderFile);
            //4.得到卷内的目录id集合
            List<Integer> folderFileCatalogueIds = folderFileCatalogueList.stream().map(Catalogue::getId).collect(Collectors.toList());
            //5.通过卷内的目录id集合得到卷内所有条目集合
            for (Integer folderFileCatalogueId : folderFileCatalogueIds ) {
                folderFileEntryAll.add(entryMongoRepository.findAll("archive_record_"+folderFileCatalogueId));
            }

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
                        String serialNumberVal = generatingSerialNumber(items,entry.getCatalogueId());
                        //生成档号（多加了序号）
                        splicingContent = archivalCodeRuler(archivalCodeRuler,items,errors,entry)+serialNumberVal;
                    }else {
                        splicingContent = archivalCodeRuler(archivalCodeRuler,items,errors,entry);
                    }
                }else {
                    splicingContent = archivalCodeRuler(archivalCodeRuler,items,errors,entry);
                }

                //生成卷内档号
                if (folderFileEntryAll!=null) {//如果所有卷内集合不为空
                    List<Entry> folderFileEntryList = new ArrayList<>();
                    //通过案卷id得到卷内集合
                    folderFileEntryList.addAll(StreamSupport.stream(folderFileEntryList.spliterator(), false).filter(item -> item.getParentId().equals(entry.getId())).collect(Collectors.toList()));
                    if (folderFileEntryList.size()>0) {
                        //生成档号
                        generatingFolderFileArchivalCode(folderFileEntryList, entriesForSave, errors, splicingContent);
                    }
                }

                archivalCodeVal.append(splicingContent);
                items.put("archivalCode", archivalCodeVal.toString());
                entry.setItems(items);
                entriesForSave.add(entry);
            }
        }
    }

    /**
     * 生成案卷及一文一件序号
     * @param items 条目
     * @return 返回序号
     */
    private String generatingSerialNumber(Map<String, Object> items,Integer catalogueId){

        //序号
        Integer serialNumber = -1;

        //TODO 调用仓储层得到最大序号
        //String tableName = catalogueRepository.findById(catalogueId).get().getTableName();
        String maxSerialNumber = "";

        //如果要生成第一个序号  (也就是最大序号为空)
        if (maxSerialNumber.equals("")){
            serialNumber = 1;
        }else {//如果有则执行以下的
            //得到序号
            serialNumber = Integer.parseInt(maxSerialNumber);
            serialNumber++;
        }

        String newSerialNumber = generatingAllTypeSerialNumber(serialNumber,catalogueId);

        //放入条目
        items.put("serialNumber",newSerialNumber);

        //返回序号
        return serialNumber.toString();

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
                str = fondsRepository.findById(entry.getFondsId()).getCode();
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
     * 案卷、一文一件及卷内序号的位数补零
     * @param serialNumber 序号
     * @return 补零后的序号
     */
    private String generatingAllTypeSerialNumber(Integer serialNumber,Integer catalogueId){

        //TODO 调用仓储层得到序号长度
        Integer i = catalogueRepository.findById(catalogueId).get().getSerialLength();
        //补零  (%表示结果为字面值 0 代表前面补充0 4 代表长度为4 d 代表参数为正数型)
        String newSerialNumber = String.format("%0"+i+"d", serialNumber);

        return newSerialNumber;
    }
}
