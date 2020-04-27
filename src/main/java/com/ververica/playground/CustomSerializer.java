package com.ververica.playground;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.time.OffsetDateTime;

public class CustomSerializer extends Serializer<OffsetDateTime> {

  @Override
  public void write(Kryo kryo, Output output, OffsetDateTime object) {

  }

  @Override
  public OffsetDateTime read(Kryo kryo, Input input, Class<OffsetDateTime> type) {
    return null;
  }
}
