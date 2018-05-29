package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.utils.FtpFileUtil;
import com.ztdx.eams.domain.archives.model.Catalogue;
import com.ztdx.eams.domain.archives.model.Entry;
import com.ztdx.eams.domain.archives.model.OriginalText;
import com.ztdx.eams.domain.archives.repository.ArchivesRepository;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import com.ztdx.eams.domain.archives.repository.elasticsearch.EntryElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.mongo.OriginalTextMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
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

    private final FtpFileUtil ftpFileUtil;

    @Autowired
    public OriginalTextService(EntryElasticsearchRepository entryElasticsearchRepository, OriginalTextMongoRepository originalTextMongoRepository, CatalogueRepository catalogueRepository, ArchivesRepository archivesRepository, FtpFileUtil ftpFileUtil) {
        this.entryElasticsearchRepository = entryElasticsearchRepository;
        this.originalTextMongoRepository = originalTextMongoRepository;
        this.catalogueRepository = catalogueRepository;
        this.archivesRepository = archivesRepository;
        this.ftpFileUtil = ftpFileUtil;
    }

    public void save(OriginalText originalText, MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidArgumentException("原文文件未上传");
        }
        Catalogue catalog = catalogueRepository.findById(originalText.getCatalogueId()).orElse(null);
        if (catalog == null) {
            throw new InvalidArgumentException("目录不存在");
        }
        if (null == archivesRepository.findById(catalog.getArchivesId()).orElse(null)) {
            throw new InvalidArgumentException("档案库不存在");
        }
