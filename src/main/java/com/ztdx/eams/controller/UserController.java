package com.ztdx.eams.controller;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.system.application.UserService;
import com.ztdx.eams.domain.system.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.Max;
import java.util.HashMap;
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
     *     {"error": {"timestamp":1,"code":500,"message":"","path":"/home"}}
     */

    /**
     * @api {post} /user/login 用户登录
     * @apiName login
     * @apiGroup user
     * @apiParam {String} username 用户名
     * @apiParam {String} password 密码
     * @apiParamExample {json} Request-Example:
     * {"data":{"username":"lht753951","password":"123456"}}
     * @apiError (Error 401) message 1.用户不存在; 2.密码错误; 3.该用户已被禁止使用.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public void login(@JsonParam String username, @JsonParam String password, HttpSession session) {

        User user = userService.login(username, password);
        UserCredential userCredential = new UserCredential(user.getId());
        session.setAttribute("LOGIN_USER", userCredential);
    }


}
