package com.imooc.miaoshaproject.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

/**
 * @author: yxy
 * Date: 2021/3/10
 * Time: 9:10
 * 描述:
 */
public class JodaDateTimeJsonDeserializer extends JsonDeserializer<DateTime>
{
    @Override
    public DateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String datesString=jsonParser.readValueAs(String.class);
        DateTimeFormatter formatter= DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");


        return DateTime.parse(datesString,formatter);
    }
}
