package com.ztdx.eams.domain.archives.model.archivalCodeRuler;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.domain.archives.application.task.EntryAsyncTask;
import com.ztdx.eams.domain.archives.model.*;
import com.ztdx.eams.domain.archives.repository.ArchivalCodeRulerRepository;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import com.ztdx.eams.domain.archives.repository.DescriptionItemRepository;
import com.ztdx.eams.domain.archives.repository.mongo.EntryMongoRepository;
import com.ztdx.eams.domain.system.repository.FondsRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;

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
    private final DescriptionItemRepository descriptionItemRepository;
    private final EntryAsyncTask entryAsyncTask;

    public GeneratingBusiness(ArchivalCodeRulerRepository archivalcodeRulerRepository, EntryMongoRepository entryMongoRepository, CatalogueRepository catalogueRepository, FondsRepository fondsRepository, DescriptionItemRepository descriptionItemRepository, EntryAsyncTask entryAsyncTask) {
        this.archivalcodeRulerRepository = archivalcodeRulerRepository;
        this.entryMongoRepository = entryMongoRepository;
        this.catalogueRepository = catalogueRepository;
        this.fondsRepository = fondsRepository;
        this.descriptionItemRepository = descriptionItemRepository;
        this.entryAsyncTask = entryAsyncTask;
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
            entryAsyncTask.indexAll(entriesForSave, catalogueId);
        }

        //把卷内条目集合存入MongoDB
        if (folderFileEntriesForSave.size() > 0){
            entryMongoRepository.saveAll(folderFileEntriesForSave);
            entryAsyncTask.indexAll(folderFileEntriesForSave, folderFileEntriesForSave.get(0).getCatalogueId());
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
        //定义案卷档号
        String folderArchivalCode ="";
        String archivalCodeNameOfFolderFile = "";
        String serialNumberNameOfFolderFile = "";

        //得到目录类型
        Catalogue catalogue = catalogueRepository.findById(catalogueId).orElse(null);
        CatalogueType catalogueType = catalogue.getCatalogueType();

        if(catalogueType != CatalogueType.FolderFile){
            throw new BusinessException("错误的接口");
        }

        archivalCodeNameOfFolderFile = getArchivalCodeMetadataName(archivalCodeNameOfFolderFile,catalogueId);
        serialNumberNameOfFolderFile = getSerialNumberName(serialNumberNameOfFolderFile,catalogueId);
        if (archivalCodeNameOfFolderFile==null){
            throw  new BusinessException(catalogueType.getDescription()+"没有档号列");
        }else if (serialNumberNameOfFolderFile==null){
            throw new BusinessException(catalogueType.getDescription()+"没有序号列");
        }

        //通过卷内目录id获得所有卷内条目集合
        List<Entry> entryList = entryMongoRepository.findAll("archive_record_"+catalogueId);

        //筛选出来上级id是传进来的案卷id的条目集合进行生成档号
        for (Entry entry : entryList) {
            if (folderId.equals(entry.getParentId())){
                folderFileList.add(entry);
            }
        }

        //通过卷内目录id获得目录
        Catalogue folderFileCatalogue = catalogueRepository.findById(catalogueId).orElse(null);
        //通过档案库id和目录类型为案卷获得案卷目录
        Optional<Catalogue> folderCatalogueList = catalogueRepository.findByArchivesIdAndCatalogueType(folderFileCatalogue.getArchivesId(),CatalogueType.Folder);
        //通过案卷id和案卷目录id获得案卷条目
        Optional<Entry> folderEntry = entryMongoRepository.findById(folderId,"archive_record_"+folderCatalogueList.get().getId());
        //得到案卷档号
        if (folderEntry.isPresent()){
            Entry find = folderEntry.get();
            folderArchivalCode = (String)find.getItems().get(archivalCodeNameOfFolderFile);
        }

        //生成档号
        generatingFolderFileArchivalCode(folderFileList,entriesForSave,errors,folderArchivalCode,archivalCodeNameOfFolderFile,serialNumberNameOfFolderFile);

        //把条目集合存入MongoDB
        if (entriesForSave.size() > 0){
            entryMongoRepository.saveAll(entriesForSave);
            entryAsyncTask.indexAll(entriesForSave, entriesForSave.get(0).getCatalogueId());
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
    public void generatingFolderFileArchivalCode(List<Entry> folderFileList,List<Entry> entriesForSave,List<String> errors,String folderArchivalCode,String archivalCodeNameOfFolderFile,String serialNumberNameOfFolderFile){

        //定义档号
        String archivalCode = "";
        //定义卷内顺序号
        Integer serialNumber = 0;
        String newSerialNumber = "";

        //循环卷内
        for (Entry entry : folderFileList) {

            //获取卷内条目
            Map<String, Object> items = entry.getItems();

            //如果档号已经存在，则返回错误信息
            if (null!=items.get(archivalCode) && !"".equals(items.get(archivalCode))) {
                errors.add("档号已存在");
                continue;
            }

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
        String archivalCodeName ="";
        String serialNumberName="";
        String archivalCodeNameOfFolderFile ="";
        String serialNumberNameOfFolderFile="";
        Integer folderFileCatalogueId = -1;

        archivalCodeName = getArchivalCodeMetadataName(archivalCodeName,catalogueId);
        if (archivalCodeName==null){
            throw  new BusinessException(getCatalogueType(catalogueId)+"没有档号列");
        }

        //通过目录id查询到的规则放入规则集合
        List<ArchivalCodeRuler> archivalCodeRulers = archivalcodeRulerRepository.findByCatalogueIdOrderByOrderNumber(catalogueId);
        //如果规则集合为空，则抛异常
        if (archivalCodeRulers.size() == 0) {
            throw new BusinessException("该目录未设置档号生成规则");
        }
        //取出规则集合的序号
        serialNumberName = StreamSupport.stream(archivalCodeRulers.spliterator(),false).filter(archivalCodeRuler -> archivalCodeRuler.getType().equals(RulerType.SerialNumber)).toString();
        if (serialNumberName!=null){
            serialNumberName = getSerialNumberName(serialNumberName,catalogueId);
            if (serialNumberName==null){
                throw new BusinessException(getCatalogueType(catalogueId)+"没有序号列");
            }
        }

        //查找条目，要传入条目id和目录id
        Iterable<Entry> entryList = entryMongoRepository.findAllById(entryIds, "archive_record_" + catalogueId);

        //如果是案卷则查找所有卷内条目集合
        if (catalogueType == CatalogueType.Folder){

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
            for (Integer folderFileCid : folderFileCatalogueIds ) {
                folderFileEntryAll.add(entryMongoRepository.findAll("archive_record_"+folderFileCid));
                folderFileCatalogueId = folderFileCid;
            }

        }

        //遍历条目集合
        for (Entry entry : entryList) {

            //取条目中的著录项集合
            Map<String, Object> items = entry.getItems();
            //如果档号已经存在，则返回错误信息
            if (null!=items.get(archivalCodeName) && !"".equals(items.get(archivalCodeName))) {
                errors.add(getCatalogueType(entry.getCatalogueId())+"档号已存在");
                continue;
            }

            //遍历规则集合
            for (ArchivalCodeRuler archivalCodeRuler : archivalCodeRulers) {

                //如果规则集合有序号
                if (archivalCodeRuler.getType()==RulerType.SerialNumber){
                    //如果条目中没有序号
                    if(items.get(serialNumberName) == null || items.get(serialNumberName).equals("")){
                        //生成序号
                        splicingContent = generatingSerialNumber(items,entry.getCatalogueId(),serialNumberName);
                    }else {
                        splicingContent = archivalCodeRuler(archivalCodeRuler,items,errors,entry,serialNumberName);
                    }
                }else {
                    splicingContent = archivalCodeRuler(archivalCodeRuler,items,errors,entry,serialNumberName);
                }

                archivalCodeVal.append(splicingContent);

            }

            //生成卷内档号
            if (folderFileEntryAll!=null) {//如果所有卷内集合不为空
                List<Entry> folderFileEntryList = new ArrayList<>();
                //通过案卷id得到卷内集合
                for (List<Entry> entries : folderFileEntryAll) {
                    folderFileEntryList.addAll(StreamSupport.stream(entries.spliterator(),false).filter(entry1 -> entry.getId().equals(entry1.getParentId())).collect(Collectors.toList()));
                }
                if (folderFileEntryList.size()>0) {
                    //生成档号
                    archivalCodeNameOfFolderFile = getArchivalCodeMetadataName(archivalCodeNameOfFolderFile,folderFileCatalogueId);
                    serialNumberNameOfFolderFile = getSerialNumberName(serialNumberNameOfFolderFile,folderFileCatalogueId);
                    if (archivalCodeNameOfFolderFile==null){
                        errors.add(getCatalogueType(folderFileCatalogueId)+"没有档号列");
                    }else if (serialNumberNameOfFolderFile==null){
                        errors.add(getCatalogueType(folderFileCatalogueId)+"没有序号列");
                    }else {
                        generatingFolderFileArchivalCode(folderFileEntryList, folderFileEntriesForSave, errors, archivalCodeVal.toString(), archivalCodeNameOfFolderFile,serialNumberNameOfFolderFile);
                    }
                }
            }

            items.put(archivalCodeName, archivalCodeVal.toString());
            entry.setItems(items);
            entriesForSave.add(entry);

            archivalCodeVal.delete(0,archivalCodeVal.length());

        }
    }

    /**
     * 生成案卷及一文一件序号
     * @param items 条目
     * @return 返回序号
     */
    private String generatingSerialNumber(Map<String, Object> items,Integer catalogueId,String nameOfFolderFile){

        //序号
        Integer serialNumber = -1;
        String maxSerialNumber = "";


        //调用仓储层得到最大序号
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "items.sn")).limit(1);
        List<Entry> list = entryMongoRepository.findAll(query,"archive_record_"+catalogueId);

        if (list.size()>0) {
            maxSerialNumber = list.get(0).getItems().getOrDefault(nameOfFolderFile,"")+"";
        }

        //如果要生成第一个序号  (也就是最大序号为空)
        if ("".equals(maxSerialNumber)){
            serialNumber = 1;
        }else {//如果有则执行以下的
            //得到序号
            serialNumber = Integer.parseInt(maxSerialNumber);
                serialNumber++;
        }

        String newSerialNumber = generatingAllTypeSerialNumber(serialNumber,catalogueId);

        //放入条目
        items.put(nameOfFolderFile,newSerialNumber);

        //返回序号
        return newSerialNumber;

    }

    /**
     * 生成档号规则
     * @param archivalCodeRuler 档号规则
     * @param items 条目
     * @param errors 错误信息
     * @param entry 条目
     * @return 返回根据档号规则获取的要拼接的内容
     */
    private String archivalCodeRuler (ArchivalCodeRuler archivalCodeRuler,Map<String, Object> items,List<String> errors,Entry entry,String serialNumberName){

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
                str = (String)entryMongoRepository.findById(entry.getId(),"archive_record_"+entry.getCatalogueId()).get().getItems().get(serialNumberName);
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

        //调用仓储层得到序号长度
        Integer i = catalogueRepository.findById(catalogueId).get().getSerialLength();
        //补零  (%表示结果为字面值 0 代表前面补充0 4 代表长度为4 d 代表参数为正数型)
        String newSerialNumber = String.format("%0"+i+"d", serialNumber);

        return newSerialNumber;
    }

    /**
     * 得到元数据名称
     * @param archivalCodeName  名称
     * @param catalogueId 目录id
     * @return
     */
    public String getArchivalCodeMetadataName(String archivalCodeName,int catalogueId){
        DescriptionItem descriptionItem = descriptionItemRepository.findByCatalogueIdAndPropertyType(catalogueId,PropertyType.Reference);
        if (descriptionItem != null){
            archivalCodeName = descriptionItem.getMetadataName();
            return archivalCodeName;
        }
        return null;
    }

    /**
     * 得到元数据名称
     * @param serialNumberName  名称
     * @param catalogueId 目录id
     * @return
     */
    public String getSerialNumberName(String serialNumberName,int catalogueId){
        DescriptionItem descriptionItem = descriptionItemRepository.findByCatalogueIdAndPropertyType(catalogueId, PropertyType.SerialNumber);
        if (descriptionItem != null){
            serialNumberName = descriptionItem.getMetadataName();
            return serialNumberName;
        }
        return null;
    }

    /**
     * 获取目录类型
     * @param catalogueId 目录id
     * @return
     */
    public String getCatalogueType(Integer catalogueId){
        return catalogueRepository.findById(catalogueId).orElse(null).getCatalogueType().getDescription();
    }

}
