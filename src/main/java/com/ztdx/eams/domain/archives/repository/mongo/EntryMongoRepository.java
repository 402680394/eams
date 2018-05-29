package com.ztdx.eams.domain.archives.repository.mongo;

import com.ztdx.eams.basic.repository.CustomMongoRepository;
import com.ztdx.eams.domain.archives.model.Entry;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EntryMongoRepository extends CustomMongoRepository<Entry, String> {
}
