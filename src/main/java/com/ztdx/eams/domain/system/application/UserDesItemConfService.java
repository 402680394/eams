package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.system.model.UserDesItemConf;
import com.ztdx.eams.domain.system.repository.UserDesItemConfRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserDesItemConfService {

    private final UserDesItemConfRepository userDesItemConfRepository;

    @Autowired
    public UserDesItemConfService(UserDesItemConfRepository userDesItemConfRepository) {
        this.userDesItemConfRepository = userDesItemConfRepository;
    }

    /**
     * 修改排序优先级
     */
    @Transactional
    public void priority(int upId, int downId) {

        Optional<UserDesItemConf>  up = userDesItemConfRepository.findById(upId);
        Optional<UserDesItemConf> down = userDesItemConfRepository.findById(downId);
        if (!up.isPresent() || !down.isPresent()) {
            throw new InvalidArgumentException("该项不存在或已被删除");
        }
        userDesItemConfRepository.updateOrderNumberById(upId, down.get().getOrderNumber());
        userDesItemConfRepository.updateOrderNumberById(downId, up.get().getOrderNumber());
    }

    public List<UserDesItemConf> getAllByIds(List<Integer> ids) {
        return userDesItemConfRepository.findAllByIdIn(ids);
    }

    @Transactional
    public void saveAll(List<UserDesItemConf> userDesItemConfs) {
        userDesItemConfRepository.saveAll(userDesItemConfs);
    }

    public List<UserDesItemConf> getByUserIdAndCatalogueId(int userId, int catalogueId) {
        return userDesItemConfRepository.findAllByUserIdAndCatalogueId(userId, catalogueId);
    }

    public Integer findMaxOrderNumber(int catalogueId, int userId) {
        return userDesItemConfRepository.findMaxOrderNumber(catalogueId, userId);
    }
    public void deleteAllByDescriptionItemIdIn(List<Integer> ids) {
        userDesItemConfRepository.deleteAllByDescriptionItemIdIn(ids);
    }
}
