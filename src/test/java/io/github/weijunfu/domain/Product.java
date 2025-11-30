package io.github.weijunfu.domain;

import io.github.weijunfu.id.amount.annotation.JsonAmount;

public class Product {

  @JsonAmount(precision = 3)
  private Double unitPrice;

  @JsonAmount
  private Double mouldCost;

  public Double getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(Double unitPrice) {
    this.unitPrice = unitPrice;
  }

  public Double getMouldCost() {
    return mouldCost;
  }

  public void setMouldCost(Double mouldCost) {
    this.mouldCost = mouldCost;
  }

  @Override
  public String toString() {
    return "Product{" +
        "unitPrice=" + unitPrice +
        ", mouldCost=" + mouldCost +
        '}';
  }
}
