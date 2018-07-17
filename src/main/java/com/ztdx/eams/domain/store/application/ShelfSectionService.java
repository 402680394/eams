package com.ztdx.eams.domain.store.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.exception.NotFoundException;
import com.ztdx.eams.domain.store.model.Shelf;
import com.ztdx.eams.domain.store.model.ShelfCell;
import com.ztdx.eams.domain.store.model.ShelfSection;
import com.ztdx.eams.domain.store.model.event.ShelfCellDeletedEvent;
import com.ztdx.eams.domain.store.repository.ShelfCellRepository;
import com.ztdx.eams.domain.store.repository.ShelfRepository;
import com.ztdx.eams.domain.store.repository.ShelfSectionRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShelfSectionService {

    private ShelfSectionRepository shelfSectionRepository;

    private ShelfCellRepository shelfCellRepository;

    private ShelfRepository shelfRepository;

    public ShelfSectionService(ShelfSectionRepository shelfSectionRepository, ShelfCellRepository shelfCellRepository, ShelfRepository shelfRepository) {
        this.shelfSectionRepository = shelfSectionRepository;
        this.shelfCellRepository = shelfCellRepository;
        this.shelfRepository = shelfRepository;
  }

    @Transactional(rollbackFor = Exception.class)
    public ShelfSection save(ShelfSection shelfSection) {
        if (!shelfRepository.existsById(shelfSection.getShelfId())){
            throw new NotFoundException("密集架不存在");
        }

        if (shelfSectionRepository.existsByShelfIdAndName(shelfSection.getShelfId(), shelfSection.getName())){
            throw new InvalidArgumentException("密集架列名称已存在");
        }

        if (shelfSectionRepository.existsByShelfIdAndCode(shelfSection.getShelfId(), shelfSection.getCode())){
            throw new InvalidArgumentException("密集架列编码已存在");
        }

        Shelf shelf = shelfRepository.findById(shelfSection.getShelfId()).orElse(null);

        assert shelf != null;
        shelfSection.setStorageId(shelf.getStorageId());
        //shelfSection.setFondsId(shelf.getFondsId());
        //shelfSection.setGmtCreate(Date.from(Instant.now()));
        shelfSection.setGmtDeleted(0);

        shelfSection = shelfSectionRepository.save(shelfSection);

        shelf.setSectionNum(shelf.getSectionNum() + 1);
        shelfRepository.save(shelf);

        shelfCellRepository.createCell(shelfSection);

        return shelfSection;
    }

    public void update(ShelfSection section) {
        ShelfSection old = shelfSectionRepository.findById(section.getId()).orElse(null);
        if (old == null || old.getGmtDeleted() == 1){
            throw new NotFoundException("密集架不存在");
        }

        old.setName(section.getName());
        old.setCode(section.getCode());
        old.setRemark(section.getRemark());
        old.setLeftTag(section.getLeftTag());
        old.setRightTag(section.getRightTag());
        old.setShelfSectionType(section.getShelfSectionType());

        shelfSectionRepository.save(old);
    }

    public ShelfSection get(int id) {
        return shelfSectionRepository.findById(id).orElse(null);
    }

    public void delete(List<Integer> ids) {
        //TODO @lijie 删除后需要下架盒
        List<ShelfSection> list = shelfSectionRepository.findAllById(ids);
        list.forEach(a -> a.setGmtDeleted(1));
        shelfSectionRepository.saveAll(list);
    }
}
