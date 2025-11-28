package io.github.weijunfu.id.view;

import io.github.weijunfu.id.annotation.JsonLong;

import java.io.Serializable;
import java.util.Objects;

public class IdView implements Serializable {

  private static final long serialVersionUID = 291217L;

  @JsonLong
  private Long id;

  public IdView() {}

  public IdView(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    IdView idView = (IdView) o;
    return Objects.equals(getId(), idView.getId());
  }
  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }
  @Override
  public String toString() {
    return "IdView{" +
        "id=" + id +
        '}';
  }
}
