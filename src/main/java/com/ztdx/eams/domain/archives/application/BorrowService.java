package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.domain.archives.model.Borrow;
import com.ztdx.eams.domain.archives.repository.BorrowRepository;
import com.ztdx.eams.domain.archives.repository.mongo.IdGeneratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class BorrowService {

    private final BorrowRepository borrowRepository;

    private final IdGeneratorRepository idGeneratorRepository;

    @Autowired
    public BorrowService(BorrowRepository borrowRepository, IdGeneratorRepository idGeneratorRepository) {
        this.borrowRepository = borrowRepository;
        this.idGeneratorRepository = idGeneratorRepository;
    }

    @Transactional
    public void apply(Borrow borrow) {
        //生成借阅单号
        String prefix = "JYSQ";
        prefix = prefix + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        borrow.setCode(prefix + idGeneratorRepository.get(prefix));

        borrowRepository.save(borrow);
    }

    @Transactional
    public void update(Borrow borrow) {
        borrowRepository.save(borrow);
    }

    public Borrow get(Integer orderId) {
        Borrow borrow = borrowRepository.findById(orderId).orElse(null);
        return borrow;
    }
}
