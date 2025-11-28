package io.github.weijunfu.id.view;

import io.github.weijunfu.id.annotation.JsonContentLong;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class IdsView implements Serializable {

  private static final long serialVersionUID = 291217L;

  @JsonContentLong
  private List<Long> ids;

  public IdsView() {
  }

  public IdsView(List<Long> ids) {
    this.ids = ids;
  }

  public List<Long> getIds() {
    return ids;
  }

  public void setIds(List<Long> ids) {
    this.ids = ids;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    IdsView idsView = (IdsView) o;
    return Objects.equals(getIds(), idsView.getIds());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getIds());
  }

  @Override
  public String toString() {
    return "IdsView{" +
        "ids=" + ids +
        '}';
  }
}
