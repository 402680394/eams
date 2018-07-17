package com.ztdx.eams.basic.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

import static org.apache.commons.lang.StringUtils.isNumeric;

@JsonComponent
public class DateJsonComponent {
    public static class Serializer extends JsonSerializer<Date> {

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeNumber(value.getTime());
        }
    }

    public static class Deserializer extends JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

            String raw = p.getValueAsString();

            //如果是时间戳
            if (isNumeric(raw)) {
                Long timeStamp = Long.parseLong(raw);
                if (raw.length() > 12){
                    return Date.from(Instant.ofEpochMilli(timeStamp));
                }else {
                    return Date.from(Instant.ofEpochSecond(timeStamp));
                }
            }

            //如果是时间字符串
            try {
                return StdDateFormat.getDateInstance().parse(raw);
            } catch (ParseException e) {
                throw new IOException("无法识别的Date格式");
            }
        }
    }
}
