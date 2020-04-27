package com.ververica.playground.state.data;

import java.util.Objects;

public class MyPojo {
  int id;

  public MyPojo(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MyPojo myPojo = (MyPojo) o;
    return id == myPojo.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "MyPojo{" +
        "id=" + id +
        '}';
  }
}
