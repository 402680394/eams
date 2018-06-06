package com.ztdx.eams.domain.archives.repository.elasticsearch;

import com.ztdx.eams.basic.repository.CustomElasticsearchRepository;
import com.ztdx.eams.domain.archives.model.OriginalText;
import org.springframework.stereotype.Repository;

/**
 * Created by li on 2018/5/30.
 */
@Repository
public interface OriginalTextElasticsearchRepository extends CustomElasticsearchRepository<OriginalText, String> {
}
