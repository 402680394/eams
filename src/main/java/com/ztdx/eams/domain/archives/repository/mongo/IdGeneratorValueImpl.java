package com.ztdx.eams.domain.archives.repository.mongo;

import com.ztdx.eams.domain.archives.model.IdGenerator;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class IdGeneratorValueImpl implements IdGeneratorValue {
    private MongoOperations mongoOperations;

    public IdGeneratorValueImpl(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
        if (!mongoOperations.collectionExists(IdGenerator.class)){
            mongoOperations.createCollection(IdGenerator.class);
        }
    }

    @Override
    public Long get(String id) {
        return get(id, 1);
    }

    @Override
    public Long get(String id, int inc) {
        Update update = new Update().inc("value", inc);
        IdGenerator idGenerator = mongoOperations.findAndModify(
                query(where("_id").is(id))
                , update
                , new FindAndModifyOptions().returnNew(true).upsert(true)
                , IdGenerator.class);
        return idGenerator.getValue();
    }
}
