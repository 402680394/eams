package com.ztdx.eams.controller.archives;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/borrow")
public class BorrowController {

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
     * @apiError (Error 500) message
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_file_write_' + #request.getParameter(\"catalogueId\"))")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void apply() {

    }

}
