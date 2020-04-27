package com.ververica.playground.state;

import com.ververica.playground.state.data.MyHolder;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;
import org.apache.flink.runtime.state.memory.MemoryStateBackend;
import org.apache.flink.state.api.ExistingSavepoint;
import org.apache.flink.state.api.Savepoint;
import org.apache.flink.state.api.functions.KeyedStateReaderFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadState {

  private static final Logger LOG = LoggerFactory.getLogger(ReadState.class);

  public static void main(String[] args) throws Exception {
    ExecutionEnvironment bEnv   = ExecutionEnvironment.getExecutionEnvironment();
    ExistingSavepoint savepoint = Savepoint.load(bEnv, "/Users/alex/development/ververica/state-migration/src/test/resources/statemigration/savepoint-ab4b16-8a08226da433", new RocksDBStateBackend(new MemoryStateBackend()));
    DataSet<MyHolder> keyedState  = savepoint.readKeyedState("state-update", new ReaderFunction());
    LOG.info("Items in state: {}", keyedState.count());
  }

  public static class ReaderFunction extends KeyedStateReaderFunction<Integer, MyHolder> {

    private static final Logger LOG = LoggerFactory.getLogger(ReaderFunction.class);
    private transient ValueState<MyHolder> holder;

    @Override
    public void open(Configuration parameters) {
      ValueStateDescriptor<MyHolder> stateDescriptor = new ValueStateDescriptor<>("holder", MyHolder.class);
      holder = getRuntimeContext().getState(stateDescriptor);
    }

    @Override
    public void readKey(Integer key, Context ctx, Collector<MyHolder> out) throws Exception {
      MyHolder value = holder.value();
      LOG.info("Key {} -> {}", key, value);
      out.collect(value);
    }
  }
}
