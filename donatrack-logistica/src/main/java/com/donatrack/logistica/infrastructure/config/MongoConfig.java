package com.donatrack.logistica.infrastructure.config;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

/**
 * Configuración de conversión bidireccional entre Binary de MongoDB y java.util.UUID.
 * Garantiza compatibilidad universal entre mongosh (mongo-init.js) y Spring Data MongoDB.
 */
@Configuration
public class MongoConfig {

  @Bean
  public MongoCustomConversions customConversions() {
    List<Converter<?, ?>> converters = new ArrayList<>();
    converters.add(new BinaryToUuidConverter());
    converters.add(new UuidToBinaryConverter());
    return new MongoCustomConversions(converters);
  }

  @ReadingConverter
  public static class BinaryToUuidConverter implements Converter<Binary, UUID> {
    @Override
    public UUID convert(Binary source) {
      if (source == null) return null;
      byte[] data = source.getData();
      if (data == null || data.length != 16) {
        return null;
      }
      ByteBuffer bb = ByteBuffer.wrap(data);
      long mostSigBits = bb.getLong();
      long leastSigBits = bb.getLong();
      return new UUID(mostSigBits, leastSigBits);
    }
  }

  @WritingConverter
  public static class UuidToBinaryConverter implements Converter<UUID, Binary> {
    @Override
    public Binary convert(UUID source) {
      if (source == null) return null;
      ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
      bb.putLong(source.getMostSignificantBits());
      bb.putLong(source.getLeastSignificantBits());
      return new Binary(BsonBinarySubType.UUID_STANDARD.getValue(), bb.array());
    }
  }
}
