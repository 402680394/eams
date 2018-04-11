package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.exception.UnauthorizedException;
import com.ztdx.eams.domain.system.model.User;
import com.ztdx.eams.domain.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
        } else if (0 != (user.getStatus())) {
            throw new UnauthorizedException("该用户已被禁止使用");
        }
        return user;
    }
}
