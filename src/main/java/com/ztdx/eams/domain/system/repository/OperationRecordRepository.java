package com.ztdx.eams.domain.system.repository;

import com.ztdx.eams.basic.repository.CustomMongoRepository;
import com.ztdx.eams.domain.system.model.OperationRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("OperationLogRepository")
public interface OperationRecordRepository extends CustomMongoRepository<OperationRecord, String> {
}
