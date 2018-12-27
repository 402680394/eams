package com.ztdx.eams.controller.store;

import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.store.application.ShelfCellService;
import com.ztdx.eams.domain.store.model.ShelfCell;
import com.ztdx.eams.domain.store.repository.ShelfCellRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/shelf/cell")
public class ShelfCellController {

    private ShelfCellService shelfCellService;

    public ShelfCellController(ShelfCellService shelfCellService) {
        this.shelfCellService = shelfCellService;
    }

    /**
     * @api {put} /shelf/cell/{id} 修改密集架格
     * @apiName update_shelfCell
     * @apiGroup shelf
     * @apiParam {Number} id 密集架格id(path参数)
     * @apiParam {String} barCode 条码
     * @apiParam {String} pointCode 库位码
     * @apiError message 1.条码已存在 2.库位码已存在 3.密集架格不存在
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_write')")
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void update(
            @PathVariable("id") int id
            , @JsonParam String barCode
            , @JsonParam String pointCode
    ){
        shelfCellService.update(id, barCode, pointCode);
    }

    /**
     * @api {get} /shelf/cell?shelfSectionId={shelfSectionId}&page={page}&size={size} 密集架格列表
     * @apiName list_shelfCell
     * @apiGroup shelf
     * @apiParam {Number} shelfSectionId 密集架列id(QueryString参数)
     * @apiParam {Number} page 页码(QueryString)
     * @apiParam {Number} size 页行数(QueryString)
     * @apiSuccess {Object[]} content 列表
     * @apiSuccess {Number} content.id 密集架格id
     * @apiSuccess {String} content.pointCode 库位码
     * @apiSuccess {String} content.code 密集架格编码
     * @apiSuccess {Number} content.shelfSectionId 密集架列id
     * @apiSuccess {Number} content.side 左右面 1:左面 2:右面
     * @apiSuccess {String} content.barCode 条形码
     * @apiSuccess {Number} content.columnNo 节号
     * @apiSuccess {Number} content.rowNo 层号
     * @apiSuccess {Number} content.sectionCellLength 节长度
     * @apiSuccess {String} content.remark 备注
     * @apiSuccess {Number} totalElements 总记录数
     * @apiSuccess {Number} totalPages 总页数
     * @apiSuccessExample {json} Response-Example
     * {
     *     "data": {
     *         "content": [
     *             {
     *                 "id": 1,
     *                 "shelfSectionId": 1,
     *                 "shelfId": 1,
     *                 "storageId": 11,
     *                 "fondsId": 2,
     *                 "pointCode": "SJZ_0001-CS01-J-1-左-1-1",
     *                 "code": "左-1-1",
     *                 "side": 1,
     *                 "barCode": null,
     *                 "columnNo": 1,
     *                 "rowNo": 1,
     *                 "sectionCellLength": 900,
     *                 "remark": null
     *             }
     *         ]
     *         "totalElements": 72,
     *         "totalPages": 4
     *     }
     * }
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_storage_read')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Page<ShelfCell> list(
            @RequestParam int shelfSectionId
            , @RequestParam(value = "page", required = false, defaultValue = "1") int page
            , @RequestParam(value ="size", required = false, defaultValue = "15") int size
    ){
        return shelfCellService.findByShelfSectionId(shelfSectionId, PageRequest.of(page-1, size));
    }
}
