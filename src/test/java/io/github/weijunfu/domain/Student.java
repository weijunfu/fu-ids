package io.github.weijunfu.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.weijunfu.id.FuIdDeserializer;
import io.github.weijunfu.id.FuIdSerializer;

import java.io.Serializable;

/**
 * test class
 */
public class Student implements Serializable {

  @JsonSerialize(using = FuIdSerializer.class)
  @JsonDeserialize(using = FuIdDeserializer.class)
  private Long id;

  private String name;

  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  @Override
  public String toString() {
    return "Student{" +
        "id=" + id +
        ", name='" + name + '\'' +
        '}';
  }
}
