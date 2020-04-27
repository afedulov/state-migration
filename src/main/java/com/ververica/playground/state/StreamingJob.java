package com.ververica.playground.state;

import com.ververica.playground.sources.IntegerSource;
import com.ververica.playground.state.data.MyHolder;
import com.ververica.playground.state.data.MyPojo;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class StreamingJob {

  private static final int PARALLELISM = 4;
  private static final int CHECKPOINT_INTERVAL_MS = 1000;

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());

    env.setParallelism(PARALLELISM);
    env.enableCheckpointing(CHECKPOINT_INTERVAL_MS);

    ParameterTool params = ParameterTool.fromArgs(args);
    DataStream<MyHolder> stream = setupJob(params, env);

    stream.print();

//    env.getConfig().disableGenericTypes();

    TypeSerializer<MyHolder> serializer =
        stream.getType().createSerializer(env.getConfig());

    env.execute("StreamingJob");
  }
  public static SingleOutputStreamOperator<MyHolder> setupJob(ParameterTool params,
      StreamExecutionEnvironment env) {
    //noinspection Convert2MethodRef
    return env.addSource(new IntegerSource(1, 20))
        .uid("source")
        .map(i -> new MyPojo(i))
        .uid("map")
        .keyBy(i -> i.getId()%5)
        .process(new StateUpdateProcessFunction())
        .uid("state-update");

  }


}
