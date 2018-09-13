package com.ztdx.eams.domain.store.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.store.model.BoxCodeRule;
import com.ztdx.eams.domain.store.repository.BoxCodeRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoxCodeRuleService {

    private final BoxCodeRuleRepository boxCodeRuleRepository;

    public BoxCodeRuleService(BoxCodeRuleRepository boxCodeRuleRepository) {
        this.boxCodeRuleRepository = boxCodeRuleRepository;
    }

    /**
     * 修改排序号
     */
    @Transactional
    public void priority(int upId, int downId) {
        BoxCodeRule up = boxCodeRuleRepository.findById(upId).orElse(null);
        BoxCodeRule down = boxCodeRuleRepository.findById(downId).orElse(null);
        if (up == null || down == null) {
            throw new InvalidArgumentException("对象不存在或已被删除");
        }
        if (up.getType() == 4 || down.getType() == 4) {
            return;
        }
        boxCodeRuleRepository.updateOrderNumberById(upId, down.getOrderNumber());
        boxCodeRuleRepository.updateOrderNumberById(downId, up.getOrderNumber());
    }

    @Transactional
    public void delete(int id) {
        boxCodeRuleRepository.deleteById(id);
    }

    @Transactional
    public void save(BoxCodeRule boxCodeRule) {
        BoxCodeRule b = boxCodeRuleRepository.findByArchivesIdAndType(boxCodeRule.getArchivesId(), 4).orElse(null);
        //只能有一个流水号
        if (null == b) {
            throw new BusinessException("只能有一个流水号");
        }

        //设置排序号
        Integer orderNumber = boxCodeRuleRepository.findMaxOrderNumber(boxCodeRule.getArchivesId());
        if (orderNumber != null) {
            boxCodeRule.setOrderNumber(orderNumber + 1);
        } else {
            boxCodeRule.setOrderNumber(1);
        }
        boxCodeRuleRepository.save(boxCodeRule);
        //流水号只能位于最后
        b.setOrderNumber(boxCodeRule.getOrderNumber() + 1);
        boxCodeRuleRepository.save(b);

    }

    @Transactional
    public void update(BoxCodeRule boxCodeRule) {
        BoxCodeRule b = boxCodeRuleRepository.findByArchivesIdAndType(boxCodeRule.getArchivesId(), 4).orElse(null);
        //只能有一个流水号
        if (null == b) {
            throw new BusinessException("只能有一个流水号");
        }
        boxCodeRuleRepository.updateById(boxCodeRule);
    }
}
