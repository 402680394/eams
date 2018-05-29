package com.ztdx.eams.controller;

import com.ztdx.eams.domain.archives.application.OriginalTextService;
import com.ztdx.eams.domain.archives.model.OriginalText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by li on 2018/5/23.
 */
@RestController
@RequestMapping(value = "/originalText")
public class OriginalTextController {

    private final OriginalTextService originalTextService;

    @Autowired
    public OriginalTextController(OriginalTextService originalTextService) {
        this.originalTextService = originalTextService;
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
     * @apiError (Error 400) message 1.目录不存在;2.档案库不存在;3.条目不存在或已被删除;4.原文文件未上传.
     * @apiError (Error 500) message 1.文件上传失败;2.连接ftp服务异常;3.ftp服务未正常关闭.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(HttpServletRequest request, MultipartFile file) {
        OriginalText originalText = new OriginalText();
        originalText.setCatalogueId(Integer.parseInt(request.getParameter("catalogueId")));
        originalText.setEntryId(request.getParameter("entryId"));
        originalText.setTitle(request.getParameter("title"));
        originalText.setType(Integer.parseInt(request.getParameter("type")));
        originalText.setVersion(request.getParameter("version"));
        originalText.setRemark(request.getParameter("remark"));
        originalTextService.save(originalText, file);
    }

    /**
     * @api {delete} /originalText 删除原文
     * @apiName deleteBatch
     * @apiGroup originalText
     * @apiParam {String} id ID
     * @apiParam {Number} catalogueId 目录ID
     * @apiParam {String} entryId 条目ID
     * @apiParamExample Request-Example:[{"id":"d500af0c-a136-4e43-bd0c-66dc6a4304e8","entryId":"9fba8809-37b4-4b21-8e79-af9325dcb56a","catalogueId":1}]
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public void deleteBatch(@RequestBody List<Map<String, Object>> list) {
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
     * @apiError (Error 400) message 1.目录不存在;2.档案库不存在;3.条目不存在或已被删除;4.原文文件未上传.
     * @apiError (Error 500) message 1.文件上传失败;2.连接ftp服务异常;3.ftp服务未正常关闭.
     * @apiUse ErrorExample
     */
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
        originalTextService.update(originalText, file);
    }

    /**
     * @api {get} /originalText/fileAttributes 获取原文文件属性
     * @apiName fileAttributes
     * @apiGroup originalText
     * @apiParam {Number} catalogueId 目录ID(url参数)
     * @apiParam {String} entryId 条目ID(url参数)
     * @apiParam {String} id 原文ID(url参数)
     * @apiSuccess (Success 200) {Object} data 原文文件属性,动态key-value对象.
     * @apiSuccess (Success 200) {String} data.标题 原文文件属性示例.
     * @apiSuccess (Success 200) {String} data.主题 原文文件属性示例.
     * @apiSuccess (Success 200) {String} data.像素 原文文件属性示例.
     * @apiError (Error 400) message 1.目录不存在;2.档案库不存在;3.条目不存在或已被删除.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/fileAttributes", method = RequestMethod.PUT)
    public Map<String, Object> fileAttributes(@RequestParam("catalogueId") int catalogueId, @RequestParam("entryId") String entryId, @RequestParam("id") String id) {
        return originalTextService.fileAttributes(catalogueId, entryId, id);
    }

    /**
     * @api {get} /originalText 获取原文信息
     * @apiName get
     * @apiGroup originalText
     * @apiParam {Number} catalogueId 目录ID(url参数)
     * @apiParam {String} entryId 条目ID(url参数)
     * @apiParam {String} id 原文ID(url参数)
     * @apiSuccess (Success 200) {String} id ID.
     * @apiSuccess (Success 200) {String} title 标题.
     * @apiSuccess (Success 200) {String} type 类型(可选值：1-正文；2-改稿；3-副本).
     * @apiSuccess (Success 200) {String} version 版本.
     * @apiSuccess (Success 200) {String} remark 备注.
     * @apiError (Error 400) message 1.目录不存在;2.档案库不存在;3.条目不存在或已被删除.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Map<String, Object> get(@RequestParam("catalogueId") int catalogueId, @RequestParam("entryId") String entryId, @RequestParam("id") String id) {
        return originalTextService.get(catalogueId, id);
    }

    /**
     * @api {get} /originalText/download 下载原文文件
     * @apiName download
     * @apiGroup originalText
     * @apiParam {Number} catalogueId 目录ID(url参数)
     * @apiParam {String} entryId 条目ID(url参数)
     * @apiParam {String} id 原文ID(url参数)
     * @apiError (Error 400) message 1.目录不存在;2.档案库不存在;3.条目不存在或已被删除.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(@RequestParam("catalogueId") int catalogueId, @RequestParam("entryId") String entryId, @RequestParam("id") String id, HttpServletResponse response) {
        originalTextService.fileDownload(catalogueId, entryId, id, response);
    }
}
