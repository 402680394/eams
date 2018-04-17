package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.system.model.Oganization;
import com.ztdx.eams.domain.system.model.User;
import com.ztdx.eams.domain.system.repository.OganizationRepository;
import com.ztdx.eams.domain.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

/**
 * Created by li on 2018/4/11.
 */
@Service
public class OganizationService {
    private final OganizationRepository oganizationRepository;

    private final UserRepository userRepository;

    @Autowired
    public OganizationService(OganizationRepository oganizationRepository, UserRepository userRepository) {
        this.oganizationRepository = oganizationRepository;
        this.userRepository = userRepository;
    }

    /**
     * 新增机构
     */
    @Transactional
    public void save(Oganization oganization) {
        int i = oganizationRepository.countByCode(oganization.getCode());
        if (i != 0) {
            throw new InvalidArgumentException("机构编码已存在");
        }
        //机构结构验证
        validate(oganization);
        //设置同级机构优先级
        Integer orderNumber = oganizationRepository.findMaxOrderNumber(oganization.getParentId(), oganization.getType());
        if (orderNumber!=null){
            oganization.setOrderNumber(orderNumber + 1);
        }
        oganization.setOrderNumber(1);
        //设置创建时间
        oganization.setGmtCreate(Calendar.getInstance().getTime());
        //存储数据
        oganizationRepository.save(oganization);
    }

    /**
     * 删除机构
     */
    @Transactional
    public void delete(int id) {
        //机构下是否存在用户
        User user = userRepository.findFirstByOrganizationId(id);
        if (null != user) {
            throw new InvalidArgumentException("该机构或子机构下存在用户");
        }
        //删除子机构
        List<Oganization> list = oganizationRepository.findAllByParentId(id);
        if (!list.isEmpty()) {
            for (Oganization o : list) {
                delete(o.getId());
            }
        }
        //删除本机构
        oganizationRepository.deleteById(id);
    }

    /**
     * 修改机构
     */
    @Transactional
    public void update(Oganization oganization) {
        Oganization o = oganizationRepository.findByCodeAndId(oganization.getCode(), oganization.getId());
        if (o == null) {
            int i = oganizationRepository.countByCode(oganization.getCode());
            if (i != 0) {
                throw new InvalidArgumentException("机构编码已存在");
            }
        }
        //机构结构验证
        validate(oganization);
        //修改数据
        oganizationRepository.updateById(oganization);
    }

    /**
     * 修改机构排序优先级
     */
    @Transactional
    public void priority(int upId, int downId) {
        Oganization up=oganizationRepository.findById(upId);
        Oganization down=oganizationRepository.findById(downId);

        if(up.getParentId()!=down.getParentId()&&up.getType()!=down.getType()){
            throw new InvalidArgumentException("机构类型或上级机构不一致");
        }
        oganizationRepository.updateOrderNumberById(upId,down.getOrderNumber());
        oganizationRepository.updateOrderNumberById(downId,up.getOrderNumber());
    }

    /**
     * 新增/修改机构结构验证
     */
    public void validate(Oganization oganization) {
        //确认上下级结构
        if (oganization.getParentId() == 0) {
            if (oganization.getType() != 1) {
                throw new InvalidArgumentException("根节点无法创建部门与科室");
            }
        } else {
            Oganization parent = oganizationRepository.findById(oganization.getParentId());
            if (oganization.getType() == 1 && parent.getType() == 2) {
                throw new InvalidArgumentException("部门下无法创建公司");
            }
            if (parent.getType() == 3) {
                throw new InvalidArgumentException("科室下无法创建机构");
            }
        }
    }
}
