package com.ztdx.eams.domain.system.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.system.model.Organization;
import com.ztdx.eams.domain.system.repository.OrganizationRepository;
import com.ztdx.eams.domain.system.repository.UserRepository;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by li on 2018/4/11.
 */
@Service
public class OrganizationService {
    private final OrganizationRepository organizationRepository;

    private final UserRepository userRepository;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository, UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    /**
     * 新增机构
     */
    @Transactional
    public void save(Organization organization) {
        if (organizationRepository.existsByCode(organization.getCode())) {
            throw new InvalidArgumentException("机构编码已存在");
        }
        //机构结构验证
        validate(organization);
        //设置同级机构优先级
        Integer orderNumber = organizationRepository.findMaxOrderNumber(organization.getParentId(), organization.getType());
        if (orderNumber != null) {
            organization.setOrderNumber(orderNumber + 1);
        } else {
            organization.setOrderNumber(1);
        }
        //存储数据
        organizationRepository.save(organization);
    }

    /**
     * 删除机构
     */
    @Transactional
    public void delete(int id) {
        if (organizationRepository.existsById(id)) {
            //机构下是否存在用户
            if (userRepository.existsByOrganizationId(id)) {
                throw new InvalidArgumentException("该机构或子机构下存在用户");
            }
            //机构下是否存在子机构
            if (organizationRepository.existsByParentId(id)) {
                throw new InvalidArgumentException("该机构下存在子机构");
            }
            //删除本机构
            organizationRepository.deleteById(id);
        }
    }

    /**
     * 修改机构
     */
    @Transactional
    public void update(Organization organization) {
        if (!organizationRepository.existsByCodeAndId(organization.getCode(), organization.getId())) {
            if (organizationRepository.existsByCode(organization.getCode())) {
                throw new InvalidArgumentException("机构编码已存在");
            }
        }
        //机构结构验证
        validate(organization);
        //修改数据
        organizationRepository.updateById(organization);
    }

    /**
     * 修改机构排序优先级
     */
    @Transactional
    public void priority(int upId, int downId) {
        Organization up = organizationRepository.findById(upId);
        Organization down = organizationRepository.findById(downId);
        if (up == null || down == null) {
            throw new InvalidArgumentException("机构不存在或已被删除");
        }

        if (up.getParentId() != down.getParentId() && up.getType() != down.getType()) {
            throw new InvalidArgumentException("机构类型或上级机构不一致");
        }
        organizationRepository.updateOrderNumberById(upId, down.getOrderNumber());
        organizationRepository.updateOrderNumberById(downId, up.getOrderNumber());
    }

    /**
     * 新增/修改机构结构验证
     */
    public void validate(Organization organization) {
        //确认上下级结构
        if (organization.getParentId() == 1) {
            if (organization.getType() != 1) {
                throw new InvalidArgumentException("根节点无法创建部门与科室");
            }
        } else {
            Organization parent = organizationRepository.findById((int) organization.getParentId());
            if (organization.getType() == 1 && parent.getType() == 2) {
                throw new InvalidArgumentException("部门下无法创建公司");
            }
            if (parent.getType() == 3) {
                throw new InvalidArgumentException("科室下无法创建机构");
            }
        }
    }

    public Organization get(int id) {
        return organizationRepository.findById(id);
    }

    public Map<Integer, Pair<String, String>> listDepartmentAndCompany(Collection<Integer> ids){
        List<Organization> organizations = organizationRepository.findAll();
        Map<Integer, Organization> map = organizations.stream().collect(Collectors.toMap(Organization::getId, a->a));
        Map<Integer, Pair<String, String>> result = new HashMap<>();
        ids.forEach(a -> {
            Pair<String, String> find = new Pair<>("", "");
            find = findCompany(map, a, find);
            result.put(a, find);
        });
        return result;
    }

    private Pair<String, String> findCompany(Map<Integer, Organization> map, Integer id, Pair<String, String> result){
        Organization organization = map.get(id);
        if (organization == null){
            return result;
        }

        Pair<String, String > find;

        if (organization.getType() == 2){
            find = new Pair<>(organization.getName(), "");
        }else if (organization.getType() == 1){
            find = new Pair<>(result.getKey(), organization.getName());
            return find;
        }else{
            find = result;
        }

        if (organization.getParentId() != null){
            return findCompany(map, organization.getParentId(), find);
        }

        return find;
    }
}
