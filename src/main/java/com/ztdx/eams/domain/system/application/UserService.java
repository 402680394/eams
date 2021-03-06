package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.exception.UnauthorizedException;
import com.ztdx.eams.domain.system.model.Fonds;
import com.ztdx.eams.domain.system.model.Role;
import com.ztdx.eams.domain.system.model.User;
import com.ztdx.eams.domain.system.repository.OrganizationRepository;
import com.ztdx.eams.domain.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


/**
 * Created by li on 2018/3/22.
 */
@Service
public class UserService {
    private final UserRepository userRepository;

    private final OrganizationRepository organizationRepository;

    private final String initPassword = "111111";

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, OrganizationRepository organizationRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 登录
     */
    public User login(String username, String password) {

        User user = userRepository.findByUsername(username);
        //验证
        if (user == null|| user.getGmtDeleted()!=0) {
            throw new UnauthorizedException("用户不存在");
        } else if (!passwordEncoder.matches(password, user.getPassword())) {
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
        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()|| optionalUser.get().getGmtDeleted()==1) {
            throw new BusinessException("用户不存在或已被删除");
        }
        userRepository.updateGmtDeletedById(id,1);

    }

    /**
     * 通过id批量删除用户
     */
    @Transactional
    public void listDelete(List<Integer> ids) {
        ids.forEach(id->{
            if (userRepository.existsById(id)) {
                userRepository.updateGmtDeletedById(id,1);
            }
        });
    }

    /**
     * 通过id批量重置用户密码
     */
    @Transactional
    public void listPassReset(List<Integer> list) {
        for (int id : list) {
            if (userRepository.existsById(id)) {
                userRepository.updatePwdById(id, passwordEncoder.encode(initPassword));
            }
        }
    }

    /**
     * 通过id锁定|解锁用户
     */
    @Transactional
    public void changeFlag(int id, int flag) {
        if (userRepository.existsById(id)) {
            userRepository.updateFlagById(id, flag);
        }
    }

    /**
     * 添加用户
     */
    @Transactional
    public User save(User user) {
        //验证
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new InvalidArgumentException("用户名已存在");
        }
        if (!organizationRepository.existsById(user.getOrganizationId())) {
            throw new InvalidArgumentException("机构不存在或已被删除");
        }
        //设置初始密码
        user.setPassword(passwordEncoder.encode(initPassword));
        return userRepository.save(user);
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

    /**
     * 密码加密，不经常使用
     */
    public void resetPwd() {
        List<User> list = userRepository.findAll();
        for (User u : list) {
            String pwd = u.getPassword();
            pwd = passwordEncoder.encode(pwd);
            u.setPassword(pwd);
        }
        userRepository.saveAll(list);
    }

    public User getByUserName(String userName) {
        return userRepository.findByUsername(userName);
    }

    public List<User> findAllById(Collection<Integer> ids) {
        return userRepository.findAllById(ids);
    }

    public List<User> findAll(){
        return userRepository.findAll();
    }
}
