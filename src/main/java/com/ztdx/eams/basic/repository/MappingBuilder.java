package com.ztdx.eams.basic.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.core.completion.Completion;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.StringUtils;

class MappingBuilder {
    public static final String FIELD_DATA = "fielddata";
    public static final String FIELD_STORE = "store";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_INDEX = "index";
    public static final String FIELD_FORMAT = "format";
    public static final String FIELD_SEARCH_ANALYZER = "search_analyzer";
    public static final String FIELD_INDEX_ANALYZER = "analyzer";
    public static final String FIELD_PROPERTIES = "properties";
    public static final String FIELD_PARENT = "_parent";
    public static final String COMPLETION_PRESERVE_SEPARATORS = "preserve_separators";
    public static final String COMPLETION_PRESERVE_POSITION_INCREMENTS = "preserve_position_increments";
    public static final String COMPLETION_MAX_INPUT_LENGTH = "max_input_length";
    public static final String INDEX_VALUE_NOT_ANALYZED = "not_analyzed";
    public static final String TYPE_VALUE_STRING = "text";
    public static final String TYPE_VALUE_GEO_POINT = "geo_point";
    public static final String TYPE_VALUE_COMPLETION = "completion";
    public static final String TYPE_VALUE_GEO_HASH_PREFIX = "geohash_prefix";
    public static final String TYPE_VALUE_GEO_HASH_PRECISION = "geohash_precision";
    private static SimpleTypeHolder SIMPLE_TYPE_HOLDER;

    MappingBuilder() {
    }

    static XContentBuilder buildMapping(Class clazz, String indexType, String idFieldName, String parentType) throws IOException {
        XContentBuilder mapping = XContentFactory.jsonBuilder().startObject().startObject(indexType);
        if (StringUtils.hasText(parentType)) {
            mapping.startObject("_parent").field("type", parentType).endObject();
        }

        XContentBuilder xContentBuilder = mapping.startObject("properties");
        mapEntity(xContentBuilder, clazz, true, idFieldName, "", false, FieldType.Auto, (Field)null);
        return xContentBuilder.endObject().endObject().endObject();
    }

    private static void mapEntity(XContentBuilder xContentBuilder, Class clazz, boolean isRootObject, String idFieldName, String nestedObjectFieldName, boolean nestedOrObjectField, FieldType fieldType, Field fieldAnnotation) throws IOException {
        java.lang.reflect.Field[] fields = retrieveFields(clazz);
        if (!isRootObject && (isAnyPropertyAnnotatedAsField(fields) || nestedOrObjectField)) {
            String type = FieldType.Object.toString().toLowerCase();
            if (nestedOrObjectField) {
                type = fieldType.toString().toLowerCase();
            }

            XContentBuilder t = xContentBuilder.startObject(nestedObjectFieldName).field("type", type);
            if (nestedOrObjectField && FieldType.Nested == fieldType && fieldAnnotation.includeInParent()) {
                t.field("include_in_parent", fieldAnnotation.includeInParent());
            }

            t.startObject("properties");
        }

        java.lang.reflect.Field[] var18 = fields;
        int var19 = fields.length;

        for(int var11 = 0; var11 < var19; ++var11) {
            java.lang.reflect.Field field = var18[var11];
            if (!field.isAnnotationPresent(Transient.class) && !isInIgnoreFields(field, fieldAnnotation)) {
                if (field.isAnnotationPresent(Mapping.class)) {
                    String mappingPath = ((Mapping)field.getAnnotation(Mapping.class)).mappingPath();
                    if (org.apache.commons.lang.StringUtils.isNotBlank(mappingPath)) {
                        ClassPathResource mappings = new ClassPathResource(mappingPath);
                        if (mappings.exists()) {
                            xContentBuilder.rawField(field.getName(), mappings.getInputStream());
                            continue;
                        }
                    }
                }

                boolean isGeoPointField = isGeoPointField(field);
                boolean isCompletionField = isCompletionField(field);
                Field singleField = (Field)field.getAnnotation(Field.class);
                if (!isGeoPointField && !isCompletionField && isEntity(field) && isAnnotated(field)) {
                    if (singleField == null) {
                        continue;
                    }

                    boolean nestedOrObject = isNestedOrObjectField(field);
                    mapEntity(xContentBuilder, getFieldType(field), false, "", field.getName(), nestedOrObject, singleField.type(), (Field)field.getAnnotation(Field.class));
                    if (nestedOrObject) {
                        continue;
                    }
                }

                MultiField multiField = (MultiField)field.getAnnotation(MultiField.class);
                if (isGeoPointField) {
                    applyGeoPointFieldMapping(xContentBuilder, field);
                }

                if (isCompletionField) {
                    CompletionField completionField = (CompletionField)field.getAnnotation(CompletionField.class);
                    applyCompletionFieldMapping(xContentBuilder, field, completionField);
                }

                if (isRootObject && singleField != null && isIdField(field, idFieldName)) {
                    applyDefaultIdFieldMapping(xContentBuilder, field);
                } else if (multiField != null) {
                    addMultiFieldMapping(xContentBuilder, field, multiField, isNestedOrObjectField(field));
                } else if (singleField != null) {
                    addSingleFieldMapping(xContentBuilder, field, singleField, isNestedOrObjectField(field));
                }
            }
        }

        if (!isRootObject && isAnyPropertyAnnotatedAsField(fields) || nestedOrObjectField) {
            xContentBuilder.endObject().endObject();
        }

    }

