package com.ztdx.eams.controller;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.domain.archives.application.DescriptionItemService;
import com.ztdx.eams.domain.archives.application.EntryService;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.archives.model.Entry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "/entry")
public class EntryController {

    private EntryService entryService;

    private DescriptionItemService descriptionItemService;

    public EntryController(EntryService entryService, DescriptionItemService descriptionItemService) {
        this.entryService = entryService;
        this.descriptionItemService = descriptionItemService;
    }


    /**
     * @api {post} /entry 新增条目
     * @apiName add_entry
     * @apiGroup entry
     * @apiParam {String} catalogueId 档案目录id
     * @apiParam {Object} items 条目详细内容，是一个动态的key-value数组。
     * 以下举例条目有姓名(name)年龄(age)注册日期(regDate)爱好(interest)
     * @apiParam {String} items.name 姓名
     * @apiParam {Number} items.age 年龄
     * @apiParam {Date} items.regDate 注册日期
     * @apiParam {String[]} items.interest 爱好
     * @apiParamExample {json} Request-Example:
     * {
     *     "catalogueId":1,
     *     "items":{
     *         "name":"姓名",
     *         "age":41,
     *         "birthday":"2018-05-09T00:00:00",
     *         "amount":73824039.1873,
     *         "aihao":[
     *             "电影",
     *             "汽车"
     *         ]
     *     }
     * }
     * @apiError (Error 400) message 1.档案目录不存在 2.其他数据验证错误
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody Entry entry, HttpSession session) {
        descriptionItemService.addVerification(entry, session);
        UserCredential userCredential = (UserCredential) session.getAttribute("LOGIN_USER");
        entry.setOwner(userCredential.getUserId());
        entryService.save(entry);
    }

    /**
     * @api {put} /entry/{id} 更新条目
     * @apiName update_entry
     * @apiGroup entry
     * @apiParam {UUID} id 条目id（url占位符）
     * @apiParam {Object} items 条目详细内容，是一个动态的key-value数组。
     * 以下举例条目有姓名(name)年龄(age)注册日期(regDate)爱好(interest)
     * @apiParam {String} items.name 姓名
     * @apiParam {Number} items.age 年龄
     * @apiParam {Date} items.regDate 注册日期
     * @apiParam {String[]} items.interest 爱好
     * @apiParamExample {json} Request-Example:
     * {
     *     "items":{
     *         "name":"姓名",
     *         "age":41,
     *         "birthday":"2018-05-09T00:00:00",
     *         "amount":73824039.1873,
     *         "aihao":[
     *             "电影",
     *             "汽车"
     *         ]
     *     }
     * }
     * @apiError (Error 400) message 1.档案目录不存在 2.其他数据验证错误
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void update(@PathVariable("id") UUID uuid, @RequestBody Entry entry, HttpSession session) {
        entry.setId(uuid);
        descriptionItemService.updateVerification(entry, session);
        entryService.update(entry);
    }

    /**
     * @api {get} /entry/search?cid={cid}&q={q}&page={page}&size={size} 按关键字搜索条目
     * @apiName search_simple
     * @apiGroup entry
     * @apiParam {Number} cid 目录id(QueryString)
     * @apiParam {String} q 关键字(QueryString)
     * @apiParam {Number} page 页码(QueryString)
     * @apiParam {Number} size 页行数(QueryString)
     * @apiSuccess (Success 200) {Array} content 列表内容
     * @apiSuccess (Success 200) {Number} content.id 条目id
     * @apiSuccess (Success 200) {Number} content.catalogueId 目录id
     * @apiSuccess (Success 200) {Number=1,2,3} content.catalogueType 目录类型 1:卷内 2:案卷 3:项目
     * @apiSuccess (Success 200) {Number} content.archiveId 档案库id
     * @apiSuccess (Success 200) {String} content.archiveName 档案库名称
     * @apiSuccess (Success 200) {Number=1,2} content.archiveType 档案库类型 1:登记库 2:归档库
     * @apiSuccess (Success 200) {Number} content.archiveContentType 档案库内容类型
     * @apiSuccess (Success 200) {Array} content.items 条目字段信息(以下内容每个档案库目录都不同，字段定义在data.column中)
     * @apiSuccess (Success 200) {date} content.items.birthday 生日
     * @apiSuccess (Success 200) {double} content.items.amount 资产
     * @apiSuccess (Success 200) {Array} content.items.aihao 爱好
     * @apiSuccess (Success 200) {String} content.items.name 姓名
     * @apiSuccess (Success 200) {int} content.items.age 年龄
     * @apiSuccess (Success 200) {Object} column 条目字段定义(以下内容每个档案库目录都不同，用来描述content.items中字段的定义)
     * @apiSuccess (Success 200) {Object} [birthday] 生日字段的属性
     * @apiSuccess (Success 200) {int} metadataId 字段id
     * @apiSuccess (Success 200) {String} metadataName 字段名称
     * @apiSuccess (Success 200) {String} displayName 字段显示名称
     * @apiSuccess (Success 200) {Object} [amount] 资产字段的属性
     * @apiSuccess (Success 200) {int} metadataId 字段id
     * @apiSuccess (Success 200) {String} metadataName 字段名称
     * @apiSuccess (Success 200) {String} displayName 字段显示名称
     * @apiSuccess (Success 200) {Object} [aihao] 爱好字段的属性
     * @apiSuccess (Success 200) {int} metadataId 字段id
     * @apiSuccess (Success 200) {String} metadataName 字段名称
     * @apiSuccess (Success 200) {String} displayName 字段显示名称
     * @apiSuccess (Success 200) {Object} [name] 姓名字段的属性
     * @apiSuccess (Success 200) {int} metadataId 字段id
     * @apiSuccess (Success 200) {String} metadataName 字段名称
     * @apiSuccess (Success 200) {String} displayName 字段显示名称
     * @apiSuccess (Success 200) {Object} [age] 年龄字段的属性
     * @apiSuccess (Success 200) {int} metadataId 字段id
     * @apiSuccess (Success 200) {String} metadataName 字段名称
     * @apiSuccess (Success 200) {String} displayName 字段显示名称
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "data":{
     *         "content":[
     *             {
     *                 "id":"322c3c75-08a5-4a01-a60f-084b6a6f9be9",
     *                 "catalogueId":1,
     *                 "catalogueType":1,
     *                 "archiveId":0,
     *                 "archiveName":"一文一件库",
     *                 "archiveType":0,
     *                 "archiveContentType":0,
     *                 "items":{
     *                     "birthday":"2018-05-16T14:44:56.328+0800",
     *                     "amount":73824039.1873,
     *                     "aihao":[
     *                         "电影",
     *                         "足球",
     *                         "汽车"
     *                     ],
     *                     "name":"里斯1",
     *                     "age":41
     *                 },
     *                 "gmtCreate":"2018-05-16T14:44:56.328+0800",
     *                 "gmtModified":"2018-05-16T14:44:56.328+0800"
     *             }
     *         ],
     *         "column":{
     *             "name": {
     *                 "metadataId":1,
     *                 "metadataName":"name",
     *                 "displayName":"姓名"
     *             },
     *             "birthday": {
     *                 "metadataId":2,
     *                 "metadataName":"birthday",
     *                 "displayName":"生日"
     *             },
     *             "amount": {
     *                 "metadataId":3,
     *                 "metadataName":"amount",
     *                 "displayName":"资产"
     *             },
     *             "aihao": {
     *                 "metadataId":4,
     *                 "metadataName":"aihao",
     *                 "displayName":"爱好"
     *             },
     *             "age": {
     *                 "metadataId":5,
     *                 "metadataName":"age",
     *                 "displayName":"年龄"
     *             }
     *         },
     *         "totalElements": 14,
     *         "totalPages": 1
     *     }
     * }
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public Map<String, Object> search(@RequestParam("cid") int catalogueId, @RequestParam("q") String queryString, @RequestParam("page") int page, @RequestParam("size") int size){
        //TODO 登记库只能查看自己的
        Page<Entry> content =  entryService.search(catalogueId, queryString, new Hashtable<>(), PageRequest.of(page, size));

        Map<String, DescriptionItem> list = descriptionItemService.list(catalogueId);

        Map<String, Object> result = new HashMap<>();
        result.put("content", content.getContent());
        result.put("column", list);
        result.put("totalElements", content.getTotalElements());
        result.put("totalPages", content.getTotalPages());
        return result;
    }

    /**
     * @api {post} /entry/search?q={q}&page={page}&size={size} 按关键字搜索条目，高级搜索
     * @apiName search_adv
     * @apiGroup entry
     * @apiParam {String} q 关键字(QueryString)
     * @apiParam {Number} page 页码(QueryString)
     * @apiParam {Number} size 页行数(QueryString)
     * @apiParam {String} catalogueId 档案目录id
     * @apiParam {Object} items 条目详细内容，是一个动态的key-value数组。
     * 以下举例条目有姓名(name)年龄(age)注册日期(regDate)爱好(interest)
     * @apiParam {String} items.name 姓名
     * @apiParam {Array} items.age 年龄 传递整数进行精确查找，传递数组[from,to]进行区间查找
     * @apiParam {Array} items.regDate 注册日期 传递数组[from,to]进行区间查找
     * @apiParam {Array} items.interest 爱好 传递数组进行多词匹配。
     * @apiSuccess (Success 200) {Array} content 列表内容
     * @apiSuccess (Success 200) {Number} content.id 条目id
     * @apiSuccess (Success 200) {Number} content.catalogueId 目录id
     * @apiSuccess (Success 200) {Number=1,2,3} content.catalogueType 目录类型 1:卷内 2:案卷 3:项目
     * @apiSuccess (Success 200) {Number} content.archiveId 档案库id
     * @apiSuccess (Success 200) {String} content.archiveName 档案库名称
     * @apiSuccess (Success 200) {Number=1,2} content.archiveType 档案库类型 1:登记库 2:归档库
     * @apiSuccess (Success 200) {Number} content.archiveContentType 档案库内容类型
     * @apiSuccess (Success 200) {Array} content.items 条目字段信息(以下内容每个档案库目录都不同，字段定义在data.column中)
     * @apiSuccess (Success 200) {date} content.items.birthday 生日
     * @apiSuccess (Success 200) {double} content.items.amount 资产
     * @apiSuccess (Success 200) {Array} content.items.aihao 爱好
     * @apiSuccess (Success 200) {String} content.items.name 姓名
     * @apiSuccess (Success 200) {Number} content.items.age 年龄
     * @apiSuccess (Success 200) {Array} column 条目字段定义(以下内容每个档案库目录都不同，用来描述content.items中字段的定义)
     * @apiSuccess (Success 200) {Number} column.metadataId 元数据id
     * @apiSuccess (Success 200) {String} column.metadataName
     * @apiSuccess (Success 200) {Array} column.displayName 爱好
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "data":{
     *         "content":[
     *             {
     *                 "id":"322c3c75-08a5-4a01-a60f-084b6a6f9be9",
     *                 "catalogueId":1,
     *                 "catalogueType":1,
     *                 "archiveId":0,
     *                 "archiveName":"一文一件库",
     *                 "archiveType":0,
     *                 "archiveContentType":0,
     *                 "items":{
     *                     "birthday":"2018-05-16T14:44:56.328+0800",
     *                     "amount":73824039.1873,
     *                     "aihao":[
     *                         "电影",
     *                         "足球",
     *                         "汽车"
     *                     ],
     *                     "name":"里斯1",
     *                     "age":41
     *                 },
     *                 "gmtCreate":"2018-05-16T14:44:56.328+0800",
     *                 "gmtModified":"2018-05-16T14:44:56.328+0800"
     *             }
     *         ],
     *         "column":[
     *             {
     *                 "metadataId":1,
     *                 "metadataName":"name",
     *                 "displayName":"姓名"
     *             },
     *             {
     *                 "metadataId":2,
     *                 "metadataName":"birthday",
     *                 "displayName":"生日"
     *             },
     *             {
     *                 "metadataId":3,
     *                 "metadataName":"amount",
     *                 "displayName":"资产"
     *             },
     *             {
     *                 "metadataId":4,
     *                 "metadataName":"aihao",
     *                 "displayName":"爱好"
     *             },
     *             {
     *                 "metadataId":5,
     *                 "metadataName":"age",
     *                 "displayName":"年龄"
     *             }
     *         ],
     *         "totalElements":14
     *     }
     * }
     */
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public Page<Entry> searchAdv(@RequestBody Entry entry, @RequestParam("q") String queryString, @RequestParam("page") int page, @RequestParam("size") int size) {
        //, @RequestParam("cid") int catalogueId, @RequestParam("q") String queryString, @RequestParam("page") int page, @RequestParam("size") int size
        return entryService.search(entry.getCatalogueId(), queryString, entry.getItems(), PageRequest.of(page, size));
    }

