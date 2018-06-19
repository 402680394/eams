package com.ztdx.eams.domain.archives.repository.mongo;

import com.ztdx.eams.basic.repository.CustomMongoRepository;
import com.ztdx.eams.domain.archives.model.Entry;
import com.ztdx.eams.domain.archives.model.condition.EntryCondition;
import org.springframework.stereotype.Repository;

@Repository
public interface ConditionMongoRepository extends CustomMongoRepository<EntryCondition, String> {
}
