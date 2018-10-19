package com.ztdx.eams.controller.store;

import com.ztdx.eams.basic.exception.NotFoundException;
import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.archives.application.ArchivesGroupService;
import com.ztdx.eams.domain.archives.application.ArchivesService;
import com.ztdx.eams.domain.archives.model.Archives;
import com.ztdx.eams.domain.archives.model.ArchivesGroup;
import com.ztdx.eams.domain.store.application.ShelfService;
import com.ztdx.eams.domain.store.application.StorageService;
import com.ztdx.eams.domain.store.model.Shelf;
import com.ztdx.eams.domain.store.model.Storage;
import com.ztdx.eams.domain.store.model.event.ShelfCellDeletedEvent;
import com.ztdx.eams.domain.system.application.RoleService;
import com.ztdx.eams.query.StoreQuery;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/shelf")
public class ShelfController {

    private ShelfService shelfService;

    private StorageService storageService;

    private StoreQuery storeQuery;

    private ApplicationContext applicationContext;

    private RoleService roleService;

    private ArchivesService archivesService;

    private ArchivesGroupService archivesGroupService;

    public ShelfController(ShelfService shelfService, StorageService storageService, StoreQuery storeQuery, ApplicationContext applicationContext, RoleService roleService, ArchivesService archivesService, ArchivesGroupService archivesGroupService) {
        this.shelfService = shelfService;
        this.storageService = storageService;
        this.storeQuery = storeQuery;
        this.applicationContext = applicationContext;
        this.roleService = roleService;
        this.archivesService = archivesService;
        this.archivesGroupService = archivesGroupService;
    }

