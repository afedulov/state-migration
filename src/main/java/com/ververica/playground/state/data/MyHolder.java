package com.ververica.playground.state.data;

import java.util.HashSet;
import java.util.Set;

public class MyHolder {
  HashSet<MyPojo> pojos;

  public MyHolder() {
    this.pojos = new HashSet<>();
  }

  public HashSet<MyPojo> getPojos() {
    return pojos;
  }

  public void setPojos(HashSet<MyPojo> pojos) {
    this.pojos = pojos;
  }

  public void addPojo(MyPojo pojo){
    pojos.add(pojo);
  }

  @Override
  public String toString() {
    return "MyHolder{" +
        "pojos=" + pojos +
        '}';
  }
}
