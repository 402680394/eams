package com.ztdx.eams.domain.archives.application;

import com.ztdx.eams.basic.exception.BusinessException;
import com.ztdx.eams.domain.archives.model.condition.EntryCondition;
import com.ztdx.eams.domain.archives.model.condition.EntryConditionType;
import com.ztdx.eams.domain.archives.model.DescriptionItem;
import com.ztdx.eams.domain.archives.model.DescriptionItemDataType;
import com.ztdx.eams.domain.archives.model.condition.*;
import com.ztdx.eams.domain.archives.model.entryItem.*;
import com.ztdx.eams.domain.archives.repository.DescriptionItemRepository;
import com.ztdx.eams.domain.archives.repository.mongo.ConditionMongoRepository;
import com.ztdx.eams.domain.archives.repository.mongo.GroupMongoRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConditionService {

    private final ConditionMongoRepository conditionMongoRepository;

    private DescriptionItemRepository descriptionItemRepository;

    private GroupMongoRepository groupMongoRepository;

    /**
     * 构造函数
     */
    @Autowired
    public ConditionService(ConditionMongoRepository conditionMongoRepository, DescriptionItemRepository descriptionItemRepository,GroupMongoRepository groupMongoRepository) {
        this.conditionMongoRepository = conditionMongoRepository;
        this.descriptionItemRepository = descriptionItemRepository;
        this.groupMongoRepository = groupMongoRepository;
    }

    /**
     * 增加档案库查询条件
     */
    public void save(EntryCondition condition) {

        EntryConditionType entryConditionType = condition.getEntryConditionType();
        switch (entryConditionType){
            case system:
                if (conditionMongoRepository.existsByName(condition.getName())){
                    throw new BusinessException("名称已存在");
                }
                break;
            case custom:
                if (conditionMongoRepository.existsByNameAndOwner(condition.getName(),condition.getOwner())){
                    throw new BusinessException("名称已存在");
                }
        }
        conditionMongoRepository.save(condition);

    }

    /**
     * 修改档案库查询条件
     */
    public void update(String id,EntryCondition condition) {

        EntryCondition entryCondition = conditionMongoRepository.findById(id,"archive_entry_condition").orElse(null);
        if (entryCondition.getName().equals(condition.getName()) || !conditionMongoRepository.existsByName(condition.getName()) || !conditionMongoRepository.existsByNameAndOwner(condition.getName(),condition.getOwner())){

            Optional<EntryCondition> find = conditionMongoRepository.findById(id);
            if (!find.isPresent()) {
                save(condition);
            }

            EntryCondition update = find.get();
            update.setId(id);
            update.setName(condition.getName());
            update.setConditions(condition.getConditions());
            conditionMongoRepository.save(update);

        }else {
           throw new BusinessException("名称已存在");
       }

    }

    /**
     * 查询档案库的查询条件
     */
    public Map<String,Object> listEntryCondition(Integer cid,int owner){

        Map<String,Object> resultMap = new HashMap<>();

        List<Map<String,Object>> systemList = new ArrayList<>();
        List<Map<String,Object>> customList = new ArrayList<>();

        //通过档案目录id获取所有的查询条件
        List<EntryCondition> entryConditionList = conditionMongoRepository.findAllByCatalogueId(cid);
        for (EntryCondition entryCondition : entryConditionList) {

            Map<String,Object> map = new HashMap<>();
            map.put("id",entryCondition.getId());
            map.put("name",entryCondition.getName());

            if (entryCondition.getEntryConditionType().equals(EntryConditionType.custom) && entryCondition.getOwner()==owner){
                customList.add(map);
            }else if (entryCondition.getEntryConditionType().equals(EntryConditionType.system)){
                systemList.add(map);
            }
        }

        resultMap.put("system",systemList);
        resultMap.put("custom",customList);

        return resultMap;
    }


    /**
     * 获取档案库的查询条件
     */
    public EntryCondition getEntryCondition(String conditionId){
        return conditionMongoRepository.findById(conditionId,"archive_entry_condition").orElse(null);
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
                conditionEsQueryBuilder = new StringConditionQueryBuilder(key, value);
                break;
            case Array:
                conditionEsQueryBuilder = new ArrayConditionQueryBuilder(key, value);
                break;
            case Text:
                conditionEsQueryBuilder = new TextConditionQueryBuilder(key, value);
                break;
            case Integer:
                conditionEsQueryBuilder = new IntegerConditionQueryBuilder(key, value);
                break;
            case Double:
                conditionEsQueryBuilder = new DoubleConditionQueryBuilder(key, value);
                break;
            case Date:
                conditionEsQueryBuilder = new DateConditionQueryBuilder(key, value);
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

    /**
     * 设置分组查询条件
     */
    public void setEntrySearchGroup(Integer cid,EntrySearchGroup group){
        //通过cid获取分组条件
        Optional<EntrySearchGroup> entrySearchGroup = groupMongoRepository.findById(cid.toString(),"archive_entry_search_group");
        if (!entrySearchGroup.isPresent()){
            group.setId(cid.toString());
            groupMongoRepository.save(group);
        }else{
            EntrySearchGroup searchGroup = entrySearchGroup.get();
            searchGroup.setEntrySearchGroupItem(group.getEntrySearchGroupItem());
            groupMongoRepository.save(searchGroup);
        }

    }

    /**
     * 获取分组查询条件
     */
    public EntrySearchGroup getListEntrySearchGroup(Integer cid){
        return groupMongoRepository.findById(cid.toString(),"archive_entry_search_group").orElse(null);
    }

}
