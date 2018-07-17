package com.ztdx.eams.controller.store;

import com.ztdx.eams.basic.exception.NotFoundException;
import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.store.application.ShelfSectionService;
import com.ztdx.eams.domain.store.application.ShelfService;
import com.ztdx.eams.domain.store.model.Shelf;
import com.ztdx.eams.domain.store.model.ShelfSection;
import com.ztdx.eams.domain.store.model.event.ShelfCellDeletedEvent;
import com.ztdx.eams.query.StoreQuery;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/shelf/section")
public class ShelfSectionController {

    private ShelfSectionService shelfSectionService;

    private ShelfService shelfService;

    private ApplicationContext applicationContext;

    private StoreQuery storeQuery;

    public ShelfSectionController(ShelfSectionService shelfSectionService, ShelfService shelfService, ApplicationContext applicationContext, StoreQuery storeQuery) {
        this.shelfSectionService = shelfSectionService;
        this.shelfService = shelfService;
        this.applicationContext = applicationContext;
        this.storeQuery = storeQuery;
    }

    /**
     * @api {post} /shelf/section 新增密集架列
     * @apiName add_shelfSection
     * @apiGroup shelf
     * @apiParam {String} name 密集架列名称
     * @apiParam {String} code 密集架列编码
     * @apiParam {String} leftTag 左侧标签
     * @apiParam {String} rightTag 右侧标签
     * @apiParam {Number} shelfId 密集架id
     * @apiParam {Number="1","2","3","4"} shelfSectionType 密集架列类型 1:左面活动架 2:右面活动架 3:双面固定架 4:双面活动架
     * @apiParam {Number} sectionCellWidth 列宽度
     * @apiParam {Number} sectionColNum 节数
     * @apiParam {Number} sectionCellLength 节长度
     * @apiParam {Number} sectionRowNum 层数
     * @apiParam {Number} sectionCellHeight 层高度
     * @apiParam {String} remark 备注
     * @apiParamExample {json} Request-Example
     * {
     * 	   "name":"测试密集架名",
     * 	   "code":"JL-1",
     * 	   "shelfId":3,
     * 	   "shelfSectionType":1,
     * 	   "sectionCellWidth":500,
     * 	   "sectionColNum": 6,
     * 	   "sectionCellLength": 900,
     * 	   "sectionRowNum": 6,
     * 	   "sectionCellHeight": 330,
     * 	   "remark": "备注",
     * 	   "leftTag": "左面标签",
     * 	   "rightTag": "右侧标签"
     * }
     * @apiSuccess {Object} content 返回对象
     * @apiSuccess {Number} content.id 密集架列id
     * @apiSuccessExample {json} Response-Example
     * {
     *     "data": {
     *         "id": 9
     *     }
     * }
     * @apiError message 1.密集架列名称已存在 2.密集架列编码已存在
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ShelfSection save(@RequestBody ShelfSection shelfSection){
        return shelfSectionService.save(shelfSection);
    }

    /**
     * @api {post} /shelf/section/multi 批量新增密集架列
     * @apiName add_multi_shelfSection
     * @apiGroup shelf
     * @apiParam {Number} shelfId 密集架id
     * @apiParam {Number} sectionNum 列数
     * @apiParam {Number} sectionCellWidth 列宽度
     * @apiParam {Number} sectionColNum 节数
     * @apiParam {Number} sectionCellLength 节长度
     * @apiParam {Number} sectionRowNum 层数
     * @apiParam {Number} sectionCellHeight 层高度
     * @apiParam {String} sectionCodePrefix 列的名称前缀
     * @apiParam {String} sectionNamePrefix 列的编码前缀
     * @apiParam {Number} sectionStartSn 列的起始编号
     * @apiParamExample {json} Request-Example
     * {
     * 	   "shelfId":1,
     * 	   "sectionNum": 2,
     * 	   "shelfSectionType":1,
     * 	   "sectionCellWidth":500,
     * 	   "sectionColNum": 6,
     * 	   "sectionCellLength": 900,
     * 	   "sectionRowNum": 6,
     * 	   "sectionCellHeight": 330,
     * 	   "remark": "beizhu",
     * 	   "sectionNamePrefix":"测试批量列",
     * 	   "sectionCodePrefix":"PLL-",
     * 	   "sectionStartSn": 1
     * }
     * @apiError message 1.密集架不存在
     */
    @RequestMapping(value = "/multi", method = RequestMethod.POST)
    public void saveAll(
            @JsonParam int shelfId
            , @JsonParam int sectionNum
            , @JsonParam int sectionCellWidth
            , @JsonParam int sectionColNum
            , @JsonParam int sectionCellLength
            , @JsonParam int sectionRowNum
            , @JsonParam int sectionCellHeight
            , @JsonParam String sectionCodePrefix
            , @JsonParam String sectionNamePrefix
            , @JsonParam int sectionStartSn
    ){
        Shelf shelf =shelfService.get(shelfId);
        if (shelf == null){
            throw new NotFoundException("密集架不存在");
        }

        shelf.setSectionNum(sectionNum);
        shelf.setSectionCellWidth(sectionCellWidth);
        shelf.setSectionColNum(sectionColNum);
        shelf.setSectionCellLength(sectionCellLength);
        shelf.setSectionRowNum(sectionRowNum);
        shelf.setSectionCellHeight(sectionCellHeight);
        shelf.setSectionCodePrefix(sectionCodePrefix);
        shelf.setSectionNamePrefix(sectionNamePrefix);
        shelf.setSectionStartSn(sectionStartSn);

        shelfService.createSection(shelf);
    }

