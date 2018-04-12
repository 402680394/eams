package com.ztdx.eams.controller;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.system.application.UserService;
import com.ztdx.eams.domain.system.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;


/**
 * Created by li on 2018/3/22.
 */
@RestController
@RequestMapping(value = "/user")
public class UserController {

    private final UserService userService;


    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * @apiDefine ErrorExample
     * @apiErrorExample {json} Error-Response:
     *     {"error": {"timestamp":1,"code":状态码,"message":"错误消息","path":"/home"}}
     */

    /**
     * @api {post} /user/login 用户登录
     * @apiName login
     * @apiGroup user
     * @apiParam {String} username 用户名
     * @apiParam {String} password 密码
     * @apiParamExample {json} Request-Example:
     *      {"username":"lht753951","password":"123456"}
     * @apiError (Error 401) message 1-用户不存在; 2-密码错误; 3-该用户已被禁止使用.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public void login(@JsonParam String username, @JsonParam String password, HttpSession session) {

        User user = userService.login(username, password);
        UserCredential userCredential = new UserCredential(user.getId());
        session.setAttribute("LOGIN_USER", userCredential);
    }

    /**
     * @api {delete} /user/{id} 删除用户
     * @apiName delete
     * @apiGroup user
     * @apiParam {int} id 用户ID（url占位符）
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        userService.delete(id);
    }

    /**
     * @api {delete} /user/list 批量删除用户
     * @apiName listDelete
     * @apiGroup user
     * @apiParam {int} id 用户ID
     * @apiParamExample {json} Request-Example:
     *                          [1,2,3]
     */
    @RequestMapping(value = "/list", method = RequestMethod.DELETE)
    public void listDelete(@RequestBody List<Integer> list) {
        userService.listDelete(list);
    }
    /**
     * @api {patch} /user/listPassReset 批量重置用户密码
     * @apiName listPassReset
     * @apiGroup user
     * @apiParam {int} id 用户ID
     * @apiParamExample {json} Request-Example:
     *                          [1,2,3]
     */
    @RequestMapping(value = "/listReset", method = RequestMethod.PATCH)
    public void listPassReset(@RequestBody List<Integer> list) {
        userService.listPassReset(list);
    }

    /**
     * @api {patch} /user/{id}/lock 锁定|解锁用户
     * @apiName changeFlag
     * @apiGroup user
     * @apiParam {int} id 用户ID（url占位符）
     * @apiParam {int} flag 状态（可选值：0-可用，1-禁用）（url参数）
     */
    @RequestMapping(value = "/{id}/lock", method = RequestMethod.PATCH)
    public void changeFlag(@PathVariable("id") int id,@RequestParam int flag) {
        userService.changeFlag(id,flag);
    }

}
