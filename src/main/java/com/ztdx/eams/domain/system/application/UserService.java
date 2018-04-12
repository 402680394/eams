package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.basic.exception.UnauthorizedException;
import com.ztdx.eams.domain.system.model.User;
import com.ztdx.eams.domain.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


/**
 * Created by li on 2018/3/22.
 */
@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
     *  通过id删除用户
     */
    @Transactional
    public void delete(int id) {
        userRepository.deleteById(id);
    }

    /**
     *  通过id批量删除用户
     */
    @Transactional
    public void listDelete(List<Integer> list) {
        for (int id:list){
            userRepository.deleteById(id);
        }
    }

    /**
     *  通过id批量重置用户密码
     */
    @Transactional
    public void listPassReset(List<Integer> list) {
        String password="111111";
        for (int id:list){
            userRepository.updatePwdById(id,password);
        }
    }

    /**
     *  通过id锁定|解锁用户
     */
    public void changeFlag(int id, int flag) {
        userRepository.updateFlagById(id,flag);
    }
}
