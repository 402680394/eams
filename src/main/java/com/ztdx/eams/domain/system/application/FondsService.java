package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.system.model.Fonds;
import com.ztdx.eams.domain.system.model.Organization;
import com.ztdx.eams.domain.system.model.Role;
import com.ztdx.eams.domain.system.repository.FondsRepository;
import com.ztdx.eams.domain.system.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by li on 2018/4/15.
 */
@Service
public class FondsService {

    private final FondsRepository fondsRepository;

    private final OrganizationRepository organizationRepository;

    @Autowired
    public FondsService(FondsRepository fondsRepository, OrganizationRepository organizationRepository) {
        this.fondsRepository = fondsRepository;
        this.organizationRepository = organizationRepository;
    }

    /**
     * 新增全宗
     */
    @Transactional
    public void save(Fonds fonds, ArrayList<Integer> associationList) {

        if (fondsRepository.existsByCode(fonds.getCode())) {
            throw new InvalidArgumentException("全宗号已存在");
        }
        //设置排序
        Integer orderNumber = fondsRepository.findMaxOrderNumber(fonds.getParentId());
        if (orderNumber != null) {
            fonds.setOrderNumber(orderNumber + 1);
        } else {
            fonds.setOrderNumber(1);
        }
        //存储数据
        fonds = fondsRepository.save(fonds);

        //关联机构
        for (int orgId : associationList) {
            Organization organization = organizationRepository.findById(orgId);
            if (null != organization.getFondsId()) {
                throw new InvalidArgumentException("机构已被其它全宗关联");
            }
            organizationRepository.updatefondsIdById(orgId, fonds.getId());
        }
    }

    /**
     * 删除全宗
     */
    @Transactional
    public void delete(int id) {
        if (fondsRepository.existsById(id)) {
            //是否存在子全宗
            if (fondsRepository.existsByParentId(id)) {
                throw new InvalidArgumentException("该全宗下存在子全宗");
            }
            //取消机构关联
            organizationRepository.updatefondsIdByfondsId(id);
            //删除本全宗
            fondsRepository.deleteById(id);
        }
    }

    /**
     * 修改全宗
     */
    @Transactional
    public void update(Fonds fonds, ArrayList<Integer> associationList) {
        if (!fondsRepository.existsByCodeAndId(fonds.getCode(), fonds.getId())) {
            if (fondsRepository.existsByCode(fonds.getCode())) {
                throw new InvalidArgumentException("全宗号已存在");
            }
        }
        //修改数据
        fondsRepository.updateById(fonds);
        //取消机构关联
        organizationRepository.updatefondsIdByfondsId(fonds.getId());
        //关联新机构
        for (int orgId : associationList) {
            Organization organization = organizationRepository.findById(orgId);
            if (null != organization.getFondsId()) {
                throw new InvalidArgumentException("机构已被其它全宗关联");
            }
            organizationRepository.updatefondsIdById(orgId, fonds.getId());
        }
    }

    /**
     * 修改全宗排序优先级
     */
    @Transactional
    public void priority(int upId, int downId) {

        Fonds up = fondsRepository.findById(upId);
        Fonds down = fondsRepository.findById(downId);
        if (up == null || down == null) {
            throw new InvalidArgumentException("全宗不存在或已被删除");
        }
        if (up.getParentId() != down.getParentId()) {
            throw new InvalidArgumentException("上级全宗不一致");
        }
        fondsRepository.updateOrderNumberById(upId, down.getOrderNumber());
        fondsRepository.updateOrderNumberById(downId, up.getOrderNumber());
    }

    public Fonds get(Integer fondId) {
        return fondsRepository.getOne(fondId);
    }

    public List<Fonds> findAllById(Set<Integer> fondsIds) {
        return fondsRepository.findAllById(fondsIds);
    }
}