    /**
     * @api {post} /entry/searchFulltext?page={page}&size={size} 全文搜索
     * @apiName searchFulltext
     * @apiGroup entry
     * @apiParam {Number} page 页码(QueryString)
     * @apiParam {Number} size 页行数(QueryString)
     * @apiParam {Array} archiveContentType 档案内容类型
     * @apiParam {String="3year","6year","8year"} dateRange 时间
     * @apiParam {Array="words","entry","file"} searchParam 搜索参数 words:全词匹配 entry:条目 file:全文
     * @apiParam {String} includeWords 包含关键字
     * @apiParam {String} rejectWords 排除关键字
     * @apiSuccess (Success 200) {Array} content 列表内容
     * @apiSuccess (Success 200) {Number} content.id 条目id
     * @apiSuccess (Success 200) {Number} content.catalogueId 目录id
     * @apiSuccess (Success 200) {Number=1,2,3} content.catalogueType 目录类型 1:卷内 2:案卷 3:项目
     * @apiSuccess (Success 200) {Number} content.archiveId 档案库id
     * @apiSuccess (Success 200) {String} content.archiveName 档案库名称
     * @apiSuccess (Success 200) {String} content.fondsName 全宗名称
     * @apiSuccess (Success 200) {Number=1,2} content.archiveType 档案库类型 1:登记库 2:归档库
     * @apiSuccess (Success 200) {Number} content.archiveContentType 档案库内容类型
     * @apiSuccess (Success 200) {Object} content.items 条目字段信息(以下内容每个档案库目录都不同，字段定义在data.column中)
     * @apiSuccess (Success 200) {date} content.items.birthday 生日
     * @apiSuccess (Success 200) {double} content.items.amount 资产
     * @apiSuccess (Success 200) {Array} content.items.aihao 爱好
     * @apiSuccess (Success 200) {String} content.items.name 姓名
     * @apiSuccess (Success 200) {Number} content.items.age 年龄
     * @apiSuccess (Success 200) {Object} content.file 文件信息
     * @apiSuccess (Success 200) {Number} content.file.fileId 文件id
     * @apiSuccess (Success 200) {Number} content.file.title 标题
     * @apiSuccess (Success 200) {String="word","excel","pdf","ppt"} content.file.fileType 类型
     * @apiSuccess (Success 200) {String} content.file.hightlight 高亮文本
     * @apiSuccess (Success 200) {Array} column 条目字段定义(以下内容每个档案库目录都不同，用来描述content.items中字段的定义)
     * @apiSuccess (Success 200) {Number} column.metadataId 元数据id
     * @apiSuccess (Success 200) {String} column.metadataName
     * @apiSuccess (Success 200) {Array} column.displayName 爱好
     * @apiSuccess (Success 200) {Object} aggregation 统计数据
     * @apiSuccess (Success 200) {Array} aggregation.archiveContentType 档案类型统计
     * @apiSuccess (Success 200) {Number} aggregation.archiveContentType.id 档案类型id
     * @apiSuccess (Success 200) {Number} aggregation.archiveContentType.name 档案类型名称
     * @apiSuccess (Success 200) {Number} aggregation.archiveContentType.count 数量
     * @apiSuccess (Success 200) {Number} totalElements 总记录数
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "data":{
     *         "content":[
     *             {
     *                 "id":"322c3c75-08a5-4a01-a60f-084b6a6f9be9",
     *                 "catalogueId":1,
     *                 "catalogueType":1,
     *                 "archiveId":0,
     *                 "archiveName":"一文一件库",
     *                 "fondsName":"测试全宗",
     *                 "archiveType":0,
     *                 "archiveContentType":0,
     *                 "items":{
     *                     "birthday":"2018-05-16T14:44:56.328+0800",
     *                     "amount":73824039.1873,
     *                     "aihao":[
     *                         "电影",
     *                         "足球",
     *                         "汽车"
     *                     ],
     *                     "name":"里斯1",
     *                     "age":41
     *                 },
     *                 "file":{
     *                     "fileId":1,
     *                     "title":"这是一个文件标题",
     *                     "fileType":"word",
     *                     "hightlight":"这是高亮文本内容"
     *                 },
     *                 "gmtCreate":"2018-05-16T14:44:56.328+0800",
     *                 "gmtModified":"2018-05-16T14:44:56.328+0800"
     *             }
     *         ],
     *         "column":[
     *             {
     *                 "metadataId":1,
     *                 "metadataName":"name",
     *                 "dispalyName":"姓名"
     *             },
     *             {
     *                 "metadataId":2,
     *                 "metadataName":"birthday",
     *                 "dispalyName":"生日"
     *             },
     *             {
     *                 "metadataId":3,
     *                 "metadataName":"amount",
     *                 "dispalyName":"资产"
     *             },
     *             {
     *                 "metadataId":4,
     *                 "metadataName":"aihao",
     *                 "dispalyName":"爱好"
     *             },
     *             {
     *                 "metadataId":5,
     *                 "metadataName":"age",
     *                 "dispalyName":"年龄"
     *             }
     *         ],
     *         "aggregations":{
     *             "archiveContentType":[
     *                 {
     *                     "id":1,
     *                     "name":"文书档案",
     *                     "count":10
     *                 }
     *             ]
     *         },
     *         "totalElements":14
     *     }
     * }
     */
    @RequestMapping(value = "/searchFulltext", method = RequestMethod.POST)
    public void searchFulltext() {

    }

