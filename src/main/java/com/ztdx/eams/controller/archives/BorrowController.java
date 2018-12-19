package com.ztdx.eams.controller.archives;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.domain.archives.application.*;
import com.ztdx.eams.domain.archives.model.*;
import com.ztdx.eams.domain.system.application.PermissionService;
import com.ztdx.eams.domain.work.application.WorkService;
import com.ztdx.eams.domain.work.model.Workflow;
import com.ztdx.eams.domain.work.model.WorkflowCompleteEvent;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
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

    private final PermissionService permissionService;

    private final ArchivesService archivesService;

    private final OriginalTextService originalTextService;

    @Value("${borrow.departmentLeader}")
    private Integer departmentLeader;

    @Value("${borrow.archiveManager}")
    private Integer archiveManager;

    @Autowired
    public BorrowController(BorrowService borrowService, WorkService workService, EntryService entryService, DescriptionItemService descriptionItemService, PermissionService permissionService, ArchivesService archivesService, OriginalTextService originalTextService) {
        this.borrowService = borrowService;
        this.workService = workService;
        this.entryService = entryService;
        this.descriptionItemService = descriptionItemService;
        this.permissionService = permissionService;
        this.archivesService = archivesService;
        this.originalTextService = originalTextService;
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
     * @apiParam {String} objective 借阅目的（0-档案编研 1-工作考察 2-编史修志 3-学术研究 4-经济建设 5-宣传教育 6-其他）（取文本值）
     * @apiParam {Number} type 借阅类型（0-电子利用 1-实体外借 2-实体查阅）
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
        map.put("departmentLeader", departmentLeader);
        //流程类型
        map.put("type", "borrow");
        //审批时间
        map.put("approvalTime", "");
        //审批状态
        map.put("status", "pending");

        List<Map<String, Object>> entryIndexs = (List) map.get("entryIndexs");
        map.remove("entryIndexs");

        for (Map entryIndex : entryIndexs) {
            int catalogueId = (int) entryIndex.get("catalogueId");
            String entryId = (String) entryIndex.get("entryId");
            Entry entry = entryService.get(catalogueId, entryId);

            String archiveName = archivesService.findArchivesNameByCatalogue_CatalogueId(catalogueId);

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
            //档案库名称
            map.put("archiveName", archiveName);
            //提名
            map.put("title", title);
            //档号
            map.put("reference", reference);
            //借阅条目
            map.put("id", entryId);
            map.put("catalogueId", catalogueId);
            //审批档案员
            map.put("filer", archiveManager);
            workService.start("borrow", map);
        }
    }

    /**
     * @api {get} /borrow/{id} 获取借阅单详情
     * @apiName details
     * @apiGroup borrow
     * @apiParam {Number} id 借阅单ID
     * @apiParam {Number} size 借阅数据显示条数(url参数)
     * @apiParam {Number} page 借阅数据显示页次 (url参数)
     * @apiParam {Number} isApproval 是否审批页 (url参数)(0-查看页 1-审批页)
     * @apiSuccess (Success 200) {Number} id 借阅单ID.
     * @apiSuccess (Success 200) {String} code 借阅单编号.
     * @apiSuccess (Success 200) {Number} applicantDate 申请日期.
     * @apiSuccess (Success 200) {String} applicantName 申请人.
     * @apiSuccess (Success 200) {String} department 部门.
     * @apiSuccess (Success 200) {String} email 电子邮件.
     * @apiSuccess (Success 200) {String} tel 电话.
     * @apiSuccess (Success 200) {Number} days 借阅天数.
     * @apiSuccess (Success 200) {String} objective 借阅目的.
     * @apiSuccess (Success 200) {Number} type 借阅类型（0-电子利用 1-实体外借 2-实体查阅）.
     * @apiSuccess (Success 200) {Number} isSee 是否查看（0-否 1-是）.
     * @apiSuccess (Success 200) {Number} isPrint 是否打印.
     * @apiSuccess (Success 200) {Number} isDownload 是否下载.
     * @apiSuccess (Success 200) {Number} isCopy 是否复印.
     * @apiSuccess (Success 200) {Number} isHandwrite 是否手抄.
     * @apiSuccess (Success 200) {String} descript 简要说明.
     * @apiSuccess (Success 200) {String} effect 利用效果.
     * @apiSuccess (Success 200) {String} total 借阅内容总条数.
     * @apiSuccess (Success 200) {String} borrowContent 借阅内容.
     * @apiSuccess (Success 200) {String} borrowContent:taskId 任务ID(审批页返回).
     * @apiSuccess (Success 200) {String} borrowContent:archiveName 档案库名称.
     * @apiSuccess (Success 200) {String} borrowContent:title 提名.
     * @apiSuccess (Success 200) {String} borrowContent:reference 档号.
     * @apiSuccess (Success 200) {String} borrowContent:approvalTime 审批时间.
     * @apiSuccess (Success 200) {String} borrowContent:status 审批状态.
     * @apiSuccess (Success 200) {Number} borrowContent:days 天数.
     * @apiSuccessExample {json} Success-Response:
     * {"data":
     * {"id": 6,
     * "code": "借阅单编号",
     * "applicantDate": 1534385981,
     * "applicantName": "申请人",
     * "department": "部门",
     * "email": "电子邮件",
     * "tel": "电话",
     * "days": 10,
     * "objective": "借阅目的",
     * "type": 1,
     * "isSee": 1,
     * "isPrint": 0,
     * "isDownload": 0,
     * "isCopy": 0,
     * "isHandwrite": 0,
     * "descript": "简要说明",
     * "effect": "利用效果",
     * "total": 10,
     * "borrowContent": [{"processId": "审批流程ID","archiveName": "档案库名称","title": "提名","reference": "档号","approvalTime": "审批时间","status": "审批状态","days": 10}]}}.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> details(@PathVariable("id") int borrowId
            , @RequestParam(name = "size", defaultValue = "15") int size
            , @RequestParam(name = "page", defaultValue = "1") int page
            , @RequestParam("isApproval") int isApproval) {
        Map<String, Object> resultMap = null;
        //查看
        if (isApproval == 0) {
            resultMap = workService.queryAllBorrowData(borrowId, size, page);
        } else {
            resultMap = workService.queryApprovalBorrowData(borrowId, size, page);
        }
        Borrow borrow = borrowService.get(borrowId);
        resultMap.put("id", borrow.getId());
        resultMap.put("code", borrow.getCode());
        resultMap.put("applicantDate", borrow.getApplicationDate());
        resultMap.put("applicantName", borrow.getApplicantName());
        resultMap.put("department", borrow.getDepartment());
        resultMap.put("email", borrow.getEmail());
        resultMap.put("tel", borrow.getTel());
        resultMap.put("days", borrow.getDays());
        resultMap.put("objective", borrow.getObjective());
        resultMap.put("type", borrow.getType());
        resultMap.put("isSee", borrow.getIsSee());
        resultMap.put("isPrint", borrow.getIsPrint());
        resultMap.put("isDownload", borrow.getIsDownload());
        resultMap.put("isCopy", borrow.getIsCopy());
        resultMap.put("isHandwrite", borrow.getIsHandwrite());
        resultMap.put("descript", borrow.getDescript());
        resultMap.put("effect", borrow.getEffect());
        return resultMap;
    }

    //@Async
    @Transactional
    @EventListener
    public void completeEvent(WorkflowCompleteEvent event) {
        if (event == null
                || event.getWorkflow() == null
                || !StringUtils.isNumeric(event.getWorkflow().getOrderId())
                || event.getWorkflow().getApplicantId() == null
                || event.getWorkflow().getOid() == null
                || event.getWorkflow().getRawVars() == null
                || event.getWorkflow().getRawVars().get("catalogueId") == null
                || event.getWorkflow().getStatus() == null
                || event.getWorkflow().getStatus() != Workflow.WorkflowResult.agree) {
            return;
        }

        int userId = event.getWorkflow().getApplicantId();

        Borrow borrow = borrowService.get(Integer.parseInt(event.getWorkflow().getOrderId()));

        Object tmpCatalogueId = event.getWorkflow().getRawVars().get("catalogueId");
        if (!StringUtils.isNumeric(tmpCatalogueId.toString())
                || borrow == null
                || borrow.getType() != 0){
            return;
        }

        List<String> urlResources = new ArrayList<>();
        int count;

        do {
            Page<OriginalText> pages = originalTextService.list(
                    Integer.parseInt(tmpCatalogueId.toString()), event.getWorkflow().getOid(), "", 0, 100);
            pages.getContent().forEach(a -> {
                if (borrow.getIsSee() == 1) {
                    urlResources.add(String.format("object_original_text_view_%s", a.getId()));
                }
                if (borrow.getIsDownload() == 1) {
                    urlResources.add(String.format("object_original_text_download_%s", a.getId()));
                }
                if (borrow.getIsPrint() == 1) {
                    urlResources.add(String.format("object_original_text_print_%s", a.getId()));
                }
            });
            count = pages.getNumber();
        }while (count > 0);

        permissionService.addUserPermission(
                userId
                , urlResources
                , borrow.getDays());
    }

    /**
     * @api {post} /borrow/approval 审批
     * @apiName approval
     * @apiGroup borrow
     * @apiParam {String[]} taskId 流程ID
     * @apiParam {Number} isAgree 是否同意（0-同意 1-拒绝）
     * @apiParam {String} Opinion 审批意见
     */
    @RequestMapping(value = "/approval", method = RequestMethod.POST)
    public void approval(@RequestBody Map<String, Object> map) {
        int isAgree = (int) map.get("isAgree");
        List<String> taskId = (List<String>) map.get("taskId");
        if (isAgree == 0) {
            workService.agree(taskId);
        } else {
            workService.refuse(taskId);
        }
    }
}
