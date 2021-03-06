package com.ztdx.eams.domain.archives.repository.mongo;

import com.ztdx.eams.basic.repository.CustomMongoRepository;
import com.ztdx.eams.domain.archives.model.OriginalText;
import org.springframework.stereotype.Repository;

/**
 * Created by li on 2018/5/23.
 */
@Repository
public interface OriginalTextMongoRepository extends CustomMongoRepository<OriginalText, String> {

}
