package com.ztdx.eams.controller.archives;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.domain.archives.application.BorrowService;
import com.ztdx.eams.domain.archives.application.DescriptionItemService;
import com.ztdx.eams.domain.archives.application.EntryService;
import com.ztdx.eams.domain.archives.model.Borrow;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.archives.model.Entry;
import com.ztdx.eams.domain.archives.model.PropertyType;
import com.ztdx.eams.domain.work.application.WorkService;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(value = "/borrow")
public class BorrowController {

    private final BorrowService borrowService;

    private final WorkService workService;

    private final EntryService entryService;

    private final DescriptionItemService descriptionItemService;

    @Autowired
    public BorrowController(BorrowService borrowService, WorkService workService, EntryService entryService, DescriptionItemService descriptionItemService) {
        this.borrowService = borrowService;
        this.workService = workService;
        this.entryService = entryService;
        this.descriptionItemService = descriptionItemService;
    }

    /**
     * @api {post} /borrow 借阅申请
     * @apiName apply
     * @apiGroup borrow
     * @apiParam {String} applicantName 申请人名称
     * @apiParam {String} department 部门
     * @apiParam {String} email 电子邮件
     * @apiParam {String} tel 电话
     * @apiParam {Number} days 借阅天数
     * @apiParam {String} objective 借阅目的（1-档案编研 2-工作考察 3-编史修志 4-学术研究 5-经济建设 6-宣传教育 7-其他）（取文本值）
     * @apiParam {Number} type 借阅类型（1-电子利用 2-实体外借 3-实体查阅）
     * @apiParam {Number} isSee 是否查看（0-否 1-是）
     * @apiParam {Number} isPrint 是否打印
     * @apiParam {Number} isDownload 是否下载
     * @apiParam {Number} isCopy 是否复印
     * @apiParam {Number} isHandwrite 是否手抄
     * @apiParam {String} descript 简要说明
     * @apiParam {Object[]} entryIndexs 条目索引 [{"catalogueId":1,"entryId":"3feda463-b785-4152-9cf8-f5969dba2bea"}]
     * @apiError (Error 500) message
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_file_search_' + #request.getParameter(\"catalogueId\"))")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void apply(@RequestBody Map<String, Object> map, HttpSession session) {
        //保存申请单信息
        Borrow borrow = new Borrow();
        borrow.setApplicantName((String) map.get("applicantName"));
        borrow.setDepartment((String) map.get("department"));
        borrow.setEmail((String) map.get("email"));
        borrow.setTel((String) map.get("tel"));
        borrow.setDays((int) map.get("days"));
        borrow.setObjective((String) map.get("objective"));
        borrow.setType((int) map.get("type"));
        borrow.setIsSee((int) map.get("isSee"));
        borrow.setIsPrint((int) map.get("isPrint"));
        borrow.setIsDownload((int) map.get("isDownload"));
        borrow.setIsCopy((int) map.get("isCopy"));
        borrow.setIsHandwrite((int) map.get("isHandwrite"));
        borrow.setDescript((String) map.get("descript"));
        borrow.setApplicantId(((UserCredential) session.getAttribute(UserCredential.KEY)).getUserId());
        borrow.setApplicationDate(new Date());
        borrow.setEffect("");

        borrowService.apply(borrow);

        //开启借阅审批流程

        //借阅单ID
        map.put("orderId", borrow.getId());
        //借阅单号
        map.put("orderCode", borrow.getCode());
        //申请人 日期
        map.put("applicantId", borrow.getApplicantId());
        map.put("applicationDate", borrow.getApplicationDate());
        //审批部门领导
        map.put("departmentLeader", 22);
        //流程类型
        map.put("type", "borrow");
        //审批状态
        map.put("status", "待审批");

        List<Map<String, Object>> entryIndexs = (List) map.get("entryIndexs");
        map.remove("entryIndexs");

        for (Map entryIndex : entryIndexs) {
            int catalogueId = (int) entryIndex.get("catalogueId");
            String entryId = (String) entryIndex.get("entryId");
            Entry entry = entryService.get(catalogueId, entryId);

            DescriptionItem titleItem = descriptionItemService.findByCatalogueIdAndPropertyType(catalogueId, PropertyType.Title);

            DescriptionItem referenceItem = descriptionItemService.findByCatalogueIdAndPropertyType(catalogueId, PropertyType.Reference);
            String title = "";
            if (titleItem != null) {
                title = entry.getItems().getOrDefault(titleItem.getMetadataName(), "").toString();
            }

            String reference = "";
            if (referenceItem != null) {
                reference = entry.getItems().getOrDefault(referenceItem.getMetadataName(), "").toString();
            }
            //提名
            map.put("title", title);
            //档号
            map.put("reference", reference);
            //借阅条目
            map.put("id", (String) entryIndex.get("entryId"));
            //审批档案员
            map.put("filer", 22);
            workService.startBorrow(map);
        }
    }

    /**
     * @api {get} /borrow/{id} 获取借阅单详情
     * @apiName details
     * @apiGroup borrow
     * @apiParam {Number} id 借阅单ID(url参数)
     * @apiSuccess (Success 200) {Number} id 借阅单ID.
     * @apiSuccess (Success 200) {String} code 借阅单编号.
     * @apiSuccess (Success 200) {Number} applicantDate 申请日期.
     * @apiSuccess (Success 200) {String} applicantName 申请人.
     * @apiSuccess (Success 200) {String} department 部门.
     * @apiSuccess (Success 200) {String} email 电子邮件.
     * @apiSuccess (Success 200) {String} tel 电话.
     * @apiSuccess (Success 200) {Number} days 借阅天数.
     * @apiSuccess (Success 200) {String} objective 借阅目的.
     * @apiSuccess (Success 200) {Number} type 借阅类型（1-电子利用 2-实体外借 3-实体查阅）.
     * @apiSuccess (Success 200) {Number} isSee 是否查看（0-否 1-是）.
     * @apiSuccess (Success 200) {Number} isPrint 是否打印.
     * @apiSuccess (Success 200) {Number} isDownload 是否下载.
     * @apiSuccess (Success 200) {Number} isCopy 是否复印.
     * @apiSuccess (Success 200) {Number} isHandwrite 是否手抄.
     * @apiSuccess (Success 200) {String} descript 简要说明.
     * @apiSuccess (Success 200) {String} effect 利用效果.
     * @apiSuccess (Success 200) {Object[]} borrowContent 借阅内容.
     * @apiSuccessExample {json} Success-Response:
     * {"data":
     *      {"id": 6,
     *      "code": "借阅单编号",
     *      "applicantDate": 1534385981,
     *      "applicantName": "申请人",
     *      "department": "部门",
     *      "email": "电子邮件",
     *      "tel": "电话",
     *      "days": 10,
     *      "objective": "借阅目的",
     *      "type": 1,
     *      "isSee": 1,
     *      "isPrint": 0,
     *      "isDownload": 0,
     *      "isCopy": 0,
     *      "isHandwrite": 0,
     *      "descript": "简要说明",
     *      "effect": "利用效果",
     *      "borrowContent": [{"processId": "审批流程ID","displayName": "显示名称"}]}}.
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Map<String, Object> details(@PathVariable("id") int borrowId) {
        return null;
    }

}
