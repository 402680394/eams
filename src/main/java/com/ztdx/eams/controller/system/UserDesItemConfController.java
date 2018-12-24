package com.ztdx.eams.controller.system;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.domain.archives.application.CatalogueService;
import com.ztdx.eams.domain.archives.application.DescriptionItemService;
import com.ztdx.eams.domain.archives.application.event.DescriptionItemAddEvent;
import com.ztdx.eams.domain.archives.application.event.DescriptionItemDeleteEvent;
import com.ztdx.eams.domain.archives.model.Catalogue;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.system.application.UserDesItemConfService;
import com.ztdx.eams.domain.system.application.UserService;
import com.ztdx.eams.domain.system.application.event.UserAddEvent;
import com.ztdx.eams.domain.system.model.User;
import com.ztdx.eams.domain.system.model.UserDesItemConf;
import com.ztdx.eams.query.SystemQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/userDesItemConf")
public class UserDesItemConfController {

    private final SystemQuery systemQuery;

    private final UserDesItemConfService userDesItemConfService;

    private final CatalogueService catalogueService;

    private final DescriptionItemService descriptionItemService;

    private final UserService userService;

    @Autowired
    public UserDesItemConfController(SystemQuery systemQuery, UserDesItemConfService userDesItemConfService, CatalogueService catalogueService, DescriptionItemService descriptionItemService, UserService userService) {
        this.systemQuery = systemQuery;
        this.userDesItemConfService = userDesItemConfService;
        this.catalogueService = catalogueService;
        this.descriptionItemService = descriptionItemService;
        this.userService = userService;
    }

    /**
     * @api {get} /userDesItemConf/list 获取当前用户某个目录的著录项列表配置
     * @apiName list
     * @apiGroup userDesItemConf
     * @apiParam {Number} catalogueId 所属目录ID(url参数)
     * @apiSuccess (Success 200) {Number} id 配置ID.
     * @apiSuccess (Success 200) {String} metadataName 著录项元数据名称.
     * @apiSuccess (Success 200) {String} displayName 著录项显示名称.
     * @apiSuccess (Success 200) {Number} orderNumber 排序号.
     * @apiSuccess (Success 200) {Number} width 宽度.
     * @apiSuccessExample {json} Success-Response:
     * {"data": {"items": [{"id": 1,"metadataName": "著录项元数据名称","displayName": "著录项显示名称","orderNumber": 1,"width": 80}]}}.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<Map<String, Object>> list(@RequestParam("catalogueId") int catalogueId, @SessionAttribute UserCredential LOGIN_USER) {
        return systemQuery.getUserDesItemConfByCatalogueId(UInteger.valueOf(catalogueId), UInteger.valueOf(LOGIN_USER.getUserId()));
    }

    /**
     * @api {put} /userDesItemConf/{up},{down} 移动当前用户某个目录著录项列表位置
     * @apiName move
     * @apiGroup userDesItemConf
     * @apiParam {Number} up 配置ID(url占位符)
     * @apiParam {Number} down 配置ID(url占位符)
     */
    @RequestMapping(value = "/{up},{down}", method = RequestMethod.PUT)
    public void move(@PathVariable("up") int up, @PathVariable("down") int down) {
        userDesItemConfService.priority(up, down);
    }

    /**
     * @api {put} /userDesItemConf/updateWidth 修改当前用户某个目录著录项列表宽度
     * @apiName updateWidth
     * @apiGroup userDesItemConf
     * @apiParam {Number} id 配置ID
     * @apiParam {Number} width 宽度
     * @apiParamExample {json} Request-Example:
     * [{"id": 1,"width": 80},{"id": 2,"width": 80}].
     */
    @RequestMapping(value = "/updateWidth", method = RequestMethod.PUT)
    public void updateWidth(@RequestBody List<Map<String, Integer>> param) {
        List<Integer> ids = new ArrayList<>();
        param.forEach(a -> ids.add(a.get("id")));
        List<UserDesItemConf> userDesItemConfs = userDesItemConfService.getAllByIds(ids);
        userDesItemConfs.forEach(userDesItemConf -> {
            param.forEach(p -> {
                if (userDesItemConf.getId() == p.get("id")) {
                    userDesItemConf.setWidth(p.get("width"));
                }
            });
        });
        userDesItemConfService.saveAll(userDesItemConfs);
    }

    @Transactional
    @EventListener
    public void addConf(UserAddEvent userAddEvent) {
        List<Catalogue> catalogues = catalogueService.getAll();

        List<UserDesItemConf> userDesItemConfs = new ArrayList<>();
        for (Catalogue catalogue : catalogues) {
            int order = 1;
            List<DescriptionItem> descriptionItems = descriptionItemService.findByCatalogueId(catalogue.getId());
            for (DescriptionItem descriptionItem : descriptionItems) {
                UserDesItemConf userDesItemConf = new UserDesItemConf();
                userDesItemConf.setUserId(userAddEvent.getUserId());
                userDesItemConf.setCatalogueId(catalogue.getId());
                userDesItemConf.setDescriptionItemId(descriptionItem.getId());
                userDesItemConf.setOrderNumber(order);
                userDesItemConfs.add(userDesItemConf);
                order++;
            }
        }
        userDesItemConfService.saveAll(userDesItemConfs);
    }

    @Transactional
    @EventListener
    public void addConf(DescriptionItemAddEvent descriptionItemAddEvent) {
        List<User> users = userService.findAll();

        List<UserDesItemConf> userDesItemConfs = new ArrayList<>();
        List<DescriptionItem> descriptionItems = descriptionItemAddEvent.getDescriptionItems();
        int catalogueId = descriptionItems.get(0).getCatalogueId();
        for (User user : users) {
            Integer order = userDesItemConfService.findMaxOrderNumber(catalogueId, user.getId());
            if(null==order){
                order=1;
            }
            for (DescriptionItem descriptionItem : descriptionItems) {
                UserDesItemConf userDesItemConf = new UserDesItemConf();
                userDesItemConf.setUserId(user.getId());
                userDesItemConf.setCatalogueId(catalogueId);
                userDesItemConf.setDescriptionItemId(descriptionItem.getId());
                userDesItemConf.setOrderNumber(order);
                userDesItemConfs.add(userDesItemConf);
                order++;
            }
        }
        userDesItemConfService.saveAll(userDesItemConfs);
    }

    @EventListener
    public void delConf(DescriptionItemDeleteEvent descriptionItemDeleteEvent) {
        userDesItemConfService.deleteAllByDescriptionItemIdIn(descriptionItemDeleteEvent.getDescriptionItemIds());
    }
}
