package com.ztdx.eams.domain.system.repository;

import com.ztdx.eams.basic.repository.CustomMongoRepository;
import com.ztdx.eams.domain.system.model.OperationLog;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("OperationLogRepository")
public interface  OperationLogRepository extends CustomMongoRepository<OperationLog, String> {
}
