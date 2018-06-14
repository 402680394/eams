package com.ztdx.eams.basic.repository;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.DefaultResultMapper;
import org.springframework.data.elasticsearch.core.EntityMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.mapping.context.MappingContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomElasticsearchResultMapper extends DefaultResultMapper {
    public CustomElasticsearchResultMapper() {
        super();
    }

    public CustomElasticsearchResultMapper(MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {
        super(mappingContext);
    }

    public CustomElasticsearchResultMapper(EntityMapper entityMapper) {
        super(entityMapper);
    }

    public CustomElasticsearchResultMapper(MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext, EntityMapper entityMapper) {
        super(mappingContext, entityMapper);
    }

    @Override
    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
        AggregatedPage<T> page = super.mapResults(response, clazz, pageable);

        Map<String, Map<String, List<String>>> highlightMap = new HashMap<>();
        response.getHits().forEach(a -> {
            highlightMap.put(a.getId(), new HashMap<>());
            a.getHighlightFields().forEach( (b, c) -> {
                highlightMap.get(a.getId()).put(b, Arrays.stream(c.fragments()).map(Text::string).collect(Collectors.toList()));
            });
        });

        page.forEach(a -> {
            Object id =getId(a);
            Map<String, List<String>> map = highlightMap.get(id);

            map.forEach((b, c) -> {
                setHighlightField(a, b, c);
            });
        });

        return page;
    }

    private <T> Object getId(T t){
        for (Field field : t.getClass().getDeclaredFields()) {
            Annotation annotation = field.getAnnotation(Id.class);
            if (annotation != null){
                field.setAccessible(true);
                try {
                    return field.get(t);
                }catch(Exception ignored){

                }
            }
        }
        return null;
    }

    private <T> void setHighlightField(T t, String fieldName, List<String> values){
        try {
            Field field = t.getClass().getDeclaredField(fieldName);
            if (field != null && field.getType() == String.class){
                field.setAccessible(true);
                StringBuilder sb = values.stream().collect(StringBuilder::new, StringBuilder::append,
                        StringBuilder::append);
                if (sb.length() > 0) {
                    field.set(t, sb.toString());
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {

        }
    }
}
