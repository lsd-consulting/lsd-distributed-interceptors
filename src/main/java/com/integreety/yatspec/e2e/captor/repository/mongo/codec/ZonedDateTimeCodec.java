package com.integreety.yatspec.e2e.captor.repository.mongo.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integreety.yatspec.e2e.config.mapper.ObjectMapperCreator;
import lombok.SneakyThrows;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.time.ZonedDateTime;

public class ZonedDateTimeCodec  implements Codec<ZonedDateTime> {
    private final ObjectMapper objectMapper = new ObjectMapperCreator().getObjectMapper();

    @SneakyThrows
    @Override
    public void encode(final BsonWriter writer, final ZonedDateTime value, final EncoderContext encoderContext) {
        writer.writeString(objectMapper.writeValueAsString(value));
    }

    @SneakyThrows
    @Override
    public ZonedDateTime decode(final BsonReader reader, final DecoderContext decoderContext) {
        return objectMapper.readValue(reader.readString(), ZonedDateTime.class);
    }

    @Override
    public Class<ZonedDateTime> getEncoderClass() {
        return ZonedDateTime.class;
    }
}