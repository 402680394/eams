package com.ztdx.eams.domain.store.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.basic.exception.NotFoundException;
import com.ztdx.eams.domain.store.model.ShelfCell;
import com.ztdx.eams.domain.store.repository.ShelfCellRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ShelfCellService {

    private ShelfCellRepository shelfCellRepository;

    public ShelfCellService(ShelfCellRepository shelfCellRepository) {
        this.shelfCellRepository = shelfCellRepository;
    }

    public void update(
            int id
            , String barCode
            , String pointCode
    ){
        ShelfCell cell = shelfCellRepository.findById(id).orElse(null);
        if (cell == null){
            throw new NotFoundException("密集架格不存在");
        }

        if (!StringUtils.isEmpty(barCode) && !barCode.equals(cell.getBarCode())){
            if (shelfCellRepository.existsByBarCodeAndIdNotAndGmtDeleted(barCode, id, 0)){
                throw new InvalidArgumentException("条码已存在");
            }
            cell.setBarCode(barCode);
        }

        if (!StringUtils.isEmpty(pointCode) && !pointCode.equals(cell.getPointCode())){
            if (shelfCellRepository.existsByPointCodeAndIdNotAndGmtDeleted(pointCode, id, 0)){
                throw new InvalidArgumentException("库位码已存在");
            }
            cell.setPointCode(pointCode);
        }

        shelfCellRepository.save(cell);
    }

    public Page<ShelfCell> findByShelfSectionId(int shelfSectionId, Pageable pageable){
        return shelfCellRepository.findByShelfSectionIdAndGmtDeleted(shelfSectionId, pageable, 0);
    }
}
