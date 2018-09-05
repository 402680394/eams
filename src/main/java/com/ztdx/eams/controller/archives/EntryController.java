package com.ztdx.eams.controller.archives;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.EntryValueConverException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.exception.NotFoundException;
import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.archives.application.*;
import com.ztdx.eams.domain.archives.application.task.EntryAsyncTask;
import com.ztdx.eams.domain.archives.model.*;
import com.ztdx.eams.domain.archives.model.condition.EntryCondition;
import com.ztdx.eams.domain.archives.model.entryItem.EntryItemConverter;
import com.ztdx.eams.domain.archives.model.event.EntryBoxNumberValidateEvent;
import com.ztdx.eams.domain.store.application.BoxService;
import com.ztdx.eams.domain.store.model.Box;
import com.ztdx.eams.domain.store.model.event.BoxDeleteEvent;
import com.ztdx.eams.domain.store.model.event.BoxInsideChangeEvent;
import com.ztdx.eams.domain.system.application.FondsService;
import com.ztdx.eams.domain.system.application.PermissionService;
import com.ztdx.eams.domain.system.application.RoleService;
import com.ztdx.eams.domain.system.model.Fonds;
import com.ztdx.eams.domain.system.model.Permission;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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

    private ConditionService conditionService;

    private OriginalTextService originalTextService;

    private EntryAsyncTask entryAsyncTask;

    private BoxService boxService;

    private ApplicationContext applicationContext;

    private ContentTypeService contentTypeService;

    private PermissionService permissionService;

    private RoleService roleService;

    public EntryController(EntryService entryService, DescriptionItemService descriptionItemService, CatalogueService catalogueService, ArchivesService archivesService, ArchivesGroupService archivesGroupService, FondsService fondsService, ConditionService conditionService, OriginalTextService originalTextService, EntryAsyncTask entryAsyncTask, BoxService boxService, ApplicationContext applicationContext, ContentTypeService contentTypeService, PermissionService permissionService, RoleService roleService) {
        this.entryService = entryService;
        this.descriptionItemService = descriptionItemService;
        this.catalogueService = catalogueService;
        this.archivesService = archivesService;
        this.archivesGroupService = archivesGroupService;
        this.fondsService = fondsService;
        this.conditionService = conditionService;
        this.originalTextService = originalTextService;
        this.entryAsyncTask = entryAsyncTask;
        this.boxService = boxService;
        this.applicationContext = applicationContext;
        this.contentTypeService = contentTypeService;
        this.permissionService = permissionService;
        this.roleService = roleService;
    }

    /**
     * @api {get} /entry/{id}?cid={cid} 获取条目详细信息
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
     * "id":"322c3c75-08a5-4a01-a60f-084b6a6f9be9",
     * "catalogueId":1,
     * "catalogueType":1,
     * "archiveId":0,
     * "archiveType":0,
     * "archiveContentType":0,
     * "items":{
     * "birthday":"2018-05-16T14:44:56.328+0800",
     * "amount":73824039.1873,
     * "aihao":[
     * "电影",
     * "足球",
     * "汽车"
     * ],
     * "name":"里斯1",
     * "age":41
     * },
     * "gmtCreate":"2018-05-16T14:44:56.328+0800",
     * "gmtModified":"2018-05-16T14:44:56.328+0800"
     * }
     * @apiError (Error 404) message 1.条目不存在
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_read_' + #catalogueId)")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Entry get(@PathVariable("id") String id, @RequestParam("cid") int catalogueId) {
        Entry entry = entryService.get(catalogueId, id);
        if (entry == null) {
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
     * "catalogueId":1,
     * "items":{
     * "name":"姓名",
     * "age":41,
     * "birthday":"2018-05-09T00:00:00",
     * "amount":73824039.1873,
     * "aihao":[
     * "电影",
     * "汽车"
     * ]
     * }
     * }
     * @apiError (Error 400) message 1.档案目录不存在 2.其他数据验证错误
     * @apiUse ErrorExample
     * @apiSuccessExample {json} Success-Response:
     * {
     * "id":"322c3c75-08a5-4a01-a60f-084b6a6f9be9"
     * }
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_write_' + #entry.catalogueId)")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Map<String, String> save(@RequestBody Entry entry, @SessionAttribute UserCredential LOGIN_USER) {
        entry.setOwner(LOGIN_USER.getUserId());
        entry = entryService.save(entry);
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("id", entry.getId());
        return resultMap;
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
     * "items":{
     * "name":"姓名",
     * "age":41,
     * "birthday":"2018-05-09T00:00:00",
     * "amount":73824039.1873,
     * "aihao":[
     * "电影",
     * "汽车"
     * ]
     * }
     * }
     * @apiError (Error 400) message 1.档案目录不存在 2.其他数据验证错误
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_write_' + #entry.catalogueId)")
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void update(@PathVariable("id") String uuid, @RequestBody Entry entry) {
        entry.setId(uuid);
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
     * "cid": 1,
     * "deletes": [
     * "322c3c75-08a5-4a01-a60f-084b6a6f9be9"
     * ]
     * }
     * @apiError (Error 400) message 1.档案目录不存在
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_write_' + #cid)")
    public void delete(@JsonParam() int cid, @JsonParam List<String> deletes) {
        Catalogue catalogue = catalogueService.get(cid);
        if (catalogue == null) {
            throw new InvalidArgumentException("档案目录不存在");
        }
        entryService.deleteOrReduction(cid, deletes, 1);
    }

    /**
     * @api {put} /entry 从回收站恢复条目
     * @apiName reduction_entry
     * @apiGroup entry
     * @apiParam {Number} cid 档案目录id
     * @apiParam {String[]} ids 要恢复的条目id数组
     * @apiParamExample {json} Request-Example:
     * {
     * "cid": 1,
     * "ids": [
     * "322c3c75-08a5-4a01-a60f-084b6a6f9be9"
     * ]
     * }
     * @apiError (Error 400) message 1.档案目录不存在
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/reduction", method = RequestMethod.PUT)
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_write_' + #cid)")
    public void reduction(@JsonParam() int cid, @JsonParam List<String> ids) {
        Catalogue catalogue = catalogueService.get(cid);
        if (catalogue == null) {
            throw new InvalidArgumentException("档案目录不存在");
        }
        entryService.deleteOrReduction(cid, ids, 0);
    }

    /**
     * @api {get} /entry/search?cid={cid}&q={q}&page={page}&size={size}&isDeleted={isDeleted} 按关键字搜索条目
     * @apiName search_simple
     * @apiGroup entry
     * @apiParam {Number} cid 目录id(QueryString)
     * @apiParam {String} [q] 关键字(QueryString)
     * @apiParam {Number} [page] 页码(QueryString)
     * @apiParam {Number} [size] 页行数(QueryString)
     * @apiParam {Number} [isDeleted] 是否为回收站数据(默认为0，查询回收站数据传1)(QueryString)
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
     * @apiSuccess (Success 200) {Object} column.[birthday] 生日字段的属性
     * @apiSuccess (Success 200) {Number} column.birthday.metadataId 字段id
     * @apiSuccess (Success 200) {String} column.birthday.metadataName 字段名称
     * @apiSuccess (Success 200) {String} column.birthday.displayName 字段显示名称
     * @apiSuccess (Success 200) {Object} column.[amount] 资产字段的属性
     * @apiSuccess (Success 200) {Number} column.amount.metadataId 字段id
     * @apiSuccess (Success 200) {String} column.amount.metadataName 字段名称
     * @apiSuccess (Success 200) {String} column.amount.displayName 字段显示名称
     * @apiSuccess (Success 200) {Object} column.[aihao] 爱好字段的属性
     * @apiSuccess (Success 200) {Number} column.aihao.metadataId 字段id
     * @apiSuccess (Success 200) {String} column.aihao.metadataName 字段名称
     * @apiSuccess (Success 200) {String} column.aihao.displayName 字段显示名称
     * @apiSuccess (Success 200) {Object} column.[name] 姓名字段的属性
     * @apiSuccess (Success 200) {Number} column.name.metadataId 字段id
     * @apiSuccess (Success 200) {String} column.name.metadataName 字段名称
     * @apiSuccess (Success 200) {String} column.name.displayName 字段显示名称
     * @apiSuccess (Success 200) {Object} column.[age] 年龄字段的属性
     * @apiSuccess (Success 200) {Number} column.age.metadataId 字段id
     * @apiSuccess (Success 200) {String} column.age.metadataName 字段名称
     * @apiSuccess (Success 200) {String} column.age.displayName 字段显示名称
     * @apiSuccess (Success 200) {Number} totalElements 总行数
     * @apiSuccess (Success 200) {Number} totalPages 总页数
     * @apiSuccess (Success 200) {Number} innerCatalogueId 卷内目录id
     * @apiSuccessExample {json} Success-Response:
     * {
     * "data":{
     * "content":[
     * {
     * "id":"322c3c75-08a5-4a01-a60f-084b6a6f9be9",
     * "catalogueId":1,
     * "catalogueType":1,
     * "archiveId":0,
     * "archiveType":0,
     * "archiveContentType":0,
     * "items":{
     * "birthday":"2018-05-16T14:44:56.328+0800",
     * "amount":73824039.1873,
     * "aihao":[
     * "电影",
     * "足球",
     * "汽车"
     * ],
     * "name":"里斯1",
     * "age":41
     * },
     * "gmtCreate":"2018-05-16T14:44:56.328+0800",
     * "gmtModified":"2018-05-16T14:44:56.328+0800"
     * }
     * ],
     * "column":{
     * "name": {
     * "metadataId":1,
     * "metadataName":"name",
     * "displayName":"姓名"
     * },
     * "birthday": {
     * "metadataId":2,
     * "metadataName":"birthday",
     * "displayName":"生日"
     * },
     * "amount": {
     * "metadataId":3,
     * "metadataName":"amount",
     * "displayName":"资产"
     * },
     * "aihao": {
     * "metadataId":4,
     * "metadataName":"aihao",
     * "displayName":"爱好"
     * },
     * "age": {
     * "metadataId":5,
     * "metadataName":"age",
     * "displayName":"年龄"
     * }
     * },
     * "totalElements": 14,
     * "totalPages": 1,
     * "innerCatalogueId":2
     * }
     * }
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_read_' + #catalogueId)")
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public Map<String, Object> search(@RequestParam("cid") int catalogueId
            , @RequestParam(value = "q", required = false, defaultValue = "") String queryString
            , @RequestParam(value = "page", required = false, defaultValue = "0") int page
            , @RequestParam(value = "size", required = false, defaultValue = "20") int size
            , @RequestParam(value = "isDeleted", required = false, defaultValue = "0") int isDeleted) {
        Page<Entry> content = entryService.search(catalogueId, queryString, null, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "gmtCreate")), isDeleted);

        return getSearchMap(catalogueId, content);
    }

    /**
     * @api {get} /entry/searchInner?cid={cid}&pid={pid}&q={q}&page={page}&size={size}&isDeleted={isDeleted} 按关键字搜索案卷卷内目录
     * @apiName searchInner
     * @apiGroup entry
     * @apiParam {Number} cid 目录id(QueryString)
     * @apiParam {String} [pid] 父条目id(QueryString)，有值查此父条目id的卷内条目，否则查询未整理的卷内（无父条目id）
     * @apiParam {String} [q] 关键字(QueryString)
     * @apiParam {Number} [page] 页码(QueryString)
     * @apiParam {Number} [size] 页行数(QueryString)
     * @apiParam {Number} [isDeleted] 是否为回收站数据(默认为0，查询回收站数据传1)(QueryString)
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
     * @apiSuccessExample {json} Success-Response:
     * {
     * "data":{
     * "content":[
     * {
     * "id":"322c3c75-08a5-4a01-a60f-084b6a6f9be9",
     * "catalogueId":1,
     * "catalogueType":1,
     * "archiveId":0,
     * "archiveType":0,
     * "archiveContentType":0,
     * "items":{
     * "birthday":"2018-05-16T14:44:56.328+0800",
     * "amount":73824039.1873,
     * "aihao":[
     * "电影",
     * "足球",
     * "汽车"
     * ],
     * "name":"里斯1",
     * "age":41
     * },
     * "gmtCreate":"2018-05-16T14:44:56.328+0800",
     * "gmtModified":"2018-05-16T14:44:56.328+0800"
     * }
     * ],
     * "column":{
     * "name": {
     * "metadataId":1,
     * "metadataName":"name",
     * "displayName":"姓名"
     * },
     * "birthday": {
     * "metadataId":2,
     * "metadataName":"birthday",
     * "displayName":"生日"
     * },
     * "amount": {
     * "metadataId":3,
     * "metadataName":"amount",
     * "displayName":"资产"
     * },
     * "aihao": {
     * "metadataId":4,
     * "metadataName":"aihao",
     * "displayName":"爱好"
     * },
     * "age": {
     * "metadataId":5,
     * "metadataName":"age",
     * "displayName":"年龄"
     * }
     * },
     * "totalElements": 14,
     * "totalPages": 1
     * }
     * }
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_read_' + #catalogueId)")
    @RequestMapping(value = "/searchInner", method = RequestMethod.GET)
    public Map<String, Object> searchInner(@RequestParam("cid") int catalogueId
            , @RequestParam(value = "pid", required = false, defaultValue = "") String parentId
            , @RequestParam(value = "q", required = false, defaultValue = "") String queryString
            , @RequestParam(value = "page", required = false, defaultValue = "0") int page
            , @RequestParam(value = "size", required = false, defaultValue = "20") int size
            , @RequestParam(value = "isDeleted", required = false, defaultValue = "0") int isDeleted) {
        Catalogue folderFile = catalogueService.getFolderFileCatalogueByFolderCatalogueId(catalogueId);
        if (folderFile == null) {
            throw new InvalidArgumentException("卷内目录未找到");
        }
        Page<Entry> content = entryService.search(
                folderFile.getId()
                , queryString
                , null
                , parentId
                , null
                , PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "gmtCreate")), isDeleted);

        return getSearchMap(catalogueId, content);
    }

    private Map<String, Object> getSearchMap(int catalogueId, Page<Entry> content) {
        Map<String, Object> list = descriptionItemService.list(catalogueId, a -> {
            Map<String, Object> result = new HashMap<>();
            result.put("metadataId", a.getMetadataId());
            result.put("metadataName", a.getMetadataName());
            result.put("displayName", a.getDisplayName());
            return result;
        });

        Catalogue folderFileCatalogue = catalogueService.getFolderFileCatalogueByFolderCatalogueId(catalogueId);

        Map<String, Object> result = new HashMap<>();
        result.put("content", content.getContent());
        result.put("column", list);
        result.put("totalElements", content.getTotalElements());
        result.put("totalPages", content.getTotalPages());
        if (folderFileCatalogue != null) {
            result.put("innerCatalogueId", folderFileCatalogue.getId());
        }
        return result;
    }

    /**
     * @api {post} /entry/search?q={q}&page={page}&size={size} 高级搜索
     * @apiName search_adv
     * @apiGroup entry
     * @apiParam {String} q 关键字(QueryString)
     * @apiParam {Number} page 页码(QueryString)
     * @apiParam {Number} size 页行数(QueryString)
     * @apiParam {String} cid 档案目录id
     * @apiParam {Object[]} conditions 条件数组
     * @apiParam {String="and","or"} conditions.logical 逻辑操作符。第一个条件可以为空。
     * @apiParam {String} conditions.column 查询的列。（通过接口可以查询可以查询的列：{get} /condition/entry/columns?cid={cid}）
     * @apiParam {String="equal","notEqual","greaterThan","greaterThanOrEqual","lessThan","lessThanOrEqual","contain","notContain"} conditions.operator 逻辑操作符。第一个条件可以为空。
     * @apiParam {String} value 查询的值，可嵌套一组新的条件。
     * @apiParamExample {json} Request-Example
     * {
     * "cid": 5,
     * "conditions": [
     * {
     * "column": "age",
     * "operator": "greaterThanOrEqual",
     * "value": 1
     * },
     * {
     * "logical": "and",
     * "column": "age",
     * "operator": "lessThanOrEqual",
     * "value": 100
     * },
     * {
     * "logical": "and",
     * "value": [
     * {
     * "column": "name",
     * "operator": "equal",
     * "value": "接收到卡"
     * },
     * {
     * "logical": "or",
     * "column": "name",
     * "operator": "equal",
     * "value": "5561"
     * }
     * ]
     * }
     * ]
     * }
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
     * "data":{
     * "content":[
     * {
     * "id":"322c3c75-08a5-4a01-a60f-084b6a6f9be9",
     * "catalogueId":1,
     * "catalogueType":1,
     * "archiveId":0,
     * "archiveType":0,
     * "archiveContentType":0,
     * "items":{
     * "birthday":"2018-05-16T14:44:56.328+0800",
     * "amount":73824039.1873,
     * "aihao":[
     * "电影",
     * "足球",
     * "汽车"
     * ],
     * "name":"里斯1",
     * "age":41
     * },
     * "gmtCreate":"2018-05-16T14:44:56.328+0800",
     * "gmtModified":"2018-05-16T14:44:56.328+0800"
     * }
     * ],
     * "column":[
     * {
     * "metadataId":1,
     * "metadataName":"name",
     * "displayName":"姓名"
     * },
     * {
     * "metadataId":2,
     * "metadataName":"birthday",
     * "displayName":"生日"
     * },
     * {
     * "metadataId":3,
     * "metadataName":"amount",
     * "displayName":"资产"
     * },
     * {
     * "metadataId":4,
     * "metadataName":"aihao",
     * "displayName":"爱好"
     * },
     * {
     * "metadataId":5,
     * "metadataName":"age",
     * "displayName":"年龄"
     * }
     * ],
     * "totalElements":14
     * }
     * }
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_read_' + #entryCondition.catalogueId)")
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public Map<String, Object> searchAdv(
            @RequestBody EntryCondition entryCondition
            , @RequestParam("q") String queryString
            , @RequestParam("page") int page
            , @RequestParam("size") int size) {
        QueryBuilder query = conditionService.convert2ElasticsearchQuery(entryCondition.getCatalogueId(), entryCondition.getConditions());
        Page<Entry> content = entryService.search(entryCondition.getCatalogueId(), queryString, query, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "gmtCreate")), 0);
        return getSearchMap(entryCondition.getCatalogueId(), content);
    }

    /**
     * @api {get} /entry/searchByBox?boxId={boxId}&page={page}&size={size} 查询盒内条目
     * @apiName searchByBox
     * @apiGroup entry
     * @apiParam {String} boxId 盒id(QueryString)
     * @apiParam {Number} page 页码(QueryString)
     * @apiParam {Number} size 页行数(QueryString)
     * @apiSuccess (Success 200) {Array} content 列表内容
     * @apiSuccess (Success 200) {Number} content.id 条目id
     * @apiSuccess (Success 200) {Number} content.title 标题
     * @apiSuccess (Success 200) {Number=1,2,3} content.danghao 档号
     * @apiSuccess (Success 200) {Number} totalElements 总元素数
     * @apiSuccessExample {json} Success-Response:
     * {
     * "data":{
     * "content":[
     * {
     * "id":"322c3c75-08a5-4a01-a60f-084b6a6f9be9",
     * "title":1,
     * "danghao":1,
     * }
     * ],
     * "totalElements":14
     * }
     * }
     */
    //@PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_read_' + #catalogueId)")
    @RequestMapping(value = "/searchByBox", method = RequestMethod.GET)
    public Map<String, Object> searchByBox(
            @RequestParam(value = "boxId") int boxId
            , @RequestParam(value = "page", required = false, defaultValue = "0") int page
            , @RequestParam(value = "size", required = false, defaultValue = "20") int size) {

        Box box = boxService.get(boxId);

        Catalogue catalogue = catalogueService.getMainCatalogue(box.getArchivesId());

        if (catalogue == null) {
            throw new InvalidArgumentException("目录不存在");
        }

        /*DescriptionItem boxNumberItem = descriptionItemService.findByCatalogueIdAndPropertyType(catalogue.getId(), PropertyType.BoxNumber);
        String prefix = "items.%s";
        if (DescriptionItemDataType.String == boxNumberItem.getDataType()){
            prefix = "items.%s.keyword";
        }
        String boxNumberColumnName = String.format(prefix, boxNumberItem.getMetadataName());

        QueryBuilder query = QueryBuilders.termQuery(boxNumberColumnName, box.getCode());

        Page<Entry> content =  entryService.search(
                catalogue.getId()
                , null
                , query
                , null
                , null
                , PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "gmtCreate")));*/

        Page<Entry> content = entryService.listInBox(catalogue.getId(), box.getCode(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "gmtCreate")));

        DescriptionItem titleItem = descriptionItemService.findByCatalogueIdAndPropertyType(catalogue.getId(), PropertyType.Title);

        DescriptionItem danghaoItem = descriptionItemService.findByCatalogueIdAndPropertyType(catalogue.getId(), PropertyType.Reference);

        List<Map<String, String>> maps = content.getContent().stream().map(a -> {
            String title = "";
            if (titleItem != null) {
                title = a.getItems().getOrDefault(titleItem.getMetadataName(), "无标题").toString();
            }

            String danghao = "";
            if (danghaoItem != null) {
                danghao = a.getItems().getOrDefault(danghaoItem.getMetadataName(), "无档号").toString();
            }
            Map<String, String> r = new HashMap<>();
            r.put("title", title);
            r.put("danghao", danghao);
            r.put("id", a.getId());
            return r;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("content", maps);
        result.put("totalElements", content.getTotalElements());
        result.put("totalPages", content.getTotalPages());
        return result;
    }

    /**
     * @api {post} /entry/searchFulltext?page={page}&size={size} 全文搜索
     * @apiName searchFulltext
     * @apiGroup entry
     * @apiParam {Number} page 页码(QueryString)
     * @apiParam {Number} size 页行数(QueryString)
     * @apiParam {String} queryString 查询关键字
     * @apiParam {Array} archiveContentType 档案内容类型
     * @apiParam {String="3year","6year","8year"} dateRange 时间
     * @apiParam {Array="words","entry","file"} searchParam 搜索参数 words:全词匹配 entry:条目 file:全文
     * @apiParam {String} includeWords 在结果中查找-包含关键字
     * @apiParam {String} rejectWords 在结果中查找-排除关键字
     * @apiParam {Number} [catalogueId] 目录id
     * @apiParam {Object} [entryItems] 著录项内容，与目录id一起传递。
     * @apiSuccess (Success 200) {Array} content 列表内容
     * @apiSuccess (Success 200) {Number} content.id 条目id
     * @apiSuccess (Success 200) {Number} content.catalogueId 目录id
     * @apiSuccess (Success 200) {Number=1,2,3} content.catalogueType 目录类型 1:一文一件 2:传统立卷案卷 3:传统立卷卷内 4:项目
     * @apiSuccess (Success 200) {Number} content.archiveId 档案库id
     * @apiSuccess (Success 200) {String} content.archiveName 档案库名称
     * @apiSuccess (Success 200) {String} content.fondsName 全宗名称
     * @apiSuccess (Success 200) {Number=1,2} content.archiveType 档案库类型 1:登记库 2:归档库
     * @apiSuccess (Success 200) {Number} content.archiveContentType 档案库内容类型
     * @apiSuccess (Success 200) {Object} content.items 条目字段信息
     * @apiSuccess (Success 200) {Object} content.file 文件信息
     * @apiSuccess (Success 200) {Number} content.file.fileId 文件id
     * @apiSuccess (Success 200) {Number} content.file.title 标题
     * @apiSuccess (Success 200) {String="word","excel","pdf","ppt"} content.file.fileType 类型
     * @apiSuccess (Success 200) {String} content.file.highLight 高亮文本
     * @apiSuccess (Success 200) {String} content.file.fileName 文件名
     * @apiSuccess (Success 200) {Boolean} content.file.isView 是否可查看
     * @apiSuccess (Success 200) {Boolean} content.file.isDownload 是否可下载
     * @apiSuccess (Success 200) {Boolean} content.file.isPrint 是否可打印
     * @apiSuccess (Success 200) {Array} column 条目字段定义(以下内容每个档案库目录都不同，用来描述content.items中字段的定义)
     * @apiSuccess (Success 200) {Number} column.metadataId 元数据id
     * @apiSuccess (Success 200) {String} column.metadataName
     * @apiSuccess (Success 200) {Array} column.displayName 显示名称
     * @apiSuccess (Success 200) {Object} aggregation 统计数据
     * @apiSuccess (Success 200) {Array} aggregation.archiveContentType 档案类型统计
     * @apiSuccess (Success 200) {Number} aggregation.archiveContentType.id 档案类型id
     * @apiSuccess (Success 200) {Number} aggregation.archiveContentType.name 档案类型名称
     * @apiSuccess (Success 200) {Number} aggregation.archiveContentType.count 数量
     * @apiSuccess (Success 200) {Number} totalElements 总记录数
     * @apiSuccessExample {json} Success-Response:
     * {
     * "data":{
     * "content":[
     * {
     * "id":"322c3c75-08a5-4a01-a60f-084b6a6f9be9",
     * "catalogueId":1,
     * "catalogueType":1,
     * "archiveId":0,
     * "archiveName":"一文一件库",
     * "fondsName":"测试全宗",
     * "archiveType":0,
     * "archiveContentType":0,
     * "items":{
     * "生日":"2018-05-16T14:44:56.328+0800",
     * "金额":73824039.1873,
     * "爱好":[
     * "电影",
     * "足球",
     * "汽车"
     * ],
     * "姓名":"里斯1",
     * "年龄":41
     * },
     * "file":{
     * "fileId":"文件id",
     * "title":"这是一个文件标题",
     * "fileType":"word",
     * "highLight":"这是高亮文本内容",
     * "fileName": "文件名",
     * "pdfConverStatus",
     * "isView": true,
     * "isDownload": true,
     * "isPrint": true
     * }
     * }
     * ],
     * "aggregations":[
     * {
     * "id":"archiveContentType",
     * "name": "档案类型",
     * "count": 10,
     * "children":[
     * {
     * "id": "1",
     * "name":"文书档案",
     * "count":10
     * }
     * ],
     * },
     * {
     * "id":"fondsId",
     * "name": "全宗",
     * "count": 10,
     * "children":[
     * {
     * "id":"1",
     * "name":"全宗A",
     * "count":10
     * }
     * ],
     * },
     * {
     * "id":"archiveId",
     * "name": "档案库",
     * "count": 10,
     * "children":[
     * {
     * "id":"1",
     * "name":"一文一件库",
     * "count":10
     * }
     * ],
     * },
     * {
     * "id":"year",
     * "name": "年度",
     * "count": 10,
     * "children":[
     * {
     * "id":"1",
     * "name":"2018年",
     * "count":10
     * }
     * ],
     * }
     * ],
     * "totalElements": 14,
     * "totalPages": 1
     * }
     * }
     */
    @RequestMapping(value = "/searchFulltext", method = RequestMethod.POST)
    public Object searchFulltext(
            @RequestBody Map<String, Object> body
            /*, @JsonParam List<Integer> archiveContentType
            , @JsonParam List<String> searchParam
            , @JsonParam String queryString
            , @JsonParam String includeWords
            , @JsonParam String rejectWords
            , @JsonParam Integer catalogueId*/
            , @RequestParam(value = "page", defaultValue = "0") int page
            , @RequestParam(value = "size", defaultValue = "20") int size
            , @SessionAttribute UserCredential LOGIN_USER
    ) {

        List<Integer> archiveContentType = null;
        if (body.get("archiveContentType") != null) {
            archiveContentType = (List<Integer>) body.get("archiveContentType");
        }
        List<String> searchParam = null;
        if (body.get("searchParam") != null) {
            searchParam = (List<String>) body.get("searchParam");
        }
        String queryString = null;
        if (body.get("queryString") != null) {
            queryString = (String) body.get("queryString");
        }
        String includeWords = null;
        if (body.get("includeWords") != null) {
            includeWords = (String) body.get("includeWords");
        }
        String rejectWords = null;
        if (body.get("rejectWords") != null) {
            rejectWords = (String) body.get("rejectWords");
        }
        Integer catalogueId = null;
        if (body.get("catalogueId") != null) {
            catalogueId = (Integer) body.get("catalogueId");
        }
        Map<String, Object> entryItems = null;
        if (body.get("entryItems") != null) {
            entryItems = (Map<String, Object>) body.get("entryItems");
            makeEntryItems(entryItems);
        }

        assert searchParam != null;
        if (!searchParam.contains(SearchFulltextOption.entry.name())
                && !searchParam.contains(SearchFulltextOption.file.name())) {
            throw new InvalidArgumentException("请选择条目或者全文其中一项");
        }

        List<TermsAggregationParam> termsAggregationParams = getFulltextSearchAggregation();

        Collection<Integer> catalogueIds = getHasPermissionCatalogueIds(LOGIN_USER.getUserId(), catalogueId);

        AggregatedPage<OriginalText> list = entryService.searchFulltext(
                archiveContentType
                , searchParam
                , queryString
                , includeWords
                , rejectWords
                , catalogueIds
                , entryItems
                , termsAggregationParams
                , PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "gmtCreate")));

        Map<String, Object> result = new HashMap<>();

        if (list == null || list.getTotalElements() == 0) {
            result.put("totalElements", 0);
            result.put("totalPages", 0);
            result.put("content", null);
            return result;
        }

        List<TermsAggregationResult> aggs = entryService.convertAggregationToResult(list.getAggregations(), termsAggregationParams);

        Set<Integer> resultCatalogueIds = new HashSet<>();
        Set<String> entryIds = new HashSet<>();
        list.stream().forEach(a -> {
            resultCatalogueIds.add(a.getCatalogueId());
            entryIds.add(a.getEntryId());
        });

        List<Catalogue> catalogues = new ArrayList<>();
        Map<Integer, Archives> archivesMap = new HashMap<>();
        Map<Integer, ArchivesGroup> archivesGroupMap = new HashMap<>();
        Map<Integer, Fonds> fondsMap = new HashMap<>();

        getCatalogueInfo(resultCatalogueIds, catalogues, archivesMap, archivesGroupMap, fondsMap);

        Map<String, Entry> entries = new HashMap<>();
        entryService.findAllById(entryIds, null).forEach(a -> entries.put(a.getId(), a));

        Map<Integer, Map<String, DescriptionItem>> descItems = descriptionItemService.findAllByCatalogueIdIn(resultCatalogueIds)
                .stream().collect(
                        Collectors.groupingBy(
                                DescriptionItem::getCatalogueId
                                , Collectors.toMap(
                                        DescriptionItem::getMetadataName, a1 -> a1, (a1, a2) -> a2)));


        result.put("totalElements", list.getTotalElements());
        result.put("totalPages", list.getTotalPages());
        makeAggregations(aggs);
        result.put("aggregations", aggs);
        result.put("content", list.stream()
                .map(a -> mapSearchResult(
                        archivesMap
                        , archivesGroupMap
                        , fondsMap
                        , entries
                        , descItems
                        , a))
                .collect(Collectors.toList()));

        return result;
    }

    private Collection<Integer> getHasPermissionCatalogueIds(int userId, Integer catalogueId) {
        Collection<Integer> catalogueIds = getHasPermissionCatalogueIds(userId);
        if (catalogueId != null && catalogueIds != null) {
            if (catalogueIds.contains(catalogueId)) {
                catalogueIds = Collections.singletonList(catalogueId);
            } else {
                catalogueIds = null;
            }
        }
        return catalogueIds;
    }

    private Collection<Integer> getHasPermissionCatalogueIds(int userId) {
        List<Catalogue> catalogues = catalogueService.list(2);
        List<Integer> ids = catalogues.stream().map(Catalogue::getId).collect(Collectors.toList());
        if (permissionService.hasAnyAuthority("ROLE_ADMIN")) {
            return ids;
        }

        List<Permission> permissions = roleService.listUserPermissionSimple(userId);
        List<Integer> mainCatalogueIds = permissions.stream()
                .filter(a -> a.getArchiveId() != null
                        && a.getResourceUrl() != null
                        && a.getResourceUrl().equals("archive_file_search_" + a.getArchiveId()))
                .map(Permission::getArchiveId).collect(Collectors.toList());
        mainCatalogueIds.retainAll(ids);
        return mainCatalogueIds;
    }

    private void makeEntryItems(Map<String, Object> entryItems) {
        replaceMapKey(entryItems, "catalogueId");
        replaceMapKey(entryItems, "fondsId");
        replaceMapKey(entryItems, "archiveContentType");
    }

    private void replaceMapKey(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            map.put("_".concat(key), map.get(key));
            map.remove(key);
        }
    }

    private Map<String, Object> mapSearchResult(Map<Integer, Archives> archivesMap, Map<Integer, ArchivesGroup> archivesGroupMap, Map<Integer, Fonds> fondsMap, Map<String, Entry> entries, Map<Integer, Map<String, DescriptionItem>> descItems, OriginalText a) {
        Map<String, Object> r = new HashMap<>();

        Entry entry = entries.get(a.getEntryId());
        Archives archives = null;
        Fonds fonds = null;

        r.put("id", a.getEntryId());
        r.put("catalogueId", a.getCatalogueId());

        if (entry != null) {
            r.put("catalogueType", entry.getCatalogueType());
            r.put("archiveId", entry.getArchiveId());

            Map<String, Object> items = formatEntryItems(entry, descItems.get(entry.getCatalogueId()));
            r.put("items", items);

            archives = archivesMap.get(entry.getArchiveId());
        }

        if (archives != null) {
            r.put("archiveName", archives.getName());
            r.put("archiveType", archives.getType());
            r.put("archiveContentType", archives.getContentTypeId());
            ArchivesGroup archivesGroup = archivesGroupMap.get(archives.getArchivesGroupId());
            if (archivesGroup != null) {
                fonds = fondsMap.get(archivesGroup.getFondsId());
            }
        }

        if (fonds != null) {
            r.put("fondsName", fonds.getName());
        }

        Map<String, Object> file = new HashMap<>();
        r.put("file", file);

        file.put("fileId", a.getId());

        file.put("fileType", a.getType());
        file.put("pdfConvertStatus", a.getPdfConverStatus());

        boolean isView = permissionService.hasAnyAuthority("ROLE_ADMIN", "object_original_text_view_" + a.getId());

        String highLight;
        String title;
        String fileName;
        if (isView) {
            highLight = a.getContentIndex();
            title = a.getTitle();
            fileName = a.getName();
        } else {
            highLight = "您没有原文查看权限";
            title = "您无权查看";
            fileName = "您无权查看";
        }

        if (highLight != null && highLight.length() > 300) {
            highLight = highLight.substring(0, a.getContentIndex().length() > 300 ? 300 : a.getContentIndex().length());
        }
        file.put("highLight", highLight);
        file.put("fileName", fileName);
        file.put("title", title);


        file.put("isView", isView);
        file.put("isDownload", permissionService.hasAnyAuthority("ROLE_ADMIN", "object_original_text_download_" + a.getId()));
        file.put("isPrint", permissionService.hasAnyAuthority("ROLE_ADMIN", "object_original_text_print_" + a.getId()));

        return r;
    }

    private List<TermsAggregationParam> getFulltextSearchAggregation() {
        List<TermsAggregationParam> result = new ArrayList<>();
        result.add(new TermsAggregationParam("archiveContentType", "档案类型"));
        result.add(new TermsAggregationParam("fondsId", "全宗"));
        result.add(new TermsAggregationParam("catalogueId", "档案库"));
        //TODO @lijie 一下三种还未实现
        //result.add(new TermsAggregationParam("year", TermsAggregationParam.TermsAggregationParamFieldType.custom));
        //result.add(new TermsAggregationParam("classificationNumber", TermsAggregationParam.TermsAggregationParamFieldType.custom));
        //result.add(new TermsAggregationParam("department", TermsAggregationParam.TermsAggregationParamFieldType.custom));

        return result;
    }

    private void makeAggregations(List<TermsAggregationResult> aggregations) {

        aggregations.forEach(termsAggregationResult -> {
            List<String> ids = termsAggregationResult.getChildren().stream().map(TermsAggregationResult::getId).collect(Collectors.toList());

            Map<String, String> map = null;

            switch (termsAggregationResult.getId()) {
                case "archiveContentType":
                    map = mapArchiveContentTypeList();
                    break;
                case "fondsId":
                    map = mapFondsList(ids.stream().map(Integer::parseInt).collect(Collectors.toList()));
                    break;
                case "catalogueId":
                    map = mapCatalogueList(ids.stream().map(Integer::parseInt).collect(Collectors.toList()));
                    break;
            }

            if (map != null) {
                Map<String, String> finalMap = map;
                termsAggregationResult.getChildren().forEach(a -> a.setName(finalMap.getOrDefault(a.getId(), a.getId())));
            }
        });
    }

    private Map<String, String> mapCatalogueList(List<Integer> ids) {
        List<Catalogue> list = catalogueService.findAllById(ids);
        List<Integer> archiveIds = list.stream().map(Catalogue::getArchivesId).collect(Collectors.toList());

        Map<Integer, String> archiveMap = archivesService.findAllById(archiveIds).stream().collect(Collectors.toMap(Archives::getId, Archives::getName));

        return list.stream().collect(Collectors.toMap(a -> String.format("%s", a.getId()), a -> {
            String archiveName = archiveMap.get(a.getArchivesId());
            if (a.getCatalogueType() == CatalogueType.Folder) {
                archiveName += "_案卷";
            } else if (a.getCatalogueType() == CatalogueType.FolderFile) {
                archiveName += "_卷内";
            }
            return archiveName;
        }));
    }

    private Map<String, String> mapFondsList(Collection<Integer> ids) {
        return fondsService.findAllById(ids).stream().collect(Collectors.toMap(a -> String.format("%s", a.getId()), Fonds::getName));
    }

    private Map<String, String> mapArchiveContentTypeList() {
        return contentTypeService.list().stream().collect(Collectors.toMap(a -> String.format("%s", a.getId()), ContentType::getName));
    }


    private Map<String, Object> formatEntryItems(Entry entry, Map<String, DescriptionItem> itemMap) {
        Map<String, Object> result = new HashMap<>();
        entry.getItems().forEach((a, b) -> {
            DescriptionItem item = itemMap.get(a);
            if (item != null) {
                String format = EntryItemConverter.format(entry, item);
                result.put(item.getDisplayName(), format);
            }
        });
        return result;
    }

    private void getCatalogueInfo(Set<Integer> catalogueIds
            , List<Catalogue> catalogues
            , Map<Integer, Archives> archivesMap
            , Map<Integer, ArchivesGroup> archivesGroupMap
            , Map<Integer, Fonds> fondsMap) {
        catalogues.addAll(catalogueService.findAllById(catalogueIds));

        List<Integer> archiveIds = catalogues.stream().map(Catalogue::getArchivesId).collect(Collectors.toList());

        List<Archives> archives = archivesService.findAllById(archiveIds);

        archivesMap.putAll(archives.stream().collect(Collectors.toMap(Archives::getId, a -> a)));

        List<Integer> archiveGroupIds = archives.stream().map(Archives::getArchivesGroupId).collect(Collectors.toList());

        List<ArchivesGroup> archivesGroups = archivesGroupService.findAllById(archiveGroupIds);

        archivesGroupMap.putAll(archivesGroups.stream().collect(Collectors.toMap(ArchivesGroup::getId, a -> a)));

        List<Integer> fondsIds = archivesGroups.stream().map(ArchivesGroup::getFondsId).collect(Collectors.toList());

        List<Fonds> fonds = fondsService.findAllById(fondsIds);

        fondsMap.putAll(fonds.stream().collect(Collectors.toMap(Fonds::getId, a -> a)));
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
     * "data": {
     * "content": [
     * {
     * "archiveId": 1,
     * "archiveName": "一文一件库",
     * "fondsName": "测试全宗",
     * "items":[
     * {
     * "catalogueId": 1,
     * "catalogueType": 1,
     * "count": 1
     * }
     * ]
     * }
     * ]
     * }
     * }
     */
    @RequestMapping(value = "/searchFullArchive", method = RequestMethod.POST)
    public List searchFullArchive(
            @JsonParam List<Integer> catalogueIds
            , @JsonParam List<Integer> archiveContentType
            , @JsonParam String keyWord
            , @SessionAttribute UserCredential LOGIN_USER
    ) {
        //TODO @lijie 权限需要换成条目的
        /*Collection<Integer> hasPermissionCatalogueIds = getHasPermissionCatalogueIds(LOGIN_USER.getUserId());
        catalogueIds.retainAll(hasPermissionCatalogueIds);*/

        Map<Integer, Long> aggs = entryService.aggsCatalogueCount(catalogueIds, archiveContentType, keyWord);

        List<Catalogue> catalogues = new ArrayList<>();
        Map<Integer, Archives> archivesMap = new HashMap<>();
        Map<Integer, ArchivesGroup> archivesGroupMap = new HashMap<>();
        Map<Integer, Fonds> fondsMap = new HashMap<>();

        getCatalogueInfo(aggs.keySet(), catalogues, archivesMap, archivesGroupMap, fondsMap);

        Map<Integer, List<Catalogue>> catalogueGroup = catalogues.stream().collect(Collectors.groupingBy(Catalogue::getArchivesId));

        List<Map<String, Object>> result = new ArrayList<>();
        catalogueGroup.forEach((aid, c) -> {
            Map<String, Object> archive = new HashMap<>();
            archive.put("archiveId", aid);
            archive.put("archiveName", archivesMap.get(aid).getName());
            archive.put("fondsName", fondsMap.get(
                    archivesGroupMap.get(archivesMap.get(aid).getArchivesGroupId()).getFondsId()).getName());
            archive.put("items", c.stream().map(a -> mapCatalogue(a, aggs)).collect(Collectors.toList()));
            result.add(archive);
        });

        return result;
    }

    private Map<String, Object> mapCatalogue(Catalogue catalogue, Map<Integer, Long> aggs) {
        Map<String, Object> result = new HashMap<>();
        result.put("catalogueId", catalogue.getId());
        result.put("catalogueType", catalogue.getCatalogueType());
        result.put("count", aggs.get(catalogue.getId()));
        return result;
    }

    @RequestMapping(value = "/test/{id}", method = RequestMethod.GET)
    public void test(@PathVariable("id") String id) {
        entryAsyncTask.test(id);
        //entryService.groupCountPageCountByBox(Collections.singletonList("222"), 7);
        //Object a = entryService.groupByBox(Collections.singletonList("222"), 7);
    }

    /**
     * @api {put} /entry/batchIdentification 批量鉴定
     * @apiName batchIdentification
     * @apiGroup entry
     * @apiParam {Array} entryIds 条目id集合.
     * @apiParam {Number} catalogueId 目录ID.
     * @apiParam {Number} isOpen 开放受控鉴定（1 无   2 开放   3 受控）.
     * @apiParam {Number} isExpired 到期鉴定(0 未到期  1 已到期).
     * @apiParam {Number} isEndangered 濒危鉴定(0 正常  1 濒危).
     * @apiParam {Number} isLose 遗失鉴定(0 未遗失  1 已遗失).
     * @apiParamExample {json} Request-Example:
     * {
     * "entryIds":["ea5ebe11-a6c3-4db6-91e4-df783be235c3","26a5aa18-2606-46fc-bbd9-5f4d78e57956"],
     * "catalogueId":10,
     * "isOpen":2,
     * "isExpired":0,
     * "isEndangered":1,
     * "isLose":0
     * }
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_write_' + #catalogueId)")
    @RequestMapping(value = "/batchIdentification", method = RequestMethod.PUT)
    public void batchIdentification(@JsonParam List<String> entryIds, @JsonParam int catalogueId, @JsonParam Integer isOpen, @JsonParam Boolean isExpired, @JsonParam Boolean isEndangered, @JsonParam Boolean isLose) {
        entryService.batchIdentification(entryIds, catalogueId, isOpen, isExpired, isEndangered, isLose);
    }

    /**
     * @api {get} /entry/getIdentification/{id} 查看单个鉴定信息
     * @apiName getIdentification
     * @apiGroup entry
     * @apiParam {Number} id 条目id(url占位符).
     * @apiSuccess (Success 200) {Number=1，2，3} isOpen 开放受控鉴定（1 无   2 开放   3 受控）.
     * @apiSuccess (Success 200) {boolean=true,false} isExpired 到期鉴定(false:未到期  true:已到期).
     * @apiSuccess (Success 200) {boolean=true,false} isEndangered 濒危鉴定(false:正常  true:濒危).
     * @apiSuccess (Success 200) {boolean=true,false} isLose 遗失鉴定(false:未遗失  true:已遗失).
     * @apiSuccessExample {json} Response-Example:
     * {
     * "data": {
     * "isOpen": 2,
     * "isLose": false,
     * "isEndangered": true,
     * "isExpired": false
     * }
     * }
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_read_' + #catalogueId)")
    @RequestMapping(value = "/getIdentification/{id}", method = RequestMethod.GET)
    public Map<String, Object> getIdentification(@PathVariable String id, @RequestParam("catalogueId") Integer catalogueId) {
        return entryService.getIdentification(id, catalogueId);
    }

    /**
     * @api {post} /entry/setNewVolume 组新卷
     * @apiName setNewVolume
     * @apiGroup entry
     * @apiParam {Array} folderFileEntryIds 卷内条目id数组
     * @apiParam {String} folderFileCatalogueId 卷内目录id
     * @apiParam {Object} entry 案卷条目
     * @apiParam {Object} entry.items 条目详细内容，是一个动态的key-value数组。
     * @apiParam {String} entry.items.name 姓名
     * @apiParam {Number} entry.items.age 年龄
     * @apiParam {Date} entry.items.regDate 注册日期
     * @apiParam {String[]} entry.items.interest 爱好
     * @apiError (Error 400) message 1.档案目录不存在 2.其他数据验证错误
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_write_' + #entry.catalogueId)")
    @RequestMapping(value = "/setNewVolume", method = RequestMethod.POST)
    public void setNewVolume(@JsonParam List<String> folderFileEntryIds, @JsonParam int folderFileCatalogueId, @JsonParam Entry entry) {
        entryService.setNewVolume(folderFileEntryIds, folderFileCatalogueId, entry);
    }

    /**
     * @api {put} /entry/separateVolume 拆卷
     * @apiName separateVolume
     * @apiGroup entry
     * @apiParam {String} folderEntryId 案卷条目id
     * @apiParam {Number} folderCatalogueId 案卷目录id
     * @apiParam {String[]} folderFileEntryIds 卷内条目id数组
     * @apiParam {Number} folderFileCatalogueId 卷内目录id
     * @apiError (Error 400) message
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_write_' + #folderFileCatalogueId)")
    @RequestMapping(value = "/separateVolume", method = RequestMethod.PUT)
    public void separateVolume(@RequestBody Map<String, Object> map) {
        String folderEntryId = (String) map.getOrDefault("folderEntryId", null);
        Integer folderCatalogueId = (Integer) map.getOrDefault("folderCatalogueId", null);
        if (folderCatalogueId == null || folderEntryId == null) {
            entryService.separateVolume((List<String>) (map.get("folderFileEntryIds")), (Integer) (map.get("folderFileCatalogueId")));
        } else {
            entryService.separateVolumeForFolder(folderEntryId, folderCatalogueId);
        }
    }

    /**
     * @api {post} /entry/archiving 归档
     * @apiName archivingEntry
     * @apiGroup entry
     * @apiParam {Boolean} delSrc 是否删除源记录
     * @apiParam {Number[]} originalType 原文类型，引用获取文件类型列表接口 /fileType/list
     * @apiParam {Boolean} archivingAll 是否归档所有条目
     * @apiParam {Object[]} mapping 映射
     * @apiParam {Number} mapping.srcId 源目录id
     * @apiParam {Number} mapping.trgId 目标目录id
     * @apiParam {String[]} mapping.srcFields 源字段列表
     * @apiParam {String[]} mapping.trgFields 目标字段列表，必须与源字段一一映射。
     * @apiParam {String[]} mapping.srcData 归档的数据，如果为空则归档全部数据
     * @apiParamExample {json} Request-Example
     * {
     * "delSrc": true,
     * "originalType": [1,2],
     * "archivingAll": true,
     * "mapping":[
     * {
     * "srcId": 1,
     * "trgId": 2,
     * "srcFields":[
     * "name",
     * "age"
     * ],
     * "trgFields":[
     * "name",
     * "age"
     * ],
     * "srcData":[
     * "xxxx-xxxx-xxxx"
     * ]
     * }
     * ]
     * }
     * @apiSuccess {Object} success 成功记录数
     * @apiSuccess {Number} success.total 总成功数
     * @apiSuccess {Number} success.main 条目数
     * @apiSuccess {Number} success.children 卷内条目数
     * @apiSuccess {Number} success.file 原文数
     * @apiSuccess {Object} failure 失败记录数
     * @apiSuccess {Number} failure.total 总成功数
     * @apiSuccess {Number} failure.main 条目数
     * @apiSuccess {Number} failure.children 卷内条目数
     * @apiSuccess {Number} failure.file 原文数
     * @apiSuccess {Object[]} items 详细信息
     * @apiSuccess {String} items.id 条目id
     * @apiSuccess {String} items.title 标题
     * @apiSuccess {String} items.errorMsg 错误消息
     * @apiSuccess {Number=1,2} items.type 类型 1:条目 2:原文
     * @apiSuccess {Number=1,2} items.status 状态 1:成功 2:失败
     * @apiSuccess {Object[]} items.children 下级信息，结构同items
     * @apiSuccessExample {json} Response-Example
     * {
     * "success": {
     * "total": 110,
     * "main": 2,
     * "children": 8,
     * "file": 100
     * },
     * "failure": {
     * "total": 5,
     * "main": 1,
     * "children": 2,
     * "file": 2
     * },
     * "items": [
     * {
     * "id": "cef91008-e167-4fb6-ae99-d9961ad9f328",
     * "parentId": null,
     * "title": "测试归档4",
     * "errorMsg": "成功",
     * "type": 1,
     * "status": 1,
     * "children": [
     * {
     * "id": "d1fbabee-8ce2-4a1e-a6c0-23083c6f754e",
     * "parentId": "cef91008-e167-4fb6-ae99-d9961ad9f328",
     * "title": "测试卷内归档",
     * "errorMsg": "成功",
     * "type": 1,
     * "status": 1,
     * "children": [
     * {
     * "id": "bb375230-45f8-46d9-a9aa-12320e929f8e",
     * "parentId": "d1fbabee-8ce2-4a1e-a6c0-23083c6f754e",
     * "title": "测试原文归档",
     * "errorMsg": "成功",
     * "type": 2,
     * "status": 1,
     * "children": []
     * }
     * ]
     * }
     * ]
     * }
     * ]
     * }
     * @apiError message 1.源目录id不存在 2.归档目录id不存在 3.字段映射错误
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_write_' + #mainTrgId) && hasAnyAuthority('archive_entry_read_' + #mainSrcId)")
    @RequestMapping(value = "/archiving", method = RequestMethod.POST)
    public Object archiving(@JsonParam(path = "delSrc") Boolean delSrc
            , @JsonParam(path = "originalType") List<Integer> originalType
            , @JsonParam(path = "archivingAll") boolean archivingAll
            , @JsonParam(path = "mapping[0].srcId") int mainSrcId
            , @JsonParam(path = "mapping[0].trgId") int mainTrgId
            , @JsonParam(path = "mapping[0].srcFields") List<String> mainSrcFields
            , @JsonParam(path = "mapping[0].trgFields") List<String> mainTrgFields
            , @JsonParam(path = "mapping[0].srcData") List<String> mainSrcData
            , @JsonParam(path = "mapping[1].srcId") Integer slaveSrcId
            , @JsonParam(path = "mapping[1].trgId") Integer slaveTrgId
            , @JsonParam(path = "mapping[1].srcFields") List<String> slaveSrcFields
            , @JsonParam(path = "mapping[1].trgFields") List<String> slaveTrgFields
            , @SessionAttribute UserCredential LOGIN_USER
    ) {
        List<ArchivingResult> error = new ArrayList<>();
        Map<String, String> mainIdMap = archivingEntry(
                mainSrcId
                , mainTrgId
                , archivingAll
                , null
                , mainSrcFields
                , mainTrgFields
                , mainSrcData
                , null
                , LOGIN_USER.getUserId()
                , error);

        //归档条目的原文
        assert mainIdMap != null;
        Map<String, String> mainFileIdMap = null;
        if (mainIdMap.size() > 0) {
            mainFileIdMap = archivingOriginal(mainSrcId, mainTrgId, archivingAll, mainIdMap.keySet(), mainIdMap, originalType, error);
        }

        Map<String, String> slaveIdMap = null;
        Map<String, String> slaveFileIdMap = null;
        if (slaveSrcId != null && slaveTrgId != null && slaveSrcFields != null && slaveTrgFields != null) {
            slaveIdMap = archivingEntry(
                    slaveSrcId
                    , slaveTrgId
                    , archivingAll
                    , mainTrgId
                    , slaveSrcFields
                    , slaveTrgFields
                    , null
                    , mainIdMap
                    , LOGIN_USER.getUserId()
                    , error);

            //归档条目的原文
            assert slaveIdMap != null;
            if (slaveIdMap.size() > 0) {
                slaveFileIdMap = archivingOriginal(slaveSrcId, slaveTrgId, archivingAll, slaveIdMap.keySet(), slaveIdMap, originalType, error);
            }
        }

        if (delSrc) {
            Map<Integer, Set<String>> delEntryIds = new HashMap<>();
            Map<Integer, Set<String>> delOriginalIds = new HashMap<>();

            delEntryIds.put(mainSrcId, new HashSet<>());
            delOriginalIds.put(mainSrcId, new HashSet<>());
            if (slaveSrcId != null) {
                delEntryIds.put(slaveSrcId, new HashSet<>());
                delOriginalIds.put(slaveSrcId, new HashSet<>());
            }
            getDeleteIds(error, delEntryIds, delOriginalIds);

            delEntryIds.forEach((a, b) -> {
                entryService.deleteOrReduction(a, b, 1);
            });

            delOriginalIds.forEach((a, b) -> {
                delOriginal(b, a);
            });
        }

        return formatArchivingResult(error);
    }

    private void getDeleteIds(
            List<ArchivingResult> error
            , Map<Integer, Set<String>> delEntryIds
            , Map<Integer, Set<String>> delOriginalIds) {
        error.forEach(a -> {
            if (a.getStatus() == ArchivingResult.Status.failure) {
                return;
            }

            if (a.getType() == ArchivingResult.Type.entry) {
                Set<String> entryIds = delEntryIds.get(a.getCatalogId());
                entryIds.add(a.getId());
            } else {
                Set<String> originalIds = delOriginalIds.get(a.getCatalogId());
                originalIds.add(a.getId());
            }
        });
    }

    private Map<String, Object> formatArchivingResult(List<ArchivingResult> list) {
        AtomicInteger successMain = new AtomicInteger(0);
        AtomicInteger successChildren = new AtomicInteger(0);
        AtomicInteger successFile = new AtomicInteger(0);
        AtomicInteger failureMain = new AtomicInteger(0);
        AtomicInteger failureChildren = new AtomicInteger(0);
        AtomicInteger failureFile = new AtomicInteger(0);
        Map<String, ArchivingResult> items = new HashMap<>();
        list.forEach(a -> {
            if (a.getStatus() == ArchivingResult.Status.success) {
                countArchivingResult(a, successMain, successChildren, successFile);
            } else {
                countArchivingResult(a, failureMain, failureChildren, failureFile);
            }

            String parentId = a.getParentId();

            ArchivingResult current = items.getOrDefault(a.getId(), null);
            if (current != null) {
                a.setChildren(current.getChildren());
            }
            items.put(a.getId(), a);

            if (parentId != null) {
                ArchivingResult parent = items.getOrDefault(parentId, null);
                if (parent != null) {
                    parent.getChildren().add(a);
                } else {
                    ArchivingResult tmp = new ArchivingResult();
                    tmp.getChildren().add(a);
                    items.put(parentId, tmp);
                }
            }
        });
        Map<String, Object> result = new HashMap<>();
        result.put("items", items.values().stream().filter(a -> a.getParentId() == null).collect(Collectors.toList()));
        result.put("success", getCntMap(successMain, successChildren, successFile));
        result.put("failure", getCntMap(failureMain, failureChildren, failureFile));
        return result;
    }

    private Map<String, Integer> getCntMap(AtomicInteger main, AtomicInteger children, AtomicInteger file) {
        Map<String, Integer> success = new HashMap<>();
        success.put("total", main.get() + children.get() + file.get());
        success.put("main", main.get());
        success.put("children", children.get());
        success.put("file", file.get());

        return success;
    }

    private void countArchivingResult(ArchivingResult a, AtomicInteger mainCnt, AtomicInteger childrenCnt, AtomicInteger fileCnt) {
        if (a.getParentId() == null) {
            mainCnt.set(mainCnt.get() + 1);
        } else if (a.getType() == ArchivingResult.Type.entry) {
            childrenCnt.set(childrenCnt.get() + 1);
        }

        if (a.getType() == ArchivingResult.Type.file) {
            fileCnt.set(fileCnt.get() + 1);
        }
    }

    private void delOriginal(Set<String> ids, Integer srcId) {
        if (ids != null && ids.size() > 0 && srcId != null) {
            List<Map<String, Object>> batch = ids.stream().map(a -> {
                Map<String, Object> map = new HashMap<>();
                map.put(a, srcId);
                return map;
            }).collect(Collectors.toList());
            originalTextService.deleteBatch(batch);
        }
    }

    private Map<String, String> archivingOriginal(
            int srcId
            , int trgId
            , boolean archivingAll
            , Collection<String> srcData
            , Map<String, String> srcDataMap
            , Collection<Integer> originalType
            , List<ArchivingResult> error) {
        if (!catalogueService.exists(srcId)) {
            throw new InvalidArgumentException("源目录id不存在");
        }

        if (!catalogueService.exists(trgId)) {
            throw new InvalidArgumentException("归档目录id不存在");
        }

        int page = 0;
        int size = 100;
        Page<OriginalText> mainOriginalTexts;

        Map<String, String> result = new HashMap<>();
        do {
            mainOriginalTexts = originalTextService.scroll(archivingAll, srcId, srcData, originalType, page++, size);
            if (mainOriginalTexts.getNumberOfElements() == 0) {
                return result;
            }
            List<String> mainSrcOriginalTextsData = mainOriginalTexts.stream().map(OriginalText::getId).collect(Collectors.toList());
            Map<String, String> mainOriginalTextsData = entryService.generatorId(mainSrcOriginalTextsData);

            List<ArchivingResult> mainOriginalTextsError = originalTextService.archivingOriginal(
                    trgId, mainOriginalTextsData, srcDataMap, mainOriginalTexts);
            if (mainOriginalTextsError != null) {
                error.addAll(mainOriginalTextsError);
            }

            result.putAll(mainOriginalTextsData);
        } while (mainOriginalTexts.hasNext());
        return result;
    }

    private Map<String, String> archivingEntry(
            int srcId
            , int trgId
            , boolean archivingAll
            , Integer parentTrgId
            , List<String> srcFields
            , List<String> trgFields
            , List<String> srcData
            , Map<String, String> parentDataMap
            , int owner
            , List<ArchivingResult> error) {

        if (srcFields.size() != trgFields.size()) {
            throw new InvalidArgumentException("字段映射错误");
        }

        for (int i = 0; i < srcFields.size(); i++) {
            if (StringUtils.isEmpty(srcFields.get(i))
                    || StringUtils.isEmpty(trgFields.get(i))) {
                srcFields.remove(i);
                trgFields.remove(i);
                i--;
            }
        }

        if (!catalogueService.exists(srcId)) {
            throw new InvalidArgumentException("源目录id不存在");
        }

        if (!catalogueService.exists(trgId)) {
            throw new InvalidArgumentException("归档目录id不存在");
        }

        Map<String, DescriptionItem> srcDescriptionItems = descriptionItemService.list(srcId, a -> a);
        Map<String, DescriptionItem> trgDescriptionItems = descriptionItemService.list(trgId, a -> a);
        validateArchivingFields(srcDescriptionItems, srcFields);
        validateArchivingFields(trgDescriptionItems, trgFields);

        AtomicReference<DescriptionItem> titleField = new AtomicReference<>();
        srcDescriptionItems.values().stream()
                .filter(a -> a.getPropertyType() == PropertyType.Title)
                .findFirst()
                .ifPresent(titleField::set);

        int page = 0;
        int size = 100;
        Page<Entry> entries;

        Map<String, String> result = new HashMap<>();
        do {
            //归档条目
            if (archivingAll || parentDataMap == null) {
                entries = entryService.scrollEntry(archivingAll, srcId, srcData, page++, size);
            } else {
                entries = entryService.scrollSubEntry(srcId, parentDataMap.keySet(), page++, size);
            }
            if (entries.getNumberOfElements() == 0) {
                return result;
            }
            List<String> srcIds = entries.stream().map(Entry::getId).collect(Collectors.toList());
            Map<String, String> srcDataMap = entryService.generatorId(srcIds);

            List<ArchivingResult> mainError = entryService.archivingEntry(
                    trgId
                    , parentTrgId
                    , srcFields
                    , trgFields
                    , srcDataMap
                    , parentDataMap
                    , entries
                    , owner
                    , (a) -> {
                        if (titleField.get() == null) {
                            return a.getId();
                        } else {
                            Object titleValue = a.getItems().getOrDefault(titleField.get().getMetadataName(), a.getId());
                            return titleValue.toString();
                        }
                    }
            );
            if (mainError != null) {
                error.addAll(mainError);
            }

            result.putAll(srcDataMap);
        } while (entries.hasNext());

        return result;
    }

    private void validateArchivingFields(Map<String, DescriptionItem> descriptionItems, List<String> fields) {
        Set<String> qryFields = descriptionItems.keySet();
        if (!qryFields.containsAll(fields)) {
            throw new InvalidArgumentException("字段设置错误");
        }
    }


    /**
     * @api {post} /entry/setBoxCode 装盒（设置盒号）
     * @apiName setBoxCode
     * @apiGroup entry
     * @apiParam {Number} catalogueId 目录id
     * @apiParam {String[]} ids 条目id数组
     * @apiParam {String} boxCode 盒号
     * @apiParamExample {json} Request-Example
     * {
     * "catalogueId": 1,
     * "ids": ["xxxx-xxxx-xxxx"],
     * "boxCode": "box001"
     * }
     * @apiError message 1.没有盒号字段 2.盒不存在 3.目录不存在 4.条目不存在
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_write_' + #catalogueId)")
    @RequestMapping(value = "/setBoxCode", method = RequestMethod.POST)
    @Transactional
    public void setBoxCode(@JsonParam List<String> ids, @JsonParam String boxCode, @JsonParam int catalogueId) {
        Catalogue catalogue = catalogueService.get(catalogueId);
        if (catalogue == null) {
            throw new InvalidArgumentException("目录不存在");
        }

        Box box = boxService.getByCode(catalogue.getArchivesId(), boxCode);
        if (box == null) {
            throw new InvalidArgumentException("盒不存在");
        }

        entryService.inBox(catalogueId, ids, boxCode);

        applicationContext.publishEvent(new BoxInsideChangeEvent(this, catalogueId, catalogue.getArchivesId(), Collections.singletonList(boxCode)));
    }

    /**
     * @api {post} /entry/unSetBoxCode 拆盒（清除盒号）
     * @apiName unSetBoxCode
     * @apiGroup entry
     * @apiParam {Number} catalogueId 目录id
     * @apiParam {String[]} ids 条目id数组
     * @apiParamExample {json} Request-Example
     * {
     * "catalogueId": 1,
     * "ids": ["xxxx-xxxx-xxxx"]
     * }
     * @apiError message 1.没有盒号字段 2.条目不存在 3.目录不存在
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_write_' + #catalogueId)")
    @RequestMapping(value = "/unSetBoxCode", method = RequestMethod.POST)
    public void unSetBoxCode(@JsonParam List<String> ids, @JsonParam int catalogueId) {
        Catalogue catalogue = catalogueService.get(catalogueId);
        if (catalogue == null) {
            throw new InvalidArgumentException("目录不存在");
        }

        Set<String> boxCodes = entryService.unBox(catalogueId, ids);

        applicationContext.publishEvent(new BoxInsideChangeEvent(this, catalogueId, catalogue.getArchivesId(), boxCodes));
    }

    /**
     * @api {get} /entry/statisticsTypeTerm 统计（按档案类型-保管期限）
     * @apiName statisticsTypeTerm
     * @apiGroup entry
     * @apiParam {Number} fondsId 全宗号（url参数）
     * @apiParam {Number} beginYear 起始年度（url参数）
     * @apiParam {Number} endYear 截止年度（url参数）
     * @apiSuccessExample {json} Response-Example
     * {
     * "items": [
     * {
     * "永久": 1,
     * "长期": 3,
     * "合计": 122,
     * "name": "文书档案",
     * "其他": 116,
     * "短期": 2
     * },
     * {
     * "永久": 15,
     * "长期": 30,
     * "合计": 135,
     * "name": "科研档案",
     * "其他": 80,
     * "短期": 10
     * }
     * ],
     * "fields":["永久","长期","合计","其他","短期"]
     * }
     */
    @RequestMapping(value = "/statisticsTypeTerm", method = RequestMethod.GET)
    public Map<String, Object> statisticsTypeTerm(@RequestParam("fondsId") int fondsId
            , @RequestParam(name = "beginYear", defaultValue = "2000", required = false) int beginYear
            , @RequestParam(name = "endYear", defaultValue = "2050", required = false) int endYear) {
        return entryService.statisticsTypeTerm(fondsId, beginYear, endYear);
    }

    /**
     * @api {get} /entry/statisticsTypeYear 统计（按档案类型-保管期限）
     * @apiName statisticsTypeYear
     * @apiGroup entry
     * @apiParam {Number} fondsId 全宗号（url参数）
     * @apiParam {Number} beginYear 起始年度（url参数）
     * @apiParam {Number} endYear 截止年度（url参数）
     * @apiSuccessExample {json} Response-Example
     * {
     * "items": [
     * {
     * "name": "2011",
     * "文书档案": 1,
     * "科研档案": 3,
     * "合计": 122,
     * "设备档案": 116,
     * "产品档案": 2
     * },
     * {
     * "name": "2012",
     * "文书档案": 15,
     * "科研档案": 30,
     * "合计": 135,
     * "设备档案": 80,
     * "产品档案": 10
     * }
     * ],
     * "fields":["文书档案","科研档案","合计","设备档案","产品档案"]
     * }
     */
    @RequestMapping(value = "/statisticsTypeYear", method = RequestMethod.GET)
    public Map<String, Object> statisticsTypeYear(@RequestParam("fondsId") int fondsId
            , @RequestParam(name = "beginYear", defaultValue = "2000", required = false) int beginYear
            , @RequestParam(name = "endYear", defaultValue = "2050", required = false) int endYear) {
        return entryService.statisticsTypeYear(fondsId, beginYear, endYear);
    }

    /**
     * @api {get} /entry/excelTemplate 获取导入条目模板
     * @apiName excelTemplate
     * @apiGroup entry
     * @apiParam {Number} catalogueId 目录ID（url参数）
     * @apiSuccessExample {json} Response-Example
     */
