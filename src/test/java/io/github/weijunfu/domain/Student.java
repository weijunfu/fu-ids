package io.github.weijunfu.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.weijunfu.id.FuIdDeserializer;
import io.github.weijunfu.id.FuIdSerializer;
import io.github.weijunfu.id.annotation.JsonLong;

import java.io.Serializable;

/**
 * test class
 */
public class Student implements Serializable {

  @JsonSerialize(using = FuIdSerializer.class)
  @JsonDeserialize(using = FuIdDeserializer.class)
  private Long id;

  @JsonLong
  private Long idSchool;

  private String name;

  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }

  public Long getIdSchool() {
    return idSchool;
  }
  public void setIdSchool(Long idSchool) {
    this.idSchool = idSchool;
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
        ", idSchool=" + idSchool +
        ", name='" + name + '\'' +
        '}';
  }
}
