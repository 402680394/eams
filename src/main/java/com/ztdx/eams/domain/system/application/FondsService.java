package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.application.event.ArchivesGroupDeleteEvent;
import com.ztdx.eams.domain.system.model.Fonds;
import com.ztdx.eams.domain.system.model.Organization;
import com.ztdx.eams.domain.system.repository.FondsRepository;
import com.ztdx.eams.domain.system.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by li on 2018/4/15.
 */
@Service
public class FondsService {

    private final FondsRepository fondsRepository;

    private final OrganizationRepository organizationRepository;

    private final ApplicationContext applicationContext;

    @Autowired
    public FondsService(FondsRepository fondsRepository, OrganizationRepository organizationRepository, ApplicationContext applicationContext) {
        this.fondsRepository = fondsRepository;
        this.organizationRepository = organizationRepository;
        this.applicationContext = applicationContext;
    }

    /**
     * 新增全宗
     */
    @Transactional
    public void save(Fonds fonds, ArrayList<Integer> associationList) {

        if (fondsRepository.existsByCodeAndGmtDeleted(fonds.getCode(), 0)) {
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
            Organization organization = organizationRepository.findById(orgId).orElse(null);
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
        Optional<Fonds> optional = fondsRepository.findById(id);
        if (!optional.isPresent() || optional.get().getGmtDeleted() == 1) {
            throw new BusinessException("该全宗不存在或已被删除");
        }
        //是否存在子全宗
        if (fondsRepository.existsByParentId(id)) {
            throw new BusinessException("该全宗下存在子全宗");
        }
        //取消机构关联
        organizationRepository.updatefondsIdByfondsId(id);
        //删除本全宗
        fondsRepository.updateGmtDeletedById(id, 1);

        applicationContext.publishEvent(new ArchivesGroupDeleteEvent(this, id));
    }

    /**
     * 修改全宗
     */
    @Transactional
    public void update(Fonds fonds, ArrayList<Integer> associationList) {
        Optional<Fonds> optional = fondsRepository.findById(fonds.getId());
        if (!optional.isPresent() || optional.get().getGmtDeleted() == 1) {
            throw new BusinessException("该全宗不存在或已被删除");
        }
        if (!fondsRepository.existsByCodeAndId(fonds.getCode(), fonds.getId())) {
            if (fondsRepository.existsByCodeAndGmtDeleted(fonds.getCode(), 0)) {
                throw new InvalidArgumentException("全宗号已存在");
            }
        }
        //修改数据
        fondsRepository.updateById(fonds);
        //取消机构关联
        organizationRepository.updatefondsIdByfondsId(fonds.getId());
        //关联新机构
        for (int orgId : associationList) {
            Organization organization = organizationRepository.findById(orgId).orElse(null);
            if (null != organization.getFondsId()) {
                throw new InvalidArgumentException("机构已被其它全宗关联");
            }
            organizationRepository.updatefondsIdById(orgId, fonds.getId());
        }
    }

    /**
     * 排序
     */
    @Transactional
    public void priority(int upId, int downId) {

        Optional<Fonds> up = fondsRepository.findById(upId);
        Optional<Fonds> down = fondsRepository.findById(downId);
        if (!up.isPresent() || !down.isPresent()) {
            throw new InvalidArgumentException("全宗不存在或已被删除");
        }
        if (up.get().getParentId() != down.get().getParentId()) {
            throw new InvalidArgumentException("上级全宗不一致");
        }
        fondsRepository.updateOrderNumberById(upId, down.get().getOrderNumber());
        fondsRepository.updateOrderNumberById(downId, up.get().getOrderNumber());
    }

    public Fonds get(Integer fondId) {
        return fondsRepository.getOne(fondId);
    }

    public List<Fonds> findAllById(Collection<Integer> fondsIds) {
        return fondsRepository.findAllById(fondsIds);
    }

    public List<Fonds> findAllByIdInAndGmtDeleted(Collection<Integer> fondsIds) {
        return fondsRepository.findAllByIdInAndGmtDeleted(fondsIds, 0);
    }

    public List<Fonds> findAll() {
        return fondsRepository.findByGmtDeletedAndParentIdNotNull(0);
    }
}
