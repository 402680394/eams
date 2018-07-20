package com.ztdx.eams.domain.store.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.exception.NotFoundException;
import com.ztdx.eams.domain.store.model.Shelf;
import com.ztdx.eams.domain.store.model.ShelfSection;
import com.ztdx.eams.domain.store.repository.ShelfCellRepository;
import com.ztdx.eams.domain.store.repository.ShelfRepository;
import com.ztdx.eams.domain.store.repository.ShelfSectionRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShelfService {

    private ShelfRepository shelfRepository;

    private ShelfSectionRepository shelfSectionRepository;

    private ShelfCellRepository shelfCellRepository;

    public ShelfService(ShelfRepository shelfRepository, ShelfSectionRepository shelfSectionRepository, ShelfCellRepository shelfCellRepository) {
        this.shelfRepository = shelfRepository;
        this.shelfSectionRepository = shelfSectionRepository;
        this.shelfCellRepository = shelfCellRepository;
    }

    @Transactional
    public Shelf save(Shelf shelf){

        if (existsByName(shelf.getStorageId(), shelf.getName())){
            throw new InvalidArgumentException("密集架名称已存在");
        }

        if (existsByCode(shelf.getStorageId(), shelf.getCode())){
            throw new InvalidArgumentException("密集架编码已存在");
        }

        shelf.setGmtCreate(Date.from(Instant.now()));
        shelf.setGmtDeleted(0);

        shelf = shelfRepository.save(shelf);

        createSection(shelf);

        return shelf;
    }

    @Transactional
    public void createSection(Shelf shelf){

        List<ShelfSection> sectionList = shelfSectionRepository.createSection(shelf);

        sectionList.forEach(section -> shelfCellRepository.createCell(section));

        int sectionCount = shelfSectionRepository.countByShelfIdAndGmtDeleted(shelf.getId(), 0);

        if(sectionCount != shelf.getSectionNum()){
            shelf.setSectionNum(sectionCount);
            shelfRepository.save(shelf);
        }
    }

    public boolean existsByName(int storageId, String name){
        return shelfRepository.existsByStorageIdAndNameAndGmtDeleted(storageId, name, 0);
    }

    public boolean existsByCode(int storageId, String code){
        return shelfRepository.existsByStorageIdAndCodeAndGmtDeleted(storageId, code, 0);
    }

    public boolean existsByName(int storageId, int shelfId, String name){
        return shelfRepository.existsByStorageIdAndIdNotAndNameAndGmtDeleted(storageId, shelfId, name, 0);
    }

    public boolean existsByCode(int storageId, int shelfId, String code){
        return shelfRepository.existsByStorageIdAndIdNotAndCodeAndGmtDeleted(storageId, shelfId, code, 0);
    }

    public void update(Shelf shelf){
        Shelf old = shelfRepository.findById(shelf.getId()).orElse(null);
        if (old == null){
            throw new NotFoundException("密集架不存在");
        }

        if (existsByName(old.getStorageId(), shelf.getId(), shelf.getName())){
            throw new InvalidArgumentException("密集架名称已存在");
        }

        if (existsByCode(old.getStorageId(), shelf.getId(), shelf.getCode())){
            throw new InvalidArgumentException("密集架编码已存在");
        }

        old.setCode(shelf.getCode());
        old.setName(shelf.getName());
        old.setRemark(shelf.getRemark());

        shelfRepository.save(old);
    }

    public Shelf get(int id) {
        return shelfRepository.findById(id).orElse(null);
    }

    public void delete(List<Integer> ids) {
        //TODO @lijie 删除后需要下架盒
        List<Shelf> list = shelfRepository.findAllById(ids);
        list.forEach(a -> a.setGmtDeleted(1));
        shelfRepository.saveAll(list);
    }


    public List<Map<String, Object>> list(int storageId) {
        List<Shelf> list = shelfRepository.findByStorageIdAndGmtDeleted(storageId, 0);
        List<Integer> ids = list.stream().map(Shelf::getId).collect(Collectors.toList());
        List<ShelfSection> sections = shelfSectionRepository.findByShelfIdInAndGmtDeleted(ids, 0);
        Map<Integer, List<ShelfSection>> sectionGroups = sections.stream().collect(Collectors.groupingBy(ShelfSection::getShelfId));
        return list.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("name", a.getName());
            map.put("code", a.getCode());
            map.put("storageId", a.getStorageId());
            map.put("remark", a.getRemark());
            map.put("shelfType", a.getShelfType());
            map.put("sectionNum", a.getSectionNum());

            List<ShelfSection> sectionList = sectionGroups.getOrDefault(a.getId(), null);
            if (sectionList != null){
                map.put("children", sectionList);
            }

            return map;
        }).collect(Collectors.toList());
    }
}
