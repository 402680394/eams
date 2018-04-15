package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.system.model.Fonds;
import com.ztdx.eams.domain.system.repository.FondsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by li on 2018/4/15.
 */
@Service
public class FondsService {

    private final FondsRepository fondsRepository;

    @Autowired
    public FondsService(FondsRepository fondsRepository) {
        this.fondsRepository = fondsRepository;
    }

    /**
     * 新增全宗
     */
    @Transactional
    public void save(Fonds fonds) {

        int i = fondsRepository.countByCode(fonds.getCode());
        if (i != 0) {
            throw new InvalidArgumentException("全宗号已存在");
        }

        //设置同级机构优先级
        int orderNumber = fondsRepository.findMaxOrderNumber(fonds.getParentId(), fonds.getType());
        fonds.setOrderNumber(orderNumber + 1);
        //存储数据
        fondsRepository.save(fonds);
    }

    /**
     * 删除全宗
     */
    @Transactional
    public void delete(int id) {

        //删除子全宗
        List<Fonds> list = fondsRepository.findAllByParentId(id);
        if (!list.isEmpty()) {
            for (Fonds f : list) {
                delete(f.getId());
            }
        }
        //删除本全宗
        fondsRepository.deleteById(id);
    }

    /**
     * 修改全宗
     */
    public void update(Fonds fonds) {

        int i = fondsRepository.countByCode(fonds.getCode());
        if (i != 0) {
            throw new InvalidArgumentException("全宗号已存在");
        }
        //修改数据
        fondsRepository.updateById(fonds);
    }

    /**
     * 修改全宗排序优先级
     */
    public void priority(int upId, int downId) {

        Fonds up=fondsRepository.findById(upId);
        Fonds down=fondsRepository.findById(downId);

        if(up.getParentId()!=down.getParentId()){
            throw new InvalidArgumentException("上级全宗不一致");
        }
        fondsRepository.updateOrderNumberById(upId,down.getOrderNumber());
        fondsRepository.updateOrderNumberById(downId,up.getOrderNumber());
    }
}
