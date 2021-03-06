package com.ztdx.eams.domain.archives.repository.elasticsearch;

import com.ztdx.eams.basic.repository.CustomElasticsearchRepository;
import com.ztdx.eams.domain.archives.model.Entry;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryElasticsearchRepository extends CustomElasticsearchRepository<Entry, String> {

}
