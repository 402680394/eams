package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.utils.FtpUtil;
import com.ztdx.eams.domain.archives.model.Entry;
import com.ztdx.eams.domain.archives.model.OriginalText;
import com.ztdx.eams.domain.archives.repository.ArchivesGroupRepository;
import com.ztdx.eams.domain.archives.repository.ArchivesRepository;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import com.ztdx.eams.domain.archives.repository.elasticsearch.EntryElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.elasticsearch.OriginalTextElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.mongo.OriginalTextMongoRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    private final OriginalTextElasticsearchRepository originalTextElasticsearchRepository;

    private final CatalogueRepository catalogueRepository;

    private final ArchivesRepository archivesRepository;

    private final ArchivesGroupRepository archivesGroupRepository;

    private final FtpUtil ftpUtil;

    @Autowired
    public OriginalTextService(EntryElasticsearchRepository entryElasticsearchRepository, OriginalTextMongoRepository originalTextMongoRepository, OriginalTextElasticsearchRepository originalTextElasticsearchRepository, CatalogueRepository catalogueRepository, ArchivesRepository archivesRepository, ArchivesGroupRepository archivesGroupRepository, FtpUtil ftpUtil) {
        this.entryElasticsearchRepository = entryElasticsearchRepository;
        this.originalTextMongoRepository = originalTextMongoRepository;
        this.originalTextElasticsearchRepository = originalTextElasticsearchRepository;
        this.catalogueRepository = catalogueRepository;
        this.archivesRepository = archivesRepository;
        this.archivesGroupRepository = archivesGroupRepository;
        this.ftpUtil = ftpUtil;
    }

    public void save(OriginalText originalText, MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidArgumentException("原文文件未上传");
        }
        Integer fondsId = archivesGroupRepository.findFondsIdByCatalogue_CatalogueId(originalText.getCatalogueId());
        if (null == fondsId) {
            throw new InvalidArgumentException("全宗档案库不存在");
        }
        Optional<Entry> find = entryElasticsearchRepository.findById(originalText.getEntryId(), "archive_record_" + originalText.getCatalogueId());
        if (!find.isPresent()) {
            throw new InvalidArgumentException("条目不存在");
        }
        //文件上传到本地服务器读取文件属性,然后上传至ftp
        fileUpload(fondsId, originalText, file);

        //设置排序号
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.matchQuery("entryId", originalText.getEntryId()));
        Iterable<OriginalText> iterable = originalTextElasticsearchRepository.search(query, new String[]{"archive_record_" + originalText.getCatalogueId()});
        int orderNumber = 0;
        Iterator<OriginalText> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            OriginalText o = iterator.next();
            if (o.getOrderNumber() > orderNumber) {
                orderNumber = o.getOrderNumber();
            }
        }
        originalText.setOrderNumber(orderNumber + 1);
        originalText.setId(String.valueOf(UUID.randomUUID()));
        originalText.setCreateTime(new Date());
        originalText.setGmtCreate(new Date());
        originalText.setGmtModified(new Date());
        //存入MongoDB
        originalTextMongoRepository.save(originalText);

        //存入Elasticsearch
        originalTextElasticsearchRepository.save(originalText);
    }

    public void deleteBatch(List<Map<String, Object>> list) {
        for (Map map : list) {
            String id = (String) map.get("id");
            int catalogueId = (int) map.get("catalogueId");

            //删除MongoDB信息
            originalTextMongoRepository.deleteById(id, "archive_record_originalText_" + catalogueId);
            //删除Elasticsearch信息
            originalTextElasticsearchRepository.deleteById(id, "archive_record_" + catalogueId);
        }
    }

    public void update(OriginalText originalText, MultipartFile file) {
        Integer fondsId = archivesGroupRepository.findFondsIdByCatalogue_CatalogueId(originalText.getCatalogueId());
        if (null == fondsId) {
            throw new InvalidArgumentException("全宗档案库不存在");
        }
        Optional<OriginalText> find = originalTextMongoRepository.findById(originalText.getId(), "archive_record_originalText_" + originalText.getCatalogueId());
        if (!find.isPresent()) {
            save(originalText, file);
            return;
        }
        if (!file.isEmpty()) {
            //文件上传到本地服务器读取文件属性,然后上传至ftp
            fileUpload(fondsId, originalText, file);
        }
        originalText.setCreateTime(find.get().getCreateTime());
        originalText.setGmtCreate(find.get().getGmtCreate());
        originalText.setGmtModified(new Date());
        //修改MongoDB
        originalTextMongoRepository.save(originalText);

        //修改Elasticsearch
        originalTextElasticsearchRepository.save(originalText);
    }

    private void fileUpload(int fondsId, OriginalText originalText, MultipartFile file) {
        //文件上传
        File tmpFile = new File(file.getOriginalFilename());
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            //先存入本地
            byte[] bytes = file.getBytes();
            fos = new FileOutputStream(tmpFile);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
            bos.flush();

            //TODO li 获取文件属性
//            Metadata metadata = JpegMetadataReader.readMetadata(tmpFile);
//            HashMap fileAttributesMap = new HashMap<String, Object>();
//            for (Directory directory : metadata.getDirectories()) {
//                fileAttributesMap.put(directory.getName(), directory.getTags());
//                for (Tag tag : directory.getTags()) {
//                    System.out.println(tag.getTagName() + ":" + tag.getDescription());
//              }
//            }
            originalText.setName(file.getOriginalFilename());
            originalText.setSize(String.valueOf(tmpFile.length()));
            //上传文件到FTP
            originalText.setMd5(ftpUtil.uploadFile(fondsId, tmpFile));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("文件上传失败");
        } finally {
            try {
                bos.close();
                fos.close();
            } catch (IOException e) {
                throw new BusinessException("文件传输流未关闭");
            }
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

    public void fileDownload(int catalogueId, String id, HttpServletResponse response) {
        Integer fondsId = archivesGroupRepository.findFondsIdByCatalogue_CatalogueId(catalogueId);
        if (null == fondsId) {
            throw new InvalidArgumentException("全宗档案库不存在");
        }
        Optional<OriginalText> find = originalTextMongoRepository.findById(id, "archive_record_originalText_" + catalogueId);
        if (find.isPresent()) {
            File file = ftpUtil.downloadFile(fondsId, find.get().getMd5(), find.get().getName());
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
                if (bis != null) {
                    try {
                        bis.close();
                        os.close();
                    } catch (IOException e) {
                        throw new BusinessException("文件传输流未关闭");
                    }
                }
                file.delete();
            }
        } else {
            throw new BusinessException("文件下载失败");
        }
    }

    public Page<OriginalText> list(int catalogueId, String entryId, String title, int page, int size) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.wildcardQuery("title",
                "*" + title + "*"));
        query.must(QueryBuilders.matchQuery("entryId", entryId));
        return originalTextElasticsearchRepository.search(query, PageRequest.of(page, size, Sort.by(Sort.Order.asc("orderNumber"))), new String[]{"archive_record_" + catalogueId});
    }

    public void sort(String upId, String downId, int catalogueId) {
        Optional<OriginalText> upFind = originalTextMongoRepository.findById(upId, "archive_record_originalText_" + catalogueId);
        Optional<OriginalText> downFind = originalTextMongoRepository.findById(downId, "archive_record_originalText_" + catalogueId);
        if (!upFind.isPresent() || !downFind.isPresent()) {
            throw new InvalidArgumentException("原文记录不存在");
        } else {
            int tmpOrderNumber;
            tmpOrderNumber = upFind.get().getOrderNumber();
            upFind.get().setOrderNumber(downFind.get().getOrderNumber());
            downFind.get().setOrderNumber(tmpOrderNumber);

            originalTextMongoRepository.save(upFind.get());
            originalTextElasticsearchRepository.save(upFind.get());
            originalTextMongoRepository.save(downFind.get());
            originalTextElasticsearchRepository.save(downFind.get());
        }
    }
}
