package com.ververica.playground.state;

import com.ververica.playground.state.data.MyHolder;
import com.ververica.playground.state.data.MyPojo;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class StateUpdateProcessFunction extends KeyedProcessFunction<Integer, MyPojo, MyHolder> {

  private transient ValueState<MyHolder> holder;

  @Override
  public void open(Configuration parameters) {
    ValueStateDescriptor<MyHolder> stateDescriptor = new ValueStateDescriptor<>("holder", MyHolder.class);
    holder = getRuntimeContext().getState(stateDescriptor);
  }

  @Override
  public void processElement(MyPojo pojo, Context ctx, Collector<MyHolder> out) throws Exception {
    MyHolder myHolder = holder.value();
    if(myHolder == null){
      myHolder = new MyHolder();
    }
    myHolder.addPojo(pojo);
    holder.update(myHolder);
    out.collect(myHolder);
  }
}
