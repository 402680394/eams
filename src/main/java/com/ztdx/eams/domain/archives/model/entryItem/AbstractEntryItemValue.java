package com.ztdx.eams.domain.archives.model.entryItem;

import com.ztdx.eams.domain.archives.model.DescriptionItem;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.lang.Nullable;

import java.lang.reflect.ParameterizedType;

public abstract class AbstractEntryItemValue<T> implements IEntryItemValue<T> {
    protected DescriptionItem descriptionItem;
    protected T value;
    protected Class<T> entityClass;

    public AbstractEntryItemValue(DescriptionItem descriptionItem, Object value) {
        this.descriptionItem = descriptionItem;

        if (value == null) {
            this.value = getDefault();
        } else if (value.getClass().isAssignableFrom(this.getEntityClass())) {
            this.value = (T) value;
        } else {
            this.value = parse(value.toString());
        }

    }

    @Override
    public T get() {
        return value;
    }

    protected abstract T getDefault();

    protected abstract T parse(@Nullable String value);

    private Class<T> getEntityClass() {
        if (!this.isEntityClassSet()) {
            try {
                this.entityClass = this.resolveReturnedClassFromGenericType();
            } catch (Exception var2) {
                throw new InvalidDataAccessApiUsageException("Unable to resolve EntityClass. Please use according setter!", var2);
            }
        }

        return this.entityClass;
    }

    private boolean isEntityClassSet() {
        return this.entityClass != null;
    }

    private Class<T> resolveReturnedClassFromGenericType() {
        ParameterizedType parameterizedType = this.resolveReturnedClassFromGenericType(this.getClass());
        return (Class)parameterizedType.getActualTypeArguments()[0];
    }

    private ParameterizedType resolveReturnedClassFromGenericType(Class<?> clazz) {
        Object genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)genericSuperclass;
            //Type rawtype = parameterizedType.getRawType();
            //if (SimpleElasticsearchRepository.class.equals(rawtype)) {
            return parameterizedType;
            //}
        }

        return this.resolveReturnedClassFromGenericType(clazz.getSuperclass());
    }
}
