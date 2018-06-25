package com.ztdx.eams.domain.archives.repository.mongo;

import com.ztdx.eams.basic.repository.CustomMongoRepository;
import com.ztdx.eams.domain.archives.model.IdGenerator;
import org.springframework.stereotype.Repository;

@Repository
public interface IdGeneratorRepository extends CustomMongoRepository<IdGenerator, String>, IdGeneratorValue {
}
