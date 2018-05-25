package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.model.*;
import com.ztdx.eams.domain.archives.repository.ArchivesRepository;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import com.ztdx.eams.domain.archives.repository.elasticsearch.EntryElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.mongo.OriginalTextMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

/**
 * Created by li on 2018/5/23.
 */
@Service
public class OriginalTextService {

    private final EntryElasticsearchRepository entryElasticsearchRepository;

    private final OriginalTextMongoRepository originalTextMongoRepository;

    private final CatalogueRepository catalogueRepository;

    private final ArchivesRepository archivesRepository;

    @Autowired
    public OriginalTextService(EntryElasticsearchRepository entryElasticsearchRepository, OriginalTextMongoRepository originalTextMongoRepository, CatalogueRepository catalogueRepository, ArchivesRepository archivesRepository) {
        this.entryElasticsearchRepository = entryElasticsearchRepository;
        this.originalTextMongoRepository = originalTextMongoRepository;
        this.catalogueRepository = catalogueRepository;
        this.archivesRepository = archivesRepository;
    }

    public void save(OriginalText originalText, MultipartFile file) {
        Catalogue catalog = catalogueRepository.findById(originalText.getCatalogueId()).orElse(null);
        if (catalog == null) {
            throw new InvalidArgumentException("目录不存在");
        }
        Archives archives = archivesRepository.findById(catalog.getArchivesId()).orElse(null);
        if (archives == null) {
            throw new InvalidArgumentException("档案库不存在");
        }
        //读取文件基本属性、类型属性
        originalText.setName(file.getOriginalFilename());

        //存入MongoDB
        originalText.setArchiveId(catalog.getArchivesId());
        originalText.setId(UUID.randomUUID());
        originalText.setGmtCreate(new Date());
        originalText.setGmtModified(new Date());
        originalTextMongoRepository.save(originalText);

        //存入Elasticsearch
        Optional<Entry> find = entryElasticsearchRepository.findById(originalText.getEntryId(), "archive_record_" + originalText.getCatalogueId());
        if (!find.isPresent()) {
            throw new InvalidArgumentException("条目不存在或已被删除");
        }
        Entry entry = find.get();
        if (null == entry.getOriginalText() || entry.getOriginalText().isEmpty()) {
            ArrayList list = new ArrayList<OriginalText>();
            list.add(originalText);
            entry.setOriginalText(list);
        } else {
            entry.getOriginalText().add(originalText);
        }
        entry.setGmtModified(new Date());
        entryElasticsearchRepository.save(entry);

        //将文件上传至本地服务器
        if (file.isEmpty()) {
            throw new InvalidArgumentException("请上传原文文件");
        } else {
            File tmpFile = new File(file.getOriginalFilename());
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            try {
                byte[] bytes = file.getBytes();
                fos = new FileOutputStream(tmpFile);
                bos = new BufferedOutputStream(fos);
                bos.write(bytes);
                fos.close();
                bos.close();
            } catch (Exception e) {
                throw new BusinessException("文件上传失败");
            }
            //上传文件到FTP

            //删除本地临时文件
            tmpFile.delete();
        }

    }

    public void deleteBatch(List<Map<String, Object>> list) {
        for (Map map : list) {
            UUID id = UUID.fromString((String) map.get("id"));
            UUID entryId = UUID.fromString((String) map.get("entryId"));
            int catalogueId = (int) map.get("catalogueId");
            //删除MongoDB信息
            originalTextMongoRepository.deleteById(id, "archive_record_originalText_" + map.get("catalogueId"));
            //删除Elasticsearch信息
            Optional<Entry> find = entryElasticsearchRepository.findById(entryId, "archive_record_" + map.get("catalogueId"));
            if (!find.isPresent()) {
                throw new InvalidArgumentException("条目不存在或已被删除");
            }
            Entry entry = find.get();
            List<OriginalText> originalTextList = entry.getOriginalText();
            if (!originalTextList.isEmpty() && null != originalTextList) {
                OriginalText deloriginalText = null;
                for (OriginalText originalText : originalTextList) {
                    originalText.getId().equals(id);
                    deloriginalText = originalText;
                }
                originalTextList.remove(deloriginalText);
            }
            entry.setOriginalText(originalTextList);
            entry.setGmtModified(new Date());
            entryElasticsearchRepository.save(entry);
            //删除ftp服务器文件

        }
    }
}