    private static java.lang.reflect.Field[] retrieveFields(Class clazz) {
        List<java.lang.reflect.Field> fields = new ArrayList();
        Class targetClass = clazz;

        do {
            fields.addAll(Arrays.asList(targetClass.getDeclaredFields()));
            targetClass = targetClass.getSuperclass();
        } while(targetClass != null && targetClass != Object.class);

        return (java.lang.reflect.Field[])fields.toArray(new java.lang.reflect.Field[fields.size()]);
    }

    private static boolean isAnnotated(java.lang.reflect.Field field) {
        return field.getAnnotation(Field.class) != null || field.getAnnotation(MultiField.class) != null || field.getAnnotation(GeoPointField.class) != null || field.getAnnotation(CompletionField.class) != null;
    }

    private static void applyGeoPointFieldMapping(XContentBuilder xContentBuilder, java.lang.reflect.Field field) throws IOException {
        xContentBuilder.startObject(field.getName());
        xContentBuilder.field("type", "geo_point");
        xContentBuilder.endObject();
    }

    private static void applyCompletionFieldMapping(XContentBuilder xContentBuilder, java.lang.reflect.Field field, CompletionField annotation) throws IOException {
        xContentBuilder.startObject(field.getName());
        xContentBuilder.field("type", "completion");
        if (annotation != null) {
            xContentBuilder.field("max_input_length", annotation.maxInputLength());
            xContentBuilder.field("preserve_position_increments", annotation.preservePositionIncrements());
            xContentBuilder.field("preserve_separators", annotation.preserveSeparators());
            if (org.apache.commons.lang.StringUtils.isNotBlank(annotation.searchAnalyzer())) {
                xContentBuilder.field("search_analyzer", annotation.searchAnalyzer());
            }

            if (org.apache.commons.lang.StringUtils.isNotBlank(annotation.analyzer())) {
                xContentBuilder.field("analyzer", annotation.analyzer());
            }
        }

        xContentBuilder.endObject();
    }

    private static void applyDefaultIdFieldMapping(XContentBuilder xContentBuilder, java.lang.reflect.Field field) throws IOException {
        xContentBuilder.startObject(field.getName()).field("type", "text").field("index", "not_analyzed");
        xContentBuilder.endObject();
    }

    private static void addSingleFieldMapping(XContentBuilder xContentBuilder, java.lang.reflect.Field field, Field fieldAnnotation, boolean nestedOrObjectField) throws IOException {
        xContentBuilder.startObject(field.getName());
        if (!nestedOrObjectField) {
            xContentBuilder.field("store", fieldAnnotation.store());
        }

        if (fieldAnnotation.fielddata()) {
            xContentBuilder.field("fielddata", fieldAnnotation.fielddata());
        }

        if (FieldType.Auto != fieldAnnotation.type()) {
            xContentBuilder.field("type", fieldAnnotation.type().name().toLowerCase());
            if (FieldType.Date == fieldAnnotation.type() && DateFormat.none != fieldAnnotation.format()) {
                xContentBuilder.field("format", DateFormat.custom == fieldAnnotation.format() ? fieldAnnotation.pattern() : fieldAnnotation.format());
            }
        }

        if (!fieldAnnotation.index()) {
            xContentBuilder.field("index", fieldAnnotation.index());
        }

        if (org.apache.commons.lang.StringUtils.isNotBlank(fieldAnnotation.searchAnalyzer())) {
            xContentBuilder.field("search_analyzer", fieldAnnotation.searchAnalyzer());
        }

        if (org.apache.commons.lang.StringUtils.isNotBlank(fieldAnnotation.analyzer())) {
            xContentBuilder.field("analyzer", fieldAnnotation.analyzer());
        }

        xContentBuilder.endObject();
    }

