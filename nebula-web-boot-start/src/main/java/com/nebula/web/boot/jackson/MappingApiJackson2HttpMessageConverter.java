package com.nebula.web.boot.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.Nullable;

/**
 * MappingApiJackson2HttpMessageConverter
 */
public class MappingApiJackson2HttpMessageConverter extends AbstractReadWriteJackson2HttpMessageConverter {

    @Nullable
    private String jsonPrefix;

    /**
     * Construct a new {@link MappingApiJackson2HttpMessageConverter} using default configuration
     * provided by {@link Jackson2ObjectMapperBuilder}.
     */
    public MappingApiJackson2HttpMessageConverter() {
        this(Jackson2ObjectMapperBuilder.json().build());
    }

    /**
     * Construct a new {@link MappingApiJackson2HttpMessageConverter} with a custom {@link ObjectMapper}.
     * You can use {@link Jackson2ObjectMapperBuilder} to build it easily.
     *
     * @param objectMapper ObjectMapper
     * @see Jackson2ObjectMapperBuilder#json()
     */
    public MappingApiJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper, initWriteObjectMapper(objectMapper), MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
    }

    private static ObjectMapper initWriteObjectMapper(ObjectMapper readObjectMapper) {
        // 拷贝 readObjectMapper
        return readObjectMapper.copy();
    }

    /**
     * Specify a custom prefix to use for this view's JSON output.
     * Default is none.
     *
     * @param jsonPrefix jsonPrefix
     * @see #setPrefixJson
     */
    public void setJsonPrefix(String jsonPrefix) {
        this.jsonPrefix = jsonPrefix;
    }

    /**
     * Indicate whether the JSON output by this view should be prefixed with ")]}', ". Default is false.
     * <p>Prefixing the JSON string in this manner is used to help prevent JSON Hijacking.
     * The prefix renders the string syntactically invalid as a script so that it cannot be hijacked.
     * This prefix should be stripped before parsing the string as JSON.
     *
     * @param prefixJson prefixJson
     * @see #setJsonPrefix
     */
    public void setPrefixJson(boolean prefixJson) {
        this.jsonPrefix = (prefixJson ? ")]}', " : null);
    }

    @Override
    protected void writePrefix(JsonGenerator generator, Object object) throws IOException {
        if (this.jsonPrefix != null) {
            generator.writeRaw(this.jsonPrefix);
        }
    }

}
