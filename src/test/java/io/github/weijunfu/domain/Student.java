package io.github.weijunfu.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.weijunfu.id.FuIdDeserializer;
import io.github.weijunfu.id.FuIdSerializer;
import io.github.weijunfu.id.annotation.JsonContentLong;
import io.github.weijunfu.id.annotation.JsonKeyLong;
import io.github.weijunfu.id.annotation.JsonLong;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

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

  @JsonContentLong
  private Long[] nums;

  @JsonKeyLong
  private Map<Long, String> others;

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

  public Long[] getNums() {

    return nums;
  }
  public void setNums(Long[] nums) {

    this.nums = nums;
  }
  public Map<Long, String> getOthers() {

    return others;
  }
  public void setOthers(Map<Long, String> others) {

    this.others = others;
  }

  @Override
  public String toString() {

    return "Student{" +
        "id=" + id +
        ", idSchool=" + idSchool +
        ", name='" + name + '\'' +
        ", nums=" + Arrays.toString(nums) +
        ", others=" + others +
        '}';
  }
}
