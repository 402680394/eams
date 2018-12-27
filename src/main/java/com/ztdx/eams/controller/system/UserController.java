package com.ztdx.eams.controller.system;

import com.ztdx.eams.basic.UserCredential;
import com.ztdx.eams.basic.exception.ForbiddenException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.params.JsonParam;
import com.ztdx.eams.domain.system.application.OrganizationService;
import com.ztdx.eams.domain.system.application.PermissionService;
import com.ztdx.eams.domain.system.application.UserService;
import com.ztdx.eams.domain.system.application.event.UserAddEvent;
import com.ztdx.eams.domain.system.model.Organization;
import com.ztdx.eams.domain.system.model.User;
import com.ztdx.eams.query.SystemQuery;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
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

    private final SystemQuery systemQuery;

    private final AuthenticationManager authenticationManager;

    private final PermissionService permissionService;

    private final OrganizationService organizationService;

    private final ApplicationContext applicationContext;

    @Autowired
    public UserController(UserService userService, SystemQuery systemQuery, AuthenticationManager authenticationManager, PermissionService permissionService, OrganizationService organizationService, ApplicationContext applicationContext) {
        this.userService = userService;
        this.systemQuery = systemQuery;
        this.authenticationManager = authenticationManager;
        this.permissionService = permissionService;
        this.organizationService = organizationService;
        this.applicationContext = applicationContext;
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
     * @apiParam {String{20}} username 用户名
     * @apiParam {String{100}} password 密码
     * @apiSuccess (Success 200) {Number} id 用户ID.
     * @apiError (Error 401) message 1-用户不存在; 2-密码错误; 3-该用户已被禁止使用.
     * @apiUse ErrorExample
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Map<String, Object> login(@JsonParam String username, @JsonParam String password, HttpSession session) {

        User user = userService.login(username, password);
        UserCredential userCredential = new UserCredential(user.getId(), user.getName());
        session.setAttribute(UserCredential.KEY, userCredential);
        HashMap resultMap = new HashMap();
        resultMap.put("id", user.getId());
        resultMap.put("name", user.getName());

        //使用spring security做认证
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return resultMap;
    }

    /**
     * @api {delete} /user/{id} 删除用户
     * @apiName delete
     * @apiGroup user
     * @apiParam {Number} id 用户ID（url占位符）
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_organization_user_admin')")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        userService.delete(id);
    }

    /**
     * @api {delete} /user/list 批量删除用户
     * @apiName listDelete
     * @apiGroup user
     * @apiParam {Number} id 用户ID
     * @apiParamExample {json} Request-Example:
     * [1,2,3]
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_organization_user_admin')")
    @RequestMapping(value = "/list", method = RequestMethod.DELETE)
    public void listDelete(@RequestBody List<Integer> list) {
        userService.listDelete(list);
    }

    /**
     * @api {put} /user/listReset 批量重置用户密码
     * @apiName listReset
     * @apiGroup user
     * @apiParam {Number} id 用户ID
     * @apiParamExample {json} Request-Example:
     * [1,2,3]
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @RequestMapping(value = "/listReset", method = RequestMethod.PUT)
    public void listPassReset(@RequestBody List<Integer> list) {
        userService.listPassReset(list);
    }

    /**
     * @api {put} /user/{id}/lock 锁定|解锁用户
     * @apiName lock
     * @apiGroup user
     * @apiParam {Number} id 用户ID（url占位符）
     * @apiParam {Number{0-1}} flag 状态（可选值：0-可用，1-禁用）（url参数）
     */
    @RequestMapping(value = "/{id}/lock", method = RequestMethod.PUT)
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_organization_user_admin')")
    public void lock(@PathVariable("id") int id, @RequestParam int flag) {
        userService.changeFlag(id, flag);
    }

    /**
     * @api {get} /user/list 获取用户列表
     * @apiName list
     * @apiGroup user
     * @apiParam {Number} pageNum 每页条数（默认为15）（url参数）
     * @apiParam {Number} pageSize 页码（默认为1）（url参数）
     * @apiParam {Number} organizationId 机构ID（url参数）
     * @apiParam {String} key 搜索值（搜索全部不传本值）（url参数）
     * @apiSuccess (Success 200) {Number} id 用户ID.
     * @apiSuccess (Success 200) {String} name 姓名.
     * @apiSuccess (Success 200) {String} workers 工号.
     * @apiSuccess (Success 200) {Number} organiztionId 所属机构ID.
     * @apiSuccess (Success 200) {String} username 用户名.
     * @apiSuccess (Success 200) {String} phone 电话.
     * @apiSuccess (Success 200) {String} email 邮箱.
     * @apiSuccess (Success 200) {String} job 职位
     * @apiSuccess (Success 200) {String} remark 备注
     * @apiSuccess (Success 200) {Number} flag 状态
     * @apiSuccess (Success 200) {Number} total 数据总数
     * @apiSuccessExample {json} Success-Response:
     * {
     * "data":{
     * "items":[{"id": 用户ID,"name": "姓名","organiztionId": 所属机构ID,"workers": "工号","username": "用户名","phone": "电话","email": "邮箱","job": "职位","remark": "备注","flag": 状态}
     * {"id": 用户ID,"name": "姓名","workers": "工号","username": "用户名","phone": "电话","email": "邮箱","job": "职位","remark": "备注","flag": 状态}]
     * "total":2
     * }
     * }.
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Map<String, Object> list(@RequestParam("organizationId") int organizationId
            , @RequestParam(name = "key", required = false, defaultValue = "") String key
            , @RequestParam(name = "pageSize", required = false, defaultValue = "15") int pageSize
            , @RequestParam(name = "pageNum", required = false, defaultValue = "1") int pageNum) {
        Organization organization = organizationService.get(organizationId);
        if (organization == null) {
            throw new InvalidArgumentException("组织机构不存在");
        }

        if (!permissionService.hasAnyAuthority(
                "ROLE_ADMIN"
                , "global_organization_read"
                , "fonds_role_user_set_" + organization.getFondsId())) {
            throw new ForbiddenException("没有此组织机构的权限");
        }

        return systemQuery.getUserList(organizationId, key, pageNum, pageSize);
    }

    /**
     * @api {post} /user 新增用户
     * @apiName save
     * @apiGroup user
     * @apiParam {String{10}} name 姓名
     * @apiParam {String{20}} workers 工号（未输入传""值）
     * @apiParam {String{10}} username 用户名
     * @apiParam {Number} organizationId 所属机构ID
     * @apiParam {String{20}} phone 电话（未输入传""值）
     * @apiParam {String{50}} email 邮箱（未输入传""值）
     * @apiParam {String{20}} job 职位（未输入传""值）
     * @apiParam {String{100}} remark 备注（未输入传""值）
     * @apiError (Error 400) message 1.用户名已存在;2.机构不存在或已被删除.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_organization_user_admin')")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void save(@RequestBody User user) {
        user = userService.save(user);
        applicationContext.publishEvent(new UserAddEvent(this, user.getId()));
    }

    /**
     * @api {put} /user 修改用户信息
     * @apiName update
     * @apiGroup user
     * @apiParam {Number} id ID
     * @apiParam {String{10}} name 姓名
     * @apiParam {String{20}} workers 工号（未输入传""值）
     * @apiParam {String{20}} username 用户名
     * @apiParam {Number} organizationId 所属机构ID
     * @apiParam {String{20}} phone 电话（未输入传""值）
     * @apiParam {String{50}} email 邮箱（未输入传""值）
     * @apiParam {String{20}} job 职位（未输入传""值）
     * @apiParam {String{100}} remark 备注（未输入传""值）
     * @apiError (Error 400) message 1.用户名已存在;2.机构不存在或已被删除.
     * @apiUse ErrorExample
     */
    @PreAuthorize("hasAnyRole('ADMIN') || hasAnyAuthority('global_organization_user_admin')")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void update(@RequestBody User user) {
        userService.update(user);
    }

    /**
     * @api {get} /user/{id} 获取用户信息详情
     * @apiName get
     * @apiGroup user
     * @apiParam {Number} id 用户ID（url占位符）
     * @apiSuccess (Success 200) {String} name 姓名
     * @apiSuccess (Success 200) {String} workers 工号
     * @apiSuccess (Success 200) {String} username 用户名
     * @apiSuccess (Success 200) {Number} organizationId 所属机构ID
     * @apiSuccess (Success 200) {String} phone 电话
     * @apiSuccess (Success 200) {String} email 邮箱
     * @apiSuccess (Success 200) {String} job 职位
     * @apiSuccess (Success 200) {String} remark 备注
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"name":"姓名","workers":"工号","username":"用户名","organizationId":所属机构ID,"phone":"电话","email":"邮箱","job":"职位","remark":"备注"}}.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> get(@PathVariable("id") int id) {
        return systemQuery.getUser(UInteger.valueOf(id));
    }

    /**
     * @api {get} /user/self 获取当前登录用户信息
     * @apiName self
     * @apiGroup user
     * @apiSuccess (Success 200) {String} name 姓名
     * @apiSuccess (Success 200) {String} workers 工号
     * @apiSuccess (Success 200) {String} username 用户名
     * @apiSuccess (Success 200) {Number} organizationId 所属机构ID
     * @apiSuccess (Success 200) {String} organizationName 所属机构名称
     * @apiSuccess (Success 200) {String} phone 电话
     * @apiSuccess (Success 200) {String} email 邮箱
     * @apiSuccess (Success 200) {String} job 职位
     * @apiSuccess (Success 200) {String} remark 备注
     * @apiSuccessExample {json} Success-Response:
     * {"data":{"name":"姓名","workers":"工号","username":"用户名","organizationName":所属机构名称,"organizationId":所属机构ID,"phone":"电话","email":"邮箱","job":"职位","remark":"备注"}}.
     */
    @RequestMapping(value = "/self", method = RequestMethod.GET)
    public Map<String, Object> self(HttpSession session) {
        UserCredential userCredential = (UserCredential) session.getAttribute(UserCredential.KEY);
        return systemQuery.getUser(UInteger.valueOf(userCredential.getUserId()));
    }

}
