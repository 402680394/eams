package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import com.ztdx.eams.domain.archives.model.condition.EntryCondition;
import com.ztdx.eams.domain.archives.repository.mongo.ConditionMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.archives.model.DescriptionItemDataType;
import com.ztdx.eams.domain.archives.model.condition.*;
import com.ztdx.eams.domain.archives.model.entryItem.*;
import com.ztdx.eams.domain.archives.repository.DescriptionItemRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConditionService {

    private final ConditionMongoRepository conditionMongoRepository;

    private DescriptionItemRepository descriptionItemRepository;

    /**
     * 构造函数
     */
    @Autowired
    public ConditionService(ConditionMongoRepository conditionMongoRepository, DescriptionItemRepository descriptionItemRepository) {
        this.conditionMongoRepository = conditionMongoRepository;
        this.descriptionItemRepository = descriptionItemRepository;
    }

    /**
     * 增加档案库查询条件
     */
    public Object save(EntryCondition condition) {
        if (conditionMongoRepository.existsByName(condition.getName())){
            throw new BusinessException("名称已存在");
        }
        return conditionMongoRepository.save(condition);
    }

    /**
     * 修改档案库查询条件
     */
    public void update(EntryCondition condition) {

        EntryCondition entryCondition = conditionMongoRepository.findById(condition.getId()).orElse(null);
        if (entryCondition.getName().equals(entryCondition.getName()) || !conditionMongoRepository.existsByName(condition.getName())){

            Optional<EntryCondition> find = conditionMongoRepository.findById(condition.getId());
            if (!find.isPresent()) {
                save(condition);
            }

            EntryCondition update = find.get();
            update.setId(condition.getId());
            update.setName(condition.getName());
            update.setConditions(condition.getConditions());
            conditionMongoRepository.save(update);

        }else {
           throw new BusinessException("名称已存在");
       }

    }

    /**
     * 获取档案库的查询条件
     */
    public EntryCondition getEntryCondition(Integer conditionId){
        return conditionMongoRepository.findById(conditionId.toString()).orElse(null);
    }

    public QueryBuilder convert2ElasticsearchQuery(int catalogueId, List<Condition> conditions){
        Map<String, DescriptionItem> map = descriptionItemRepository.findByCatalogueId(catalogueId).stream()
                .collect(
                        Collectors.toMap(
                                DescriptionItem::getMetadataName
                                , a -> a
                                , (d1, d2) -> d2));
        format(conditions);
        return genQuery(conditions, map);
    }

    @SuppressWarnings("unchecked")
    private QueryBuilder genQuery(List<Condition> conditions, Map<String, DescriptionItem> descriptionItemMap){
        if (conditions == null || conditions.size() == 0){
            return null;
        }

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        List<QueryBuilder> queryBuilders = new ArrayList<>();
        conditions.forEach(a -> {
            QueryBuilder subQuery;
            if (a.getNested()){
                List<Condition> nestedConditions = (List<Condition>)a.getValue();
                subQuery = genQuery(nestedConditions, descriptionItemMap);
            }else {
                subQuery = genSimpleQuery(a, descriptionItemMap);
            }

            if (subQuery != null) {
                queryBuilders.add(subQuery);
            }
        });

        Operator.logical logical = getOperator(conditions);
        if (logical == null || logical == Operator.logical.and){
            boolQueryBuilder.must().addAll(queryBuilders);
        }else{
            boolQueryBuilder.should().addAll(queryBuilders);
        }

        return boolQueryBuilder;
    }

    private QueryBuilder genSimpleQuery(Condition condition, Map<String, DescriptionItem> descriptionItemMap){
        DescriptionItem item = descriptionItemMap.get(condition.getColumn());

        DescriptionItemDataType dataType = item.getDataType();
        Object value = condition.getValue();
        String key = "items." + item.getMetadataName();

        AbstractConditionEsQueryBuilder conditionEsQueryBuilder;
        switch (dataType){
            case String:
                String stringValue = new StringEntryItemValue(item, value).get();
                conditionEsQueryBuilder = new StringConditionQueryBuilder(key, stringValue);
                break;
            case Array:
                ArrayList arrayValue = new ArrayEntryItemValue(item, value).get();
                conditionEsQueryBuilder = new ArrayConditionQueryBuilder(key, arrayValue);
                break;
            case Text:
                String textValue = new StringEntryItemValue(item, value).get();
                conditionEsQueryBuilder = new TextConditionQueryBuilder(key, textValue);
                break;
            case Integer:
                Integer integerValue = new IntegerEntryItemValue(item, value).get();
                conditionEsQueryBuilder = new IntegerConditionQueryBuilder(key, integerValue);
                break;
            case Double:
                Double doubleValue = new DoubleEntryItemValue(item, value).get();
                conditionEsQueryBuilder = new DoubleConditionQueryBuilder(key, doubleValue);
                break;
            case Date:
                Date dateValue = new DateEntryItemValue(item, value).get();
                conditionEsQueryBuilder = new DateConditionQueryBuilder(key, dateValue);
                break;
            default:
                throw new BusinessException("解析查询条件时，不支持的类型");
        }

        switch (condition.getOperator()) {
            case equal:
                return conditionEsQueryBuilder.equal();
            case notEqual:
                return conditionEsQueryBuilder.notEqual();
            case contain:
                return conditionEsQueryBuilder.contain();
            case lessThan:
                return conditionEsQueryBuilder.lessThan();
            case notContain:
                return conditionEsQueryBuilder.notContain();
            case greaterThan:
                return conditionEsQueryBuilder.greaterThan();
            case lessThanOrEqual:
                return conditionEsQueryBuilder.lessThanOrEqual();
            case greaterThanOrEqual:
                return conditionEsQueryBuilder.greaterThanOrEqual();
            default:
                return null;
        }
    }

    private Operator.logical getOperator(List<Condition> conditions){
        if (conditions.size() <= 1){
            return null;
        }else {
            return conditions.get(conditions.size() - 1).getLogical();
        }
    }

    @SuppressWarnings("unchecked")
    private void format(List<Condition> conditions){
        for (int i = 0; i < conditions.size(); i++){
            Condition condition = conditions.get(i);
            if (condition.getNested() && condition.getValue() instanceof List){
                List<Condition> nestedConditions = (List<Condition>) condition.getValue();
                format(nestedConditions);
            }

            if (condition.getLogical() == Operator.logical.and && i - 1 >= 0){
                Condition previous = conditions.get(i - 1);
                merge(previous, condition);
                conditions.remove(i);
                i --;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void merge(Condition s, Condition t){
        if (s.getNested() && s.getValue() instanceof List){
            ((List<Condition>) s.getValue()).add(t);
        }else{
            List<Condition> list = new ArrayList<>();
            Condition copy = new Condition();
            copy.setNested(s.getNested());
            copy.setOperator(s.getOperator());
            copy.setColumn(s.getColumn());
            copy.setLogical(null);
            copy.setValue(s.getValue());

            list.add(copy);
            list.add(t);

            s.setNested(true);
            s.setColumn(null);
            s.setOperator(null);
            s.setValue(list);
        }
    }
}