    /**
     * @api {post} /entry/searchFullArchive 全库搜索
     * @apiName searchFullArchive
     * @apiGroup entry
     * @apiParam {Array} [catalogueIds] 目录id列表
     * @apiParam {Array} [archiveContentType] 档案内容类型
     * @apiParam {String="3year","6year","8year"} [dateRange] 时间
     * @apiParam {String} [keyWord] 关键字
     * @apiSuccess (Success 200) {Array} content 列表内容
     * @apiSuccess (Success 200) {Number} content.archiveId 档案库id
     * @apiSuccess (Success 200) {String} content.archiveName 档案库名称
     * @apiSuccess (Success 200) {String} content.fondsName 全宗名称
     * @apiSuccess (Success 200) {Array} content.items 目录数组
     * @apiSuccess (Success 200) {Number} content.items.catalogueId 目录id
     * @apiSuccess (Success 200) {Number=1,2,3,4} content.items.catalogueType 目录类型 1:一文一件 2:传统立卷案卷 3:传统立卷卷内 4:项目
     * @apiSuccess (Success 200) {Number} content.items.count 统计结果
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "data": {
     *         "content": [
     *             {
     *                 "archiveId": 1,
     *                 "archiveName": "一文一件库",
     *                 "fondsName": "测试全宗",
     *                 "items":[
     *                     {
     *                         "catalogueId": 1,
     *      *                  "catalogueType": 1,
     *                         "count": 1
     *                     }
     *                 ]
     *             }
     *         ]
     *     }
     * }
     */
    @RequestMapping(value = "/searchFullArchive", method = RequestMethod.POST)
    public void searchFullArchive() {

    }
}
