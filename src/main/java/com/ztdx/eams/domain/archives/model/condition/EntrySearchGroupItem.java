package com.ztdx.eams.domain.archives.model.condition;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
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
public class EntrySearchGroupItem<T> {

    private String column;

    @Convert(converter = EntrySearchGroupItemSort.EnumConverter.class)
    private EntrySearchGroupItemSort entrySearchGroupItemSort;

    public static class Serializer extends JsonObjectSerializer<EntrySearchGroupItem> {

        @Override
        protected void serializeObject(EntrySearchGroupItem value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

            if (value.getColumn() != null) {
                jgen.writeStringField("column", value.getColumn());
            }

            /*if (value.getOperator() != null) {
                jgen.writeStringField("operator", value.getOperator().getCode());
            }*/

        }
    }

}
