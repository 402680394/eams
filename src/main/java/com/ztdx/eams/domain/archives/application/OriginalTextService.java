package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.utils.FileReaderUtils;
import com.ztdx.eams.basic.utils.FtpUtil;
import com.ztdx.eams.domain.archives.model.Entry;
import com.ztdx.eams.domain.archives.model.OriginalText;
import com.ztdx.eams.domain.archives.repository.ArchivesGroupRepository;
import com.ztdx.eams.domain.archives.repository.ArchivesRepository;
import com.ztdx.eams.domain.archives.repository.CatalogueRepository;
import com.ztdx.eams.domain.archives.repository.elasticsearch.EntryElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.elasticsearch.OriginalTextElasticsearchRepository;
import com.ztdx.eams.domain.archives.repository.mongo.OriginalTextMongoRepository;
import org.apache.commons.codec.digest.DigestUtils;
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

    /**
     * 新增原文
     */
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

    /**
     * 删除原文
     */
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

    /**
     * 修改原文
     */
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

    /**
     * 原文文件上传
     */
    private void fileUpload(int fondsId, OriginalText originalText, MultipartFile file) {
        //先存入本地
        File tmpFile = new File(file.getOriginalFilename());
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        FileInputStream fisMD5 = null;
        try {
            byte[] bytes = file.getBytes();
            fos = new FileOutputStream(tmpFile);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
            bos.flush();
            //计算MD5
            fisMD5 = new FileInputStream(tmpFile);
            String MD5 = DigestUtils.md5Hex(fisMD5);
            //设置路径
            String[] path = new String[]{String.valueOf(fondsId), MD5.substring(0, 2), MD5.substring(2, 4)};
            //TODO li 获取文件属性

            originalText.setName(file.getOriginalFilename());
            originalText.setSize(String.valueOf(tmpFile.length()));
            originalText.setMd5(MD5);
            //上传文件到FTP
            ftpUtil.uploadFile(path, MD5, tmpFile);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("文件上传失败");
        } finally {
            if (fisMD5 != null) {
                try {
                    fisMD5.close();
                } catch (IOException e) {
                    throw new BusinessException("文件传输流未关闭");
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    throw new BusinessException("文件传输流未关闭");
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new BusinessException("文件传输流未关闭");
                }
            }
            //删除本地临时文件
            tmpFile.delete();
        }
    }

    /**
     * 获取原文文件元数据
     */
    public Map<String, Object> fileAttributes(int catalogueId, String entryId, String id) {
        Optional<OriginalText> find = originalTextMongoRepository.findById(id, "archive_record_originalText_" + catalogueId);
        if (find.isPresent()) {
            return find.get().getFileAttributesMap();
        }
        return new HashMap<>();
    }

    /**
     * 获取原文
     */
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

    /**
     * 原文文件下载
     */
    public void fileDownload(int catalogueId, String id, HttpServletResponse response) {
        Integer fondsId = archivesGroupRepository.findFondsIdByCatalogue_CatalogueId(catalogueId);
        if (null == fondsId) {
            throw new InvalidArgumentException("全宗档案库不存在");
        }
        Optional<OriginalText> find = originalTextMongoRepository.findById(id, "archive_record_originalText_" + catalogueId);
        if (find.isPresent()) {
            File file = new File(find.get().getName());
            //设置路径
            String[] path = new String[]{String.valueOf(fondsId), find.get().getMd5().substring(0, 2), find.get().getMd5().substring(2, 4)};
            ftpUtil.downloadFile(path, find.get().getMd5(), file);

            byte[] buff = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            OutputStream os = null;
            try {
                response.setContentType("application/octet-stream");
                response.setHeader("content-type", "application/octet-stream");
                response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(find.get().getName(), "UTF-8"));
                os = response.getOutputStream();
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                int i = bis.read(buff);
                while (i != -1) {
                    os.write(buff, 0, buff.length);
                    os.flush();
                    i = bis.read(buff);
                }
            } catch (IOException e) {
                throw new BusinessException("文件下载失败");
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        throw new BusinessException("文件传输流未关闭");
                    }
                }
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        throw new BusinessException("文件传输流未关闭");
                    }
                }
                if (os != null) {
                    try {
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

    /**
     * 原文列表
     */
    public Page<OriginalText> list(int catalogueId, String entryId, String title, int page, int size) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.wildcardQuery("title",
                "*" + title + "*"));
        query.must(QueryBuilders.matchQuery("entryId", entryId));
        return originalTextElasticsearchRepository.search(query, PageRequest.of(page, size, Sort.by(Sort.Order.asc("orderNumber"))), new String[]{"archive_record_" + catalogueId});
    }

    /**
     * 原文排序
     */
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

    /**
     * 生成全文索引
     */
    public void createContentIndex(String id, int catalogueId) {
        Integer fondsId = archivesGroupRepository.findFondsIdByCatalogue_CatalogueId(catalogueId);
        if (null == fondsId) {
            throw new InvalidArgumentException("全宗档案库不存在");
        }
        Optional<OriginalText> find = originalTextMongoRepository.findById(id, "archive_record_originalText_" + catalogueId);
        if (find.isPresent()) {
            String name = find.get().getName();
            if (name.endsWith(".txt") || name.endsWith(".doc") || name.endsWith(".docx")) {
                //临时文件
                File file = new File(name);
                try {
                    //设置路径
                    String[] path = new String[]{String.valueOf(fondsId), find.get().getMd5().substring(0, 2), find.get().getMd5().substring(2, 4)};
                    //下载到临时文件
                    ftpUtil.downloadFile(path, find.get().getMd5(), file);
                    //读取内容
                    if (name.endsWith(".txt")) {
                        find.get().setContentIndex(FileReaderUtils.txtRead(file));
                    }
                    if (name.endsWith(".doc")) {
                        find.get().setContentIndex(FileReaderUtils.docRead(file));
                    }
                    if (name.endsWith(".docx")) {
                        find.get().setContentIndex(FileReaderUtils.docxRead(file));
                    }
                    find.get().setContentIndexStatus(1);
                    originalTextElasticsearchRepository.save(find.get());
                    originalTextMongoRepository.save(find.get());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    //删除临时文件
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        }
    }
}
