package com.ztdx.eams.controller;

import com.ztdx.eams.domain.archives.application.EntryService;
import com.ztdx.eams.domain.archives.model.Entry;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/entry")
public class EntryController {

    private EntryService entryService;

    public EntryController(EntryService entryService) {
        this.entryService = entryService;
    }


    /**
     * @api {post} /entry 新增条目
     * @apiName archives/entry
     * @apiGroup entry
     * @apiParam {String} catalogueId 档案目录id
     * @apiParam {Object} items 条目详细内容，是一个动态的key-value数组。
     * 以下举例条目有姓名(name)年龄(age)注册日期(regDate)爱好(interest)
     * @apiParam {String} items.name 姓名
     * @apiParam {int} items.age 年龄
     * @apiParam {Date} items.regDate 注册日期
     * @apiParam {String[]} items.interest 爱好
     * @apiError (Error 400) message 1.档案目录不存在 2.其他数据验证错误
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody Entry entry){
        entryService.save(entry);
    }

    /**
     * @api {put} /entry/{id} 更新条目
     * @apiName archives/entry
     * @apiGroup entry
     * @apiParam {UUID} id 条目id（url占位符）
     * @apiParam {String} catalogueId 档案目录id
     * @apiParam {Object} items 条目详细内容，是一个动态的key-value数组。
     * 以下举例条目有姓名(name)年龄(age)注册日期(regDate)爱好(interest)
     * @apiParam {String} items.name 姓名
     * @apiParam {int} items.age 年龄
     * @apiParam {Date} items.regDate 注册日期
     * @apiParam {String[]} items.interest 爱好
     * @apiError (Error 400) message 1.档案目录不存在 2.其他数据验证错误
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void update(@PathVariable("id")UUID uuid, @RequestBody Entry entry){
        entry.setId(uuid);
        entryService.update(entry);
    }

}
