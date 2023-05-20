package com.nebula.web.boot.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.nebula.base.utils.TimeUtil;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @author : wh
 * @date : 2023/5/20 15:33
 * @description:
 *  <br>
 *  使用方式
 *   @JsonDeserialize(using = LocalDateTimeJacksonDeserializer.class)
 *   private LocalDateTime shipTime;
 *  </> 
 */
public class LocalDateTimeJacksonDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(
        JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        long longValue = jsonParser.getLongValue();
        return TimeUtil.toLocalDateTime(longValue);
    }
}
