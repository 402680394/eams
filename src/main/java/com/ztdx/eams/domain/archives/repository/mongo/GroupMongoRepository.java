package com.ztdx.eams.domain.archives.repository.mongo;

import com.ztdx.eams.basic.repository.CustomMongoRepository;
import com.ztdx.eams.domain.archives.model.condition.EntryCondition;
import com.ztdx.eams.domain.archives.model.condition.EntrySearchGroup;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMongoRepository extends CustomMongoRepository<EntrySearchGroup, String> {

}
