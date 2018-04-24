package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.exception.UnauthorizedException;
import com.ztdx.eams.domain.system.model.User;
import com.ztdx.eams.domain.system.repository.OrganizationRepository;
import com.ztdx.eams.domain.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Created by li on 2018/3/22.
 */
@Service
public class UserService {
    private final UserRepository userRepository;

    private final OrganizationRepository organizationRepository;

    private final String initPassword = "111111";

    @Autowired
    public UserService(UserRepository userRepository, OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    /**
     * 登录
     */
    public User login(String username, String password) {

        User user = userRepository.findByUsername(username);
        //验证
        if (user == null) {
            throw new UnauthorizedException("用户不存在");
        } else if (!password.equals(user.getPassword())) {
            throw new UnauthorizedException("密码错误");
        } else if (0 != (user.getFlag())) {
            throw new UnauthorizedException("该用户已被禁止使用");
        }
        return user;
    }

    /**
     * 通过id删除用户
     */
    @Transactional
    public void delete(int id) {
        if (userRepository.existsById(id)){
            userRepository.deleteById(id);
        }
    }

    /**
     * 通过id批量删除用户
     */
    @Transactional
    public void listDelete(List<Integer> list) {
        for (int id : list) {
            if (userRepository.existsById(id)){
                userRepository.deleteById(id);
            }
        }
    }

    /**
     * 通过id批量重置用户密码
     */
    @Transactional
    public void listPassReset(List<Integer> list) {
        for (int id : list) {
            if (userRepository.existsById(id)){
                userRepository.updatePwdById(id, initPassword);
            }
        }
    }

    /**
     * 通过id锁定|解锁用户
     */
    @Transactional
    public void changeFlag(int id, int flag) {
        if (userRepository.existsById(id)){
            userRepository.updateFlagById(id, flag);
        }
    }

    /**
     * 添加用户
     */
    @Transactional
    public void save(User user) {
        //验证
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new InvalidArgumentException("用户名已存在");
        }
        if (!organizationRepository.existsById(user.getOrganizationId())) {
            throw new InvalidArgumentException("机构不存在或已被删除");
        }
        //设置初始密码
        user.setPassword(initPassword);
        userRepository.save(user);
    }

    /**
     * 修改用户信息
     */
    @Transactional
    public void update(User user) {
        //验证用户名
        if (!userRepository.existsByUsernameAndId(user.getUsername(), user.getId())) {
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new InvalidArgumentException("用户名已存在");
            }
        }
        if (!organizationRepository.existsById(user.getOrganizationId())) {
            throw new InvalidArgumentException("机构不存在或已被删除");
        }
        //修改信息
        userRepository.updateById(user);
    }
}