    /**
     * @api {put} /shelf/section/{id} 修改密集架列
     * @apiName update_shelfSection
     * @apiGroup shelf
     * @apiParam {Number} id 密集架id(path参数)
     * @apiParam {String} name 密集架列名称
     * @apiParam {String} code 密集架列编码
     * @apiParam {String} leftTag 左面标签
     * @apiParam {String} rightTag 右侧标签
     * @apiParam {Number="1","2","3","4"} shelfSectionType 密集架列类型 1:左面活动架 2:右面活动架 3:双面固定架 4:双面活动架
     * @apiParam {String} remark 备注
     * @apiParamExample {json} Request-Example
     * {
     * 	   "name":"测试1111",
     * 	   "code":"CS011111",
     * 	   "remark": "beizhu",
     * 	   "leftTag": "L",
     * 	   "rightTag": "R",
     * 	   "shelfSectionType": 1
     * }
     * @apiError message 1.密集架列名称已存在 2.密集架列编码已存在
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void update(
            @PathVariable("id") int id
            , @JsonParam String name
            , @JsonParam String code
            , @JsonParam String remark
            , @JsonParam String leftTag
            , @JsonParam String rightTag
            , @JsonParam int shelfSectionType
    ){
        ShelfSection section = new ShelfSection();
        section.setId(id);
        section.setName(name);
        section.setCode(code);
        section.setRemark(remark);
        section.setLeftTag(leftTag);
        section.setRightTag(rightTag);
        section.setShelfSectionType(shelfSectionType);

        shelfSectionService.update(section);
    }

    /**
     * @api {get} /shelf/section/{id} 获取密集架列信息
     * @apiName get_shelfSection
     * @apiGroup shelf
     * @apiParam {Number} id 密集架列id(path参数)
     * @apiSuccess  {String} name 密集架列名称
     * @apiSuccess {String} code 密集架列编码
     * @apiSuccess {String} remark 备注
     * @apiSuccess {String} leftTag 左面标签
     * @apiSuccess {String} rightTag 右侧标签
     * @apiSuccess {Number="1","2","3","4"} shelfSectionType 密集架列类型 1:左面活动架 2:右面活动架 3:双面固定架 4:双面活动架
     * @apiSuccessExample {json} Response-Example
     * {
     *     "data": {
     *         "id": 11,
     *         "name": "测试1111",
     *         "code": "CS011111",
     *         "leftTag": "L",
     *         "rightTag": "R",
     *         "shelfSectionType": 1,
     *         "remark": "beizhu"
     *     }
     * }
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ShelfSection get(@PathVariable("id") int id){
        ShelfSection shelfSection = shelfSectionService.get(id);
        if (shelfSection == null || shelfSection.getGmtDeleted() == 1){
            throw new NotFoundException("密集架列不存在");
        }
        return shelfSection;
    }

    /**
     * @api {delete} /shelf/section 删除密集架列
     * @apiName delete_shelfSection
     * @apiGroup shelf
     * @apiParam {Number[]} ids 要删除的密集架列id数组
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public void delete(@JsonParam List<Integer> ids){
        shelfSectionService.delete(ids);

        Collection<String> cellPointCodes = storeQuery.getCellIdsByShelfSectionIdIn(ids);

        applicationContext.publishEvent(new ShelfCellDeletedEvent(this, cellPointCodes));
    }
}