//    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_read_' + #entry.catalogueId)")
    @RequestMapping(value = "/excelTemplate", method = RequestMethod.GET)
    public void excelTemplate(@RequestParam("catalogueId") int catalogueId, HttpServletResponse response) {
        XSSFWorkbook wb = entryService.excelTemplate(catalogueId);

        String fileName = archivesService.findArchivesNameByCatalogue_CatalogueId(catalogueId);

        try {
            response.setContentType("application/octet-stream");
            response.setHeader("content-type", "application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

            OutputStream os = response.getOutputStream();
            wb.write(os);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @api {post} /entry/importEntry 批量导入条目
     * @apiName importEntry
     * @apiGroup entry
     * @apiParam {File} file Excel文件
     * @apiParamExample {json} Request-Example:
     */
//    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('archive_entry_write_' + #entry.catalogueId)")
    @RequestMapping(value = "/importEntry", method = RequestMethod.POST)
    public void importEntry(@RequestParam("catalogueId") int catalogueId, @RequestParam("file") MultipartFile file, @SessionAttribute UserCredential LOGIN_USER) {
        //先存入本地
        File tmpFile = new File(file.getOriginalFilename());
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            byte[] bytes = file.getBytes();
            fos = new FileOutputStream(tmpFile);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
            bos.flush();

            entryService.importEntry(catalogueId, tmpFile, LOGIN_USER.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("文件上传失败");
        } finally {
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
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
        }
    }


    @Async
    @EventListener
    @Transactional
    public void resetBoxCount(BoxInsideChangeEvent boxInsideChangeEvent) throws InterruptedException {
        System.out.println("处理盒变更事件");
        int catalogueId = boxInsideChangeEvent.getCatalogueId();
        int archiveId = boxInsideChangeEvent.getArchiveId();
        Collection<String> boxCodes = boxInsideChangeEvent.getBoxCodes();
        Map<String, GroupCount> list =
                entryService.groupCountPageCountByBox(boxCodes, catalogueId)
                        .stream().collect(Collectors.toMap(a -> a.key, a -> a));

        Map<String, List<String>> group = entryService.groupByBox(boxCodes, catalogueId);

        boxCodes.forEach(a -> {
            int files = 0;
            int pages = 0;
            GroupCount groupCount = list.getOrDefault(a, null);
            if (groupCount != null) {
                pages = groupCount.count;
            }
            //TODO @lijie 优化到循环外面
            List<String> boxIds = group.getOrDefault(a, null);
            if (boxIds != null && boxIds.size() > 0) {
                files = originalTextService.countByCatalogueIdAndEntryIdIn(catalogueId, boxIds);
            }
            boxService.updateTotal(a, archiveId, pages, files);
        });
    }

    @EventListener
    public void validateBoxNumber(EntryBoxNumberValidateEvent event) {
        boolean exists = boxService.existsByCodeAndArchivesId(event.getArchiveId(), event.getBoxCode());

        if (!exists) {
            throw new EntryValueConverException(String.format("盒号(%s)不存在", event.getBoxCode()));
        }
    }

    @Async
    @EventListener
    public void entryUnBox(BoxDeleteEvent boxDeleteEvent) {

        int catalogueId = boxDeleteEvent.getCatalogueId();
        List<Integer> boxIds = boxDeleteEvent.getBoxIds();

        List<String> boxCodes = boxService.getCodeByIds(boxIds);

        entryService.unBoxByBoxCode(catalogueId, boxCodes);
    }
}