    /**
     * @api {post} /shelf 新增密集架
     * @apiName add_shelf
     * @apiGroup shelf
     * @apiParam {String} name 密集架名称
     * @apiParam {String} code 密集架编码
     * @apiParam {Number} storageId 库房id
     * @apiParam {Number="1","2"} shelfType 密集架类型 1:自动 2:手动
     * @apiParam {Number} sectionNum 列数
     * @apiParam {Number} sectionCellWidth 列宽度
     * @apiParam {Number} sectionColNum 节数
     * @apiParam {Number} sectionCellLength 节长度
     * @apiParam {Number} sectionRowNum 层数
     * @apiParam {Number} sectionCellHeight 层高度
     * @apiParam {String} remark 备注
     * @apiParam {String} [sectionCodePrefix] 列的名称前缀
     * @apiParam {String} [sectionNamePrefix] 列的编码前缀
     * @apiParam {Number} [sectionStartSn] 列的起始编号
     * @apiParamExample {json} Request-Example
     * {
     * 	   "name":"测试",
     * 	   "code":"CS01",
     * 	   "storageId":11,
     * 	   "shelfType":1,
     * 	   "sectionNum":8,
     * 	   "sectionCellWidth":500,
     * 	   "sectionColNum": 6,
     * 	   "sectionCellLength": 900,
     * 	   "sectionRowNum": 6,
     * 	   "sectionCellHeight": 330,
     * 	   "remark": "beizhu",
     * 	   "sectionCodePrefix": "J-",
     * 	   "sectionNamePrefix": "密集架列",
     * 	   "sectionStartSn": 1
     * }
     * @apiSuccess {Number} id 密集架id
     * @apiSuccessExample {json} Response-Example
     * {
     *     "data": {
     *         "id": 1
     *     }
     * }
     * @apiError message 1.密集架名称已存在 2.密集架编码已存在 3.库房不存在
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_write')")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Shelf save(@RequestBody Shelf shelf){
        Storage storage = storageService.get(shelf.getStorageId());
        if (storage == null){
            throw new NotFoundException("库房不存在");
        }

        shelf.setFondsId(storage.getFondsId());

        return shelfService.save(shelf);
    }

    /**
     * @api {put} /shelf/{id} 修改密集架
     * @apiName update_shelf
     * @apiGroup shelf
     * @apiParam {Number} id 密集架id(path参数)
     * @apiParam {String} name 密集架名称
     * @apiParam {String} code 密集架编码
     * @apiParam {String} remark 备注
     * @apiError message 1.密集架名称已存在 2.密集架编码已存在 3.密集架不存在
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_write')")
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void update(@PathVariable("id") int id, @JsonParam String name, @JsonParam String code, @JsonParam String remark){

        Shelf shelf = new Shelf();

        shelf.setId(id);
        shelf.setCode(code);
        shelf.setName(name);
        shelf.setRemark(remark);

        shelfService.update(shelf);
    }

    /**
     * @api {get} /shelf/{id} 获取密集架信息
     * @apiName get_shelf
     * @apiGroup shelf
     * @apiParam {Number} id 密集架id(path参数)
     * @apiSuccess  {String} name 密集架名称
     * @apiSuccess {String} code 密集架编码
     * @apiSuccess {String} remark 备注
     * @apiSuccess {Number="1","2"} shelfType 密集架类型 1:自动 2:手动
     * @apiSuccessExample {json} Response-Example
     * {
     *     "data": {
     *         "id": 1,
     *         "name": "测试1",
     *         "code": "CSa01",
     *         "shelfType": 1,
     *         "remark": "beizhu1"
     *     }
     * }
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Shelf get(@PathVariable("id") int id){
        Shelf shelf = shelfService.get(id);
        if (shelf == null || shelf.getGmtDeleted() == 1){
            throw new NotFoundException("密集架不存在");
        }
        return shelf;
    }

    /**
     * @api {delete} /shelf 删除密集架
     * @apiName delete_shelf
     * @apiGroup shelf
     * @apiParam {Number[]} ids 要删除的密集架id数组
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_write')")
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public void delete(@JsonParam List<Integer> ids){
        shelfService.delete(ids);

        Collection<String> cellPointCodes = storeQuery.getCellIdsByShelfIdIn(ids);

        applicationContext.publishEvent(new ShelfCellDeletedEvent(this, cellPointCodes));
    }

    /**
     * @api {post} /shelf/{id}/copy 拷贝密集架
     * @apiName copy_shelf
     * @apiGroup shelf
     * @apiParam {Number} id 密集架id(path参数)
     * @apiParam {String} name 密集架名称
     * @apiParam {String} code 密集架编码
     * apiParam {String} remark 备注
     * @apiError message 1.密集架名称已存在 2.密集架编码已存在 3.不允许跨全宗复制
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_write')")
    @RequestMapping(value = "/{id}/copy", method = RequestMethod.POST)
    public void copy(@PathVariable("id") int id, @JsonParam String name, @JsonParam String code, @JsonParam String remark){
        Shelf shelf = shelfService.get(id);
        if (shelf == null || shelf.getGmtDeleted() == 1){
            throw new NotFoundException("密集架不存在");
        }

        shelf.setId(0);
        shelf.setName(name);
        shelf.setCode(code);
        shelf.setRemark(remark);

        shelfService.save(shelf);
    }

    /**
     * @api {get} /shelf?storageId={storageId} 密集架列表
     * @apiName list_shelf
     * @apiGroup shelf
     * @apiParam {Number} storageId 库房id(QueryString参数)
     * @apiSuccess {Object[]} data 列表
     * @apiSuccess {Number} data.id 密集架id
     * @apiSuccess {String} data.name 密集架名称
     * @apiSuccess {String} data.code 密集架编码
     * @apiSuccess {Number} data.storageId 库房id
     * @apiSuccess {String} data.remark 备注
     * @apiSuccess {Number="1","2"} data.shelfType 密集架类型 1:自动 2:手动
     * @apiSuccess {Number} data.sectionNum 列数
     * @apiSuccess {Object[]} data.children 密集架列
     * @apiSuccess {String} data.children.name 密集架列名称
     * @apiSuccess {String} data.children.code 密集架列编码
     * @apiSuccess {String} data.children.leftTag 左侧标签
     * @apiSuccess {String} data.children.rightTag 右侧标签
     * @apiSuccess {Number} data.children.shelfId 密集架id
     * @apiSuccess {Number="1","2","3","4"} data.children.shelfSectionType 密集架列类型 1:左面活动架 2:右面活动架 3:双面固定架 4:双面活动架
     * @apiSuccess {Number} data.children.sectionCellWidth 列宽度
     * @apiSuccess {Number} data.children.sectionColNum 节数
     * @apiSuccess {Number} data.children.sectionCellLength 节长度
     * @apiSuccess {Number} data.children.sectionRowNum 层数
     * @apiSuccess {Number} data.children.sectionCellHeight 层高度
     * @apiSuccess {String} data.children.remark 备注
     * @apiSuccessExample {json} Response-Example
     * {
     *     "data": [
     *         {
     *             "sectionNum": 11,
     *             "code": "CS01",
     *             "children": [
     *                 {
     *                     "id": 1,
     *                     "shelfId": 1,
     *                     "storageId": 11,
     *                     "fondsId": 2,
     *                     "name": "密集架列1",
     *                     "code": "J-1",
     *                     "leftTag": "J-1-l",
     *                     "rightTag": "J-1-r",
     *                     "shelfSectionType": 4,
     *                     "sectionColNum": 6,
     *                     "sectionRowNum": 6,
     *                     "sectionCellLength": 900,
     *                     "sectionCellWidth": 500,
     *                     "sectionCellHeight": 330,
     *                     "remark": null
     *                 }
     *             ],
     *             "name": "测试",
     *             "remark": "beizhu",
     *             "shelfType": 1,
     *             "id": 1,
     *             "storageId": 11
     *         }
     *     ]
     * }
     *
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_read')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<Map<String, Object>> list(@RequestParam int storageId){
        return shelfService.list(storageId);
    }


    /**
     * @api {get} /shelf/tree?archiveId={archiveId} 密集架列表（树形结构）
     * @apiName listByArchiveId
     * @apiGroup shelf
     * @apiParam {Number} archiveId 档案库id(QueryString参数)
     * @apiSuccess {Object[]} data 列表
     * @apiSuccess {Number} data.id 库房id
     * @apiSuccess {String} data.name 库房名称
     * @apiSuccess {String} data.nodeType 节点类型 storage:库房 shelf:密集架 shelfSection:密集架列
     * @apiSuccess {Object[]} data.children 下级密集架
     * @apiSuccess {Number} data.children.id 密集架id
     * @apiSuccess {String} data.children.name 密集架名称
     * @apiSuccess {String} data.children.nodeType 节点类型 storage:库房 shelf:密集架 shelfSection:密集架列
     * @apiSuccess {Object[]} data.children.children 密集架列
     * @apiSuccess {String} data.children.children.id 密集架列id
     * @apiSuccess {String} data.children.children.name 密集架列名称
     * @apiSuccess {String} data.children.children.nodeType 节点类型 storage:库房 shelf:密集架 shelfSection:密集架列
     * @apiSuccessExample {json} Response-Example
     * {
     *     "data": [
     *         {
     *             "children": [
     *                 {
     *                     "children": [
     *                         {
     *                             "name": "11",
     *                             "id": 13,
     *                             "nodeType": "shelfSection"
     *                         }
     *                     ],
     *                     "name": "测试1",
     *                     "id": 5,
     *                     "nodeType": "shelf"
     *                 }
     *             ],
     *             "name": "石家庄库房",
     *             "id": 11,
     *             "nodeType": "storage"
     *         }
     *     ]
     * }
     *
     */
    //@PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_read')")
    @RequestMapping(value = "/tree", method = RequestMethod.GET)
    public List<Map<String, Object>> listByArchiveId(@RequestParam int archiveId){

        Archives archives = archivesService.get(archiveId);
        if (archives == null){
            throw new NotFoundException("档案库不存在");
        }

        ArchivesGroup group = archivesGroupService.get(archives.getArchivesGroupId());
        if (group == null){
            throw new NotFoundException("档案库分组不存在");
        }

        return shelfService.listByFondsId(group.getFondsId());
    }
}
