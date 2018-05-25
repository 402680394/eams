package com.ztdx.eams.controller;

import com.ztdx.eams.domain.archives.application.OriginalTextService;
import com.ztdx.eams.domain.archives.model.OriginalText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
     * @apiParam {Number} archiveId 档案库ID(form-data参数)
     * @apiParam {Number} catalogueId 目录ID(form-data参数)
     * @apiParam {String} entryId 条目ID(form-data参数)
     * @apiParam {String} title 文件标题(form-data参数)
     * @apiParam {Number} type 文件类型(form-data参数)
     * @apiParam {Number} version 文件版本(form-data参数)
     * @apiParam {Number} remark 备注(form-data参数)
     * @apiParam {File} file 原文文件(form-data参数)
     * @apiError (Error 400) message 1.目录不存在;2.档案库不存在;3.条目不存在或已被删除.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(HttpServletRequest request, MultipartFile file) {
        OriginalText originalText = new OriginalText();
        originalText.setCatalogueId(Integer.parseInt(request.getParameter("catalogueId")));
        originalText.setEntryId(UUID.fromString(request.getParameter("entryId")));
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
}