//        verification(originalText.getCatalogueId(), originalText.getEntryId(), originalText.getId());
//        Entry entry = entryElasticsearchRepository.findById(originalText.getEntryId(), "archive_record_" + originalText.getCatalogueId()).get();
        Optional<Entry> find = entryElasticsearchRepository.findById(originalText.getEntryId(), "archive_record_" + originalText.getCatalogueId());
        if (!find.isPresent()) {
            throw new InvalidArgumentException("条目不存在或已被删除");
        }
        Entry entry = find.get();
        //读取文件基本属性
        originalText.setName(file.getOriginalFilename());

        //文件上传到本地服务器读取文件属性,然后上传至ftp
        fileUpload(originalText, file);

        originalText.setId(String.valueOf(UUID.randomUUID()));
        originalText.setGmtCreate(new Date());
        originalText.setGmtModified(new Date());
        //存入MongoDB
        originalTextMongoRepository.save(originalText);

        //存入Elasticsearch
        if (null == entry.getOriginalText() || entry.getOriginalText().isEmpty()) {
            ArrayList list = new ArrayList<OriginalText>();
            list.add(originalText);
            entry.setOriginalText(list);
        } else {
            entry.getOriginalText().add(originalText);
        }
        entry.setGmtModified(new Date());
        entryElasticsearchRepository.save(entry);
    }

    public void deleteBatch(List<Map<String, Object>> list) {
        for (Map map : list) {
            String id = (String) map.get("id");
            String entryId = (String) map.get("entryId");
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
        }
    }

    public void update(OriginalText originalText, MultipartFile file) {
//        verification(originalText.getCatalogueId(), originalText.getEntryId(), originalText.getId());
//        Entry entry = entryElasticsearchRepository.findById(originalText.getEntryId(), "archive_record_" + originalText.getCatalogueId()).get();
        Catalogue catalog = catalogueRepository.findById(originalText.getCatalogueId()).orElse(null);
        if (catalog == null) {
            throw new InvalidArgumentException("目录不存在");
        }
        if (null == archivesRepository.findById(catalog.getArchivesId()).orElse(null)) {
            throw new InvalidArgumentException("档案库不存在");
        }
        Optional<Entry> find = entryElasticsearchRepository.findById(originalText.getEntryId(), "archive_record_" + originalText.getCatalogueId());
        if (!find.isPresent()) {
            throw new InvalidArgumentException("条目不存在或已被删除");
        }
        Entry entry = find.get();
        if (!file.isEmpty()) {
            //读取文件基本属性
            originalText.setName(file.getOriginalFilename());

            //文件上传到本地服务器读取文件属性,然后上传至ftp
            fileUpload(originalText, file);
        }
        originalText.setGmtCreate(new Date());
        originalText.setGmtModified(new Date());
        //修改MongoDB
        originalTextMongoRepository.save(originalText);

        //修改Elasticsearch
        if (null == entry.getOriginalText() || entry.getOriginalText().isEmpty()) {
            ArrayList list = new ArrayList<OriginalText>();
            list.add(originalText);
            entry.setOriginalText(list);
        } else {
            entry.getOriginalText().add(originalText);
        }
        entry.setGmtModified(new Date());
        entryElasticsearchRepository.save(entry);
    }

    private void fileUpload(OriginalText originalText, MultipartFile file) {
        //文件上传
        File tmpFile = new File(file.getOriginalFilename());
        try {
            //先存入本地
            byte[] bytes = file.getBytes();
            FileOutputStream fos = new FileOutputStream(tmpFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(bytes);
            fos.close();
            bos.close();

            //获取文件属性
//            Metadata metadata = JpegMetadataReader.readMetadata(tmpFile);
//            HashMap fileAttributesMap = new HashMap<String, Object>();
//            for (Directory directory : metadata.getDirectories()) {
//                fileAttributesMap.put(directory.getName(), directory.getTags());
//                for (Tag tag : directory.getTags()) {
//                    System.out.println(tag.getTagName() + ":" + tag.getDescription());
//              }
//            }

            //上传文件到FTP
            originalText.setFtpFileMD5(ftpFileUtil.uploadFile(tmpFile));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("文件上传失败");
        } finally {
            //删除本地临时文件
            tmpFile.delete();
        }
    }

    public Map<String, Object> fileAttributes(int catalogueId, String entryId, String id) {
        Optional<OriginalText> find = originalTextMongoRepository.findById(id, "archive_record_originalText_" + catalogueId);
        if (find.isPresent()) {
            return find.get().getFileAttributesMap();
        }
        return new HashMap<>();
    }

    public Map<String, Object> get(int catalogueId, String id) {
        HashMap resultMap = new HashMap<String, Object>();
        Optional<OriginalText> find = originalTextMongoRepository.findById(id, "archive_record_originalText_" + catalogueId);
        if (find.isPresent()) {
            resultMap.put("id", find.get().getId());
            resultMap.put("title", find.get().getTitle());
            resultMap.put("type", find.get().getType());
            resultMap.put("version", find.get().getVersion());
            resultMap.put("remark", find.get().getRemark());
        }
        return resultMap;
    }

    public void fileDownload(int catalogueId, String entryId, String id, HttpServletResponse response) {
        Optional<OriginalText> find = originalTextMongoRepository.findById(id, "archive_record_originalText_" + catalogueId);
        if (find.isPresent()) {
            File file = ftpFileUtil.downloadFile(find.get().getFtpFileMD5(), find.get().getName());
            String filename = null;
            try {
                filename = URLEncoder.encode(find.get().getName(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);

            byte[] buff = new byte[1024];
            BufferedInputStream bis = null;
            OutputStream os = null;
            try {
                os = response.getOutputStream();
                bis = new BufferedInputStream(new FileInputStream(file));
                int i = bis.read(buff);
                while (i != -1) {
                    os.write(buff, 0, buff.length);
                    os.flush();
                    i = bis.read(buff);
                }
            } catch (IOException e) {
                throw new BusinessException("文件下载失败");
            } finally {
                file.delete();
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

//    private void verification(int catalogueId, String entryId, String id) {
//        Catalogue catalog = catalogueRepository.findById(catalogueId).orElse(null);
//        if (catalog == null) {
//            throw new InvalidArgumentException("目录不存在");
//        }
//        if (null == archivesRepository.findById(catalog.getArchivesId()).orElse(null)) {
//            throw new InvalidArgumentException("档案库不存在");
//        }
//        if (!entryElasticsearchRepository.existsById(entryId, "archive_record_" + catalogueId)) {
//            throw new InvalidArgumentException("条目不存在或已被删除");
//        }
//    }
}