    private static void addNestedFieldMapping(XContentBuilder builder, java.lang.reflect.Field field, InnerField annotation) throws IOException {
        builder.startObject(annotation.suffix());
        if (FieldType.Auto != annotation.type()) {
            builder.field("type", annotation.type().name().toLowerCase());
        }

        if (!annotation.index()) {
            builder.field("index", annotation.index());
        }

        if (org.apache.commons.lang.StringUtils.isNotBlank(annotation.searchAnalyzer())) {
            builder.field("search_analyzer", annotation.searchAnalyzer());
        }

        if (org.apache.commons.lang.StringUtils.isNotBlank(annotation.indexAnalyzer())) {
            builder.field("analyzer", annotation.indexAnalyzer());
        }

        if (annotation.fielddata()) {
            builder.field("fielddata", annotation.fielddata());
        }

        builder.endObject();
    }

    private static void addMultiFieldMapping(XContentBuilder builder, java.lang.reflect.Field field, MultiField annotation, boolean nestedOrObjectField) throws IOException {
        builder.startObject(field.getName());
        builder.field("type", annotation.mainField().type());
        builder.startObject("fields");
        InnerField[] var4 = annotation.otherFields();
        int var5 = var4.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            InnerField innerField = var4[var6];
            addNestedFieldMapping(builder, field, innerField);
        }

        builder.endObject();
        builder.endObject();
    }

    protected static boolean isEntity(java.lang.reflect.Field field) {
        TypeInformation typeInformation = ClassTypeInformation.from(field.getType());
        Class<?> clazz = getFieldType(field);
        boolean isComplexType = !SIMPLE_TYPE_HOLDER.isSimpleType(clazz);
        return isComplexType && !Map.class.isAssignableFrom(typeInformation.getType());
    }

    protected static Class<?> getFieldType(java.lang.reflect.Field field) {
        ResolvableType resolvableType = ResolvableType.forField(field);
        if (resolvableType.isArray()) {
            return resolvableType.getComponentType().getRawClass();
        } else {
            ResolvableType componentType = resolvableType.getGeneric(new int[]{0});
            return Iterable.class.isAssignableFrom(field.getType()) && componentType != ResolvableType.NONE ? componentType.getRawClass() : resolvableType.getRawClass();
        }
    }

    private static boolean isAnyPropertyAnnotatedAsField(java.lang.reflect.Field[] fields) {
        if (fields != null) {
            java.lang.reflect.Field[] var1 = fields;
            int var2 = fields.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                java.lang.reflect.Field field = var1[var3];
                if (field.isAnnotationPresent(Field.class)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isIdField(java.lang.reflect.Field field, String idFieldName) {
        return idFieldName.equals(field.getName());
    }

    private static boolean isInIgnoreFields(java.lang.reflect.Field field, Field parentFieldAnnotation) {
        if (null != parentFieldAnnotation) {
            String[] ignoreFields = parentFieldAnnotation.ignoreFields();
            return Arrays.asList(ignoreFields).contains(field.getName());
        } else {
            return false;
        }
    }

    private static boolean isNestedOrObjectField(java.lang.reflect.Field field) {
        Field fieldAnnotation = (Field)field.getAnnotation(Field.class);
        return fieldAnnotation != null && (FieldType.Nested == fieldAnnotation.type() || FieldType.Object == fieldAnnotation.type());
    }

    private static boolean isGeoPointField(java.lang.reflect.Field field) {
        return field.getType() == GeoPoint.class || field.getAnnotation(GeoPointField.class) != null;
    }

    private static boolean isCompletionField(java.lang.reflect.Field field) {
        return field.getType() == Completion.class;
    }

    static {
        SIMPLE_TYPE_HOLDER = SimpleTypeHolder.DEFAULT;
    }
}
