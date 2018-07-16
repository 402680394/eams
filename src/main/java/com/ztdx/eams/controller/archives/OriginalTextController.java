package com.ztdx.eams.controller.archives;

import com.ztdx.eams.basic.exception.ForbiddenException;
import com.ztdx.eams.domain.archives.application.OriginalTextService;
import com.ztdx.eams.domain.archives.model.OriginalText;
import com.ztdx.eams.domain.system.application.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by li on 2018/5/23.
 */
@RestController
@RequestMapping(value = "/originalText")
public class OriginalTextController {

    private final OriginalTextService originalTextService;

    private final PermissionService permissionService;

    //private final Scheduler scheduler;

    @Autowired
    public OriginalTextController(OriginalTextService originalTextService, PermissionService permissionService) {
        this.originalTextService = originalTextService;
        this.permissionService = permissionService;
        //this.scheduler = scheduler;
    }

    /**
     * @api {post} /originalText 新增原文
     * @apiName save
     * @apiGroup originalText
     * @apiParam {Number} catalogueId 目录ID(form-data参数)
     * @apiParam {String} entryId 条目ID(form-data参数)
     * @apiParam {String} title 文件标题(form-data参数)
     * @apiParam {Number} type 文件类型(form-data参数)
     * @apiParam {String} version 文件版本(form-data参数)
     * @apiParam {String} remark 备注(form-data参数)
     * @apiParam {File} file 原文文件(form-data参数)
     * @apiError (Error 400) message 1.全宗档案库不存在;2.条目不存在;3.原文文件未上传.
     * @apiError (Error 500) message 1.文件上传失败;2.未连接到ftp服务;3.ftp服务未正常关闭.4.文件传输流未关闭.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_file_write_' + #request.getParameter(\"catalogueId\"))")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(HttpServletRequest request, MultipartFile file) {
        OriginalText originalText = new OriginalText();
        originalText.setCatalogueId(Integer.parseInt(request.getParameter("catalogueId")));
        originalText.setEntryId(request.getParameter("entryId"));
        originalText.setTitle(request.getParameter("title"));
        originalText.setType(Integer.parseInt(request.getParameter("type")));
        originalText.setVersion(request.getParameter("version"));
        originalText.setRemark(request.getParameter("remark"));
        originalText = originalTextService.save(originalText, file);
        originalTextService.placeOnFile(originalText);
    }

    /**
     * @api {delete} /originalText 删除原文
     * @apiName deleteBatch
     * @apiGroup originalText
     * @apiParam {String} id ID
     * @apiParam {Number} catalogueId 目录ID
     * @apiParamExample {json} Request-Example:
     * [{"id":"d500af0c-a136-4e43-bd0c-66dc6a4304e8","catalogueId":1}]
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public void deleteBatch(@RequestBody List<Map<String, Object>> list) {
        list.forEach(a -> {
            if (!permissionService.hasAnyAuthority(
                    "ROLE_ADMIN"
                    , "archive_file_write_"
                            + a.getOrDefault("catalogueId", "").toString())
                    ) {
                throw new ForbiddenException("没有权限删除原文");
            }
        });
        originalTextService.deleteBatch(list);
    }

    /**
     * @api {put} /originalText 修改原文
     * @apiName update
     * @apiGroup originalText
     * @apiParam {Number} catalogueId 目录ID(form-data参数)
     * @apiParam {String} entryId 条目ID(form-data参数)
     * @apiParam {String} id 原文ID(form-data参数)
     * @apiParam {String} title 文件标题(form-data参数)
     * @apiParam {Number} type 文件类型(form-data参数)
     * @apiParam {String} version 文件版本(form-data参数)
     * @apiParam {String} remark 备注(form-data参数)
     * @apiParam {File} file 原文文件(form-data参数)
     * @apiError (Error 400) message 全宗档案库不存在
     * @apiError (Error 500) message 1.文件上传失败;2.未连接到ftp服务;3.ftp服务未正常关闭;4.文件传输流未关闭.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_file_write_' + #request.getParameter(\"catalogueId\"))")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(HttpServletRequest request, MultipartFile file) {
        OriginalText originalText = new OriginalText();
        originalText.setCatalogueId(Integer.parseInt(request.getParameter("catalogueId")));
        originalText.setEntryId(request.getParameter("entryId"));
        originalText.setId(request.getParameter("id"));
        originalText.setTitle(request.getParameter("title"));
        originalText.setType(Integer.parseInt(request.getParameter("type")));
        originalText.setVersion(request.getParameter("version"));
        originalText.setRemark(request.getParameter("remark"));
        originalText = originalTextService.update(originalText, file);
        if (null != originalText) {
            originalTextService.placeOnFile(originalText);
        }
    }

    /**
     * @api {get} /originalText/fileAttributes 获取原文文件属性
     * @apiName fileAttributes
     * @apiGroup originalText
     * @apiParam {Number} catalogueId 目录ID(url参数)
     * @apiParam {String} id 原文ID(url参数)
     * @apiSuccess (Success 200) {Object} data 原文文件属性,动态key-value对象.
     * @apiSuccess (Success 200) {String} data.标题 原文文件属性示例.
     * @apiSuccess (Success 200) {String} data.主题 原文文件属性示例.
     * @apiSuccess (Success 200) {String} data.像素 原文文件属性示例.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_file_read_' + #catalogueId)")
    @RequestMapping(value = "/fileAttributes", method = RequestMethod.GET)
    public Map<String, String> fileAttributes(@RequestParam("catalogueId") int catalogueId, @RequestParam("id") String id) {
        return originalTextService.fileAttributes(catalogueId, id);
    }

    /**
     * @api {get} /originalText 获取原文信息
     * @apiName get
     * @apiGroup originalText
     * @apiParam {Number} catalogueId 目录ID(url参数)
     * @apiParam {String} id 原文ID(url参数)
     * @apiSuccess (Success 200) {String} id ID.
     * @apiSuccess (Success 200) {String} title 标题.
     * @apiSuccess (Success 200) {String} type 类型.
     * @apiSuccess (Success 200) {String} version 版本.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_file_read_' + #catalogueId)")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Map<String, Object> get(@RequestParam("catalogueId") int catalogueId, @RequestParam("id") String id) {
        return originalTextService.get(catalogueId, id);
    }

    /**
     * @api {get} /originalText/download 下载原文文件
     * @apiName download
     * @apiGroup originalText
     * @apiParam {Number} catalogueId 目录ID(url参数)
     * @apiParam {String} id 原文ID(url参数)
     * @apiError (Error 400) message 1.全宗档案库不存在;2.文件下载失败;3.ftp服务未正常关闭;4.文件传输流未关闭.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_file_read_' + #catalogueId)")
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(@RequestParam("catalogueId") int catalogueId, @RequestParam("id") String id, HttpServletResponse response) {
        originalTextService.fileDownload(1, catalogueId, id, response);
    }

    /**
     * @api {get} /originalText/downloadPDF 下载PDF格式文件
     * @apiName downloadPDF
     * @apiGroup originalText
     * @apiParam {Number} catalogueId 目录ID(url参数)
     * @apiParam {String} id 原文ID(url参数)
     * @apiError (Error 400) message 1.全宗档案库不存在;2.文件下载失败;3.ftp服务未正常关闭;4.文件传输流未关闭.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_file_read_' + #catalogueId)")
    @RequestMapping(value = "/downloadPDF", method = RequestMethod.GET)
    public void downloadPDF(@RequestParam("catalogueId") int catalogueId, @RequestParam("id") String id, HttpServletResponse response) {
        originalTextService.fileDownload(2, catalogueId, id, response);
    }

    /**
     * @api {get} /originalText/list 获取原文列表
     * @apiName list
     * @apiGroup originalText
     * @apiParam {Number} catalogueId 目录ID(url参数)
     * @apiParam {String} entryId 条目ID(url参数)
     * @apiParam {String} title 标题(url参数)(非必须)
     * @apiParam {Number} page 页码(url参数)
     * @apiParam {Number} size 每页条数(url参数)
     * @apiSuccess (Success 200) {String} id ID.
     * @apiSuccess (Success 200) {String} title 标题.
     * @apiSuccess (Success 200) {String} type 类型.
     * @apiSuccess (Success 200) {String} version 版本.
     * @apiSuccess (Success 200) {String} name 名称.
     * @apiSuccess (Success 200) {String} size 文件大小.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiSuccess (Success 200) {Number} createTime 创建时间.
     * @apiSuccess (Success 200) {Number} contentIndexStatus 全文索引状态(0-未生成  1-已生成 2-生成失败 3-文件类型不可用).
     * @apiSuccess (Success 200) {Number} pdfConverStatus pdf转换状态(0-未转换 1-已转换 2-转换失败 3-文件类型不可用).
     * @apiSuccess (Success 200) {Number} total 总条数.
     * @apiSuccessExample {json} Success-Response:
     * {
     * "data": {
     * "total": 8,
     * "items": [
     * {
     * "size": "482491",
     * "createTime": 1527663874,
     * "name": "DAT 1-2000 档案工作基本术语.pdf",
     * "remark": "备注",
     * "id": "e60b9591-fc7f-4de5-be4c-f5beddaf510d",
     * "title": "顺丰55677",
     * "type": 1,
     * "version": "1.0"
     * }
     * ]
     * }
     * }
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_file_read_' + #catalogueId)")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map<String, Object> list(@RequestParam("catalogueId") int catalogueId,
                                    @RequestParam("entryId") String entryId,
                                    @RequestParam(required = false, name = "title", defaultValue = "") String title,
                                    @RequestParam(required = false, name = "page", defaultValue = "0") int page,
                                    @RequestParam(required = false, name = "size", defaultValue = "20") int size) {
        Map<String, Object> result = new HashMap();
        List<Map<String, Object>> list = new ArrayList();
        Page<OriginalText> originalTextPage = originalTextService.list(catalogueId, entryId, title, page, size);
        for (OriginalText originalText : originalTextPage.getContent()) {
            HashMap<String, Object> map = new HashMap();
            map.put("id", originalText.getId());
            map.put("title", originalText.getTitle());
            map.put("type", originalText.getType());
            map.put("version", originalText.getVersion());
            map.put("name", originalText.getName());
            map.put("size", originalText.getSize());
            map.put("remark", originalText.getRemark());
            map.put("createTime", originalText.getCreateTime());
            map.put("contentIndexStatus", originalText.getContentIndexStatus());
            map.put("pdfConverStatus", originalText.getPdfConverStatus());
            list.add(map);
        }
        result.put("items", list);
        result.put("total", originalTextPage.getTotalElements());
        return result;
    }

    /**
     * @api {put} /originalText/sort 原文排序
     * @apiName sort
     * @apiGroup originalText
     * @apiParam {String} upId 上移原文ID（url参数）
     * @apiParam {String} downId 下移原文ID（url参数）
     * @apiParam {Number} catalogueId 目录ID（url参数）
     * @apiError (Error 400) message 原文记录不存在
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_file_read_' + #catalogueId)")
    @RequestMapping(value = "/sort", method = RequestMethod.PUT)
    public void sort(@RequestParam("upId") String upId, @RequestParam("downId") String downId, @RequestParam("catalogueId") int catalogueId) {
        originalTextService.sort(upId, downId, catalogueId);
    }

}
