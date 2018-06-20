package com.ztdx.eams.domain.archives.model.condition;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import com.ztdx.eams.basic.exception.InvalidArgumentException;
import lombok.Data;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.boot.jackson.JsonObjectSerializer;

import javax.persistence.Convert;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonComponent
public class Condition<T> {

    @Convert(converter = Operator.EnumConverter.class)
    private Operator.logical logical;

    private String column;

    @Convert(converter = Operator.logical.EnumConverter.class)
    private Operator operator;

    private Boolean nested;

    private T value;

    public static class Serializer extends JsonObjectSerializer<Condition> {

        @Override
        protected void serializeObject(Condition value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

            if (value.getLogical() != null) {
                jgen.writeStringField("logical", value.getLogical().getCode());
            }

            if (value.getColumn() != null) {
                jgen.writeStringField("column", value.getColumn());
            }

            if (value.getOperator() != null) {
                jgen.writeStringField("operator", value.getOperator().getCode());
            }

            jgen.writeFieldName("value");
            if (value.getNested() && value.getValue() instanceof List){
                jgen.writeStartArray();
                List<Condition> list =(List<Condition>) value.getValue();
                for (Condition condition : list) {
                    serialize(condition, jgen, provider);
                }
                jgen.writeEndArray();
            }else{
                jgen.writeObject(value.getValue());
            }
        }
    }

    public static class Deserializer extends JsonObjectDeserializer<Condition> {

        @Override
        protected Condition deserializeObject(JsonParser jsonParser, DeserializationContext context, ObjectCodec codec, JsonNode tree) throws IOException {
            Condition condition = new Condition();
            condition.setNested(false);
            if (tree.hasNonNull("logical")) {
                condition.setLogical(Operator.logical.create(tree.get("logical").asText()));
            }

            boolean nested = false;
            if (tree.hasNonNull("value")) {
                JsonNode node = tree.get("value");
                if (node.isArray()){
                    List<Condition> list = new ArrayList<>();
                    for (JsonNode item : node) {
                        list.add(deserializeObject(jsonParser, context, codec, item));
                    }
                    condition.setValue(list);
                    condition.setNested(true);
                    nested = true;
                }else{
                    if (node.isNumber()){
                        condition.setValue(node.numberValue());
                    }else if (node.isBoolean()){
                        condition.setValue(node.booleanValue());
                    }else {
                        condition.setValue(node.asText());
                    }
                }
            }else{
                throw new InvalidArgumentException("条件缺少值");
            }

            if (tree.hasNonNull("column")) {
                condition.setColumn(tree.get("column").asText());
            }else if (!nested){
                throw new InvalidArgumentException("条件缺少列名");
            }

            if (tree.hasNonNull("operator")) {
                condition.setOperator(Operator.create(tree.get("operator").asText()));
            }else if (!nested){
                throw new InvalidArgumentException("条件缺少操作");
            }


            return condition;
        }
    }
}
