package com.ztdx.eams.controller;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.exception.NotFoundException;
import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.archives.application.*;
import com.ztdx.eams.domain.archives.model.*;
import com.ztdx.eams.domain.system.application.FondsService;
import com.ztdx.eams.domain.system.model.Fonds;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/entry")
public class EntryController {

    private EntryService entryService;

    private DescriptionItemService descriptionItemService;

    private CatalogueService catalogueService;

    private ArchivesService archivesService;

    private ArchivesGroupService archivesGroupService;

    private FondsService fondsService;

    public EntryController(EntryService entryService, DescriptionItemService descriptionItemService, CatalogueService catalogueService, ArchivesService archivesService, ArchivesGroupService archivesGroupService, FondsService fondsService) {
        this.entryService = entryService;
        this.descriptionItemService = descriptionItemService;
        this.catalogueService = catalogueService;
        this.archivesService = archivesService;
        this.archivesGroupService = archivesGroupService;
        this.fondsService = fondsService;
    }

    /**
     * @api {get} /entry/{id} 获取条目详细信息
     * @apiName get_entry
     * @apiGroup entry
     * @apiParam {String} id 条目id (Path变量)
     * @apiSuccess (Success 200) {Number} id 条目id
     * @apiSuccess (Success 200) {Number} catalogueId 目录id
     * @apiSuccess (Success 200) {Number=1,2,3} catalogueType 目录类型 1:一文一件 2:传统立卷案卷 3:传统立卷卷内 4:项目
     * @apiSuccess (Success 200) {Number} archiveId 档案库id
     * @apiSuccess (Success 200) {Number=1,2} archiveType 档案库类型 1:登记库 2:归档库
     * @apiSuccess (Success 200) {Number} archiveContentType 档案库内容类型
     * @apiSuccess (Success 200) {Array} items 条目字段信息(以下内容每个档案库目录都不同，字段定义在data.column中)
     * @apiSuccess (Success 200) {date} items.birthday 生日
     * @apiSuccess (Success 200) {double} items.amount 资产
     * @apiSuccess (Success 200) {Array} items.aihao 爱好
     * @apiSuccess (Success 200) {String} items.name 姓名
     * @apiSuccess (Success 200) {int} items.age 年龄
     * @apiSuccessExample {json} Response-Example:
     * {
     *     "id":"322c3c75-08a5-4a01-a60f-084b6a6f9be9",
     *     "catalogueId":1,
     *     "catalogueType":1,
     *     "archiveId":0,
     *     "archiveType":0,
     *     "archiveContentType":0,
     *     "items":{
     *         "birthday":"2018-05-16T14:44:56.328+0800",
     *         "amount":73824039.1873,
     *         "aihao":[
     *             "电影",
     *             "足球",
     *             "汽车"
     *         ],
     *         "name":"里斯1",
     *         "age":41
     *     },
     *     "gmtCreate":"2018-05-16T14:44:56.328+0800",
     *     "gmtModified":"2018-05-16T14:44:56.328+0800"
     * }
     * @apiError (Error 404) message 1.条目不存在
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Entry get(@PathVariable("id") String id){
        Entry entry = entryService.get(id);
        if (entry == null){
            throw new NotFoundException("条目不存在");
        }
        return entry;
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
    public void save(@RequestBody Entry entry, @SessionAttribute UserCredential LOGIN_USER) {
        //descriptionItemService.addVerification(entry, LOGIN_USER);
        entry.setOwner(LOGIN_USER.getUserId());
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
    public void update(@PathVariable("id") String uuid, @RequestBody Entry entry, @SessionAttribute UserCredential LOGIN_USER) {
        entry.setId(uuid);
        descriptionItemService.updateVerification(entry, LOGIN_USER);
        entryService.update(entry);
    }

    /**
     * @api {delete} /entry 删除条目
     * @apiName delete_entry
     * @apiGroup entry
     * @apiParam {Number} cid 档案目录id
     * @apiParam {String[]} deletes 要删除的条目id数组
     * @apiParamExample {json} Request-Example:
     * {
     *     "cid": 1,
     *     "deletes": [
     *         "322c3c75-08a5-4a01-a60f-084b6a6f9be9"
     *     ]
     * }
     * @apiError (Error 400) message 1.档案目录不存在
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_delete_' + #cid)")
    public void delete(@JsonParam() int cid, @JsonParam List<String> deletes){
        Catalogue catalogue = catalogueService.get(cid);
        if (catalogue == null){
            throw new InvalidArgumentException("档案目录不存在");
        }
        entryService.delete(cid, deletes);
    }

    /**
     * @api {get} /entry/search?cid={cid}&q={q}&page={page}&size={size} 按关键字搜索条目
     * @apiName search_simple
     * @apiGroup entry
     * @apiParam {Number} cid 目录id(QueryString)
     * @apiParam {String} [q] 关键字(QueryString)
     * @apiParam {Number} [page] 页码(QueryString)
     * @apiParam {Number} [size] 页行数(QueryString)
     * @apiSuccess (Success 200) {Array} content 列表内容
     * @apiSuccess (Success 200) {Number} content.id 条目id
     * @apiSuccess (Success 200) {Number} content.catalogueId 目录id
     * @apiSuccess (Success 200) {Number=1,2,3} content.catalogueType 目录类型 1:一文一件 2:传统立卷案卷 3:传统立卷卷内 4:项目
     * @apiSuccess (Success 200) {Number} content.archiveId 档案库id
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
    public Map<String, Object> search(@RequestParam("cid") int catalogueId
            , @RequestParam(value = "q", required = false, defaultValue = "") String queryString
            , @RequestParam(value = "page", required = false, defaultValue = "0") int page
            , @RequestParam(value ="size", required = false, defaultValue = "20") int size){
        //TODO @lijie 登记库只能查看自己的
        Page<Entry> content =  entryService.search(catalogueId, queryString, new Hashtable<>(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "gmtDeleted")));

        Map<String, Object> list = descriptionItemService.list(catalogueId, a -> {
            Map<String, Object> result = new HashMap<>();
            result.put("metadataId", a.getMetadataId());
            result.put("metadataName", a.getMetadataName());
            result.put("displayName", a.getDisplayName());
            return result;
        });

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
     * @apiSuccess (Success 200) {Number=1,2,3} content.catalogueType 目录类型 1:一文一件 2:传统立卷案卷 3:传统立卷卷内 4:项目
     * @apiSuccess (Success 200) {Number} content.archiveId 档案库id
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
        return entryService.search(entry.getCatalogueId(), queryString, entry.getItems(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "gmtDeleted")));
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
     * @apiSuccess (Success 200) {Number=1,2,3} content.catalogueType 目录类型 1:一文一件 2:传统立卷案卷 3:传统立卷卷内 4:项目
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
     *                         "catalogueType": 1,
     *                         "count": 1
     *                     }
     *                 ]
     *             }
     *         ]
     *     }
     * }
     */
    @RequestMapping(value = "/searchFullArchive", method = RequestMethod.POST)
    public List searchFullArchive(
            @JsonParam List<Integer> catalogueIds
            ,@JsonParam List<Integer> archiveContentType
            ,@JsonParam String keyWord
    ) {
        Map<Integer, Long> aggs = entryService.aggsCatalogueCount(catalogueIds, archiveContentType, keyWord);

        List<Catalogue> catalogues =catalogueService.findAllById(aggs.keySet());

        List<Integer> archiveIds = catalogues.stream().map(Catalogue::getArchivesId).collect(Collectors.toList());

        List<Archives> archives = archivesService.findAllById(archiveIds);

        Map<Integer, Archives> archivesMap = archives.stream().collect(Collectors.toMap(Archives::getId, a -> a));

        List<Integer> archiveGroupIds = archives.stream().map(Archives::getArchivesGroupId).collect(Collectors.toList());

        List<ArchivesGroup> archivesGroups = archivesGroupService.findAllById(archiveGroupIds);

        Map<Integer, ArchivesGroup> archivesGroupMap = archivesGroups.stream().collect(Collectors.toMap(ArchivesGroup::getId, a-> a));

        List<Integer> fondsIds = archivesGroups.stream().map(ArchivesGroup::getFondsId).collect(Collectors.toList());

        List<Fonds> fonds = fondsService.findAllById(fondsIds);

        Map<Integer, Fonds> fondsMap = fonds.stream().collect(Collectors.toMap(Fonds::getId, a -> a));

        Map<Integer, List<Catalogue>> catalogueGroup = catalogues.stream().collect(Collectors.groupingBy(Catalogue::getArchivesId));

        List<Map<String, Object>> result = new ArrayList<>();
        catalogueGroup.forEach((aid, c) -> {
            Map<String, Object> archive = new HashMap<>();
            archive.put("archiveId", aid);
            archive.put("archiveName", archivesMap.get(aid).getName());
            archive.put("fondsName", fondsMap.get(
                    archivesGroupMap.get(archivesMap.get(aid).getArchivesGroupId()).getFondsId()).getName());
            archive.put("items", c.stream().map(a ->mapCatalogue(a, aggs)).collect(Collectors.toList()));
            result.add(archive);
        });

        return result;
    }

    private Map<String, Object> mapCatalogue(Catalogue catalogue, Map<Integer, Long> aggs){
        Map<String, Object> result = new HashMap<>();
        result.put("catalogueId", catalogue.getId());
        result.put("catalogueType", catalogue.getCatalogueType());
        result.put("count", aggs.get(catalogue.getId()));
        return result;
    }

    @RequestMapping(value = "/test/{id}", method = RequestMethod.GET)
    public Object test(@PathVariable("id") String id){
        return entryService.test(id);
    }
}
