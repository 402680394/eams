package com.ztdx.eams.domain.store.application;

import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.store.model.Box;
import com.ztdx.eams.domain.store.model.BoxCodeRule;
import com.ztdx.eams.domain.store.model.event.ShelfCellDeletedEvent;
import com.ztdx.eams.domain.store.repository.BoxCodeRuleRepository;
import com.ztdx.eams.domain.store.repository.BoxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by li on 2018/7/10.
 */
@Service
public class BoxService {

    private final BoxRepository boxRepository;

    private final BoxCodeRuleRepository boxCodeRuleRepository;

    @Autowired
    public BoxService(BoxRepository boxRepository, BoxCodeRuleRepository boxCodeRuleRepository) {
        this.boxRepository = boxRepository;
        this.boxCodeRuleRepository = boxCodeRuleRepository;
    }

    /*
    *修改盒信息
    */
    @Transactional
    public void update(Box box) {
        Box b = boxRepository.findById(box.getId()).get();
        box.setCode(box.getCodeRule() + box.getFlowNumber());
        if (!b.getCode().equals(box.getCode())) {
            if (boxRepository.existsByCodeAndArchivesId(box.getCode(), box.getArchivesId())) {
                throw new InvalidArgumentException("盒号已存在");
            }
        }
        //修改数据
        boxRepository.updateById(box);
    }

    /*
    *删除盒
    */
    @Transactional
    public void delete(List<Integer> ids) {
        List<Box> list = boxRepository.findAllById(ids);
        boxRepository.deleteAll(list);
    }

    /*
    *上架盒
    */
    @Transactional
    public void onFrame(String cellCode, List<Integer> ids) {
        for (int id : ids) {
            boxRepository.updateOnFrameById(id, true, cellCode);
        }
    }

    /*
    *下架盒
    */
    @Transactional
    public void downFrame(List<Integer> ids) {
        for (int id : ids) {
            boxRepository.updateOnFrameById(id, false, "");
        }
    }

    /*
    *新增盒
    */
    @Transactional
    public void save(Box box, int total) {

        BoxCodeRule boxCodeRule = boxCodeRuleRepository.findByArchivesIdAndType(box.getArchivesId(), 4);

        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setMinimumIntegerDigits(boxCodeRule.getFlowNumberLength());
        formatter.setGroupingUsed(false);
        for (int i = 0; i < total; i++) {

            try {
                int flowNumber = formatter.parse(box.getFlowNumber()).intValue();
                flowNumber = flowNumber + i;
                //位数
                int count = 0;
                while (flowNumber > 0) {
                    flowNumber = flowNumber / 10;
                    count++;
                }
                if (count > boxCodeRule.getFlowNumberLength()) {
                    throw new InvalidArgumentException("流水号超出最大限制");
                }
                box.setFlowNumber(formatter.format(flowNumber));
                box.setCode(box.getCodeRule() + box.getFlowNumber());
            } catch (ParseException e) {
                throw new InvalidArgumentException("流水号不是纯数字");
            }

            if (boxRepository.existsByCodeAndArchivesId(box.getCode(), box.getArchivesId())) {
                throw new InvalidArgumentException("盒号已存在");
            }
            boxRepository.save(box);

        }
    }

    public Box getByCode(int archiveId, String code) {
        return boxRepository.findByArchivesIdAndCode(archiveId, code);
    }

    public Box get(int id) {
        return boxRepository.findById(id).orElse(null);
    }

    public void updateTotal(String code, int archivesId, int pages, int files) {
        boxRepository.updateTotal(code, archivesId, pages, files);
    }

    public boolean existsByCodeAndArchivesId(int archiveId, String boxCode) {
        return boxRepository.existsByCodeAndArchivesId(boxCode, archiveId);
    }

    public List<String> getCodeByIds(List<Integer> ids) {
        List<Box> list = boxRepository.findByIdIn(ids);
        return list.stream().map(Box::getCode).collect(Collectors.toList());
    }

    public void checkOnFrameByIds(List<Integer> ids) {
        List<Box> list = boxRepository.findByIdIn(ids);
        for (Box box : list) {
            if (box.isOnFrame()) {
                throw new InvalidArgumentException("需要先下架在架档案盒");
            }
        }
    }

    @Async
    @EventListener
    @Transactional
    public void shelfCellDeleted(ShelfCellDeletedEvent shelfCellDeletedEvent) throws InterruptedException {
        shelfCellDeletedEvent.getShelfCellPointCodes().forEach(point -> {
            boxRepository.updateOnFrameByPoint(false, "", point);
        });

    }
}
