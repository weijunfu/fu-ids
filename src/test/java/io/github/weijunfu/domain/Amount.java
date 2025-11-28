package io.github.weijunfu.domain;

import io.github.weijunfu.id.amount.annotation.JsonAmount;

import java.io.Serializable;

public class Amount implements Serializable {

  private static final long serialVersionUID = 291217L;

  @JsonAmount
  private Double amount1;

  @JsonAmount
  private Double amount2;

  @JsonAmount(precision = 3)
  private Double amount3;

  @JsonAmount(precision = 3)
  private Double amount4;

  public Amount() {
  }

  public Double getAmount1() {
    return amount1;
  }

  public void setAmount1(Double amount1) {
    this.amount1 = amount1;
  }

  public Double getAmount2() {
    return amount2;
  }

  public void setAmount2(Double amount2) {
    this.amount2 = amount2;
  }

  public Double getAmount3() {
    return amount3;
  }

  public void setAmount3(Double amount3) {
    this.amount3 = amount3;
  }

  public Double getAmount4() {
    return amount4;
  }

  public void setAmount4(Double amount4) {
    this.amount4 = amount4;
  }

  @Override
  public String toString() {
    return "Amount{" +
        "amount1=" + amount1 +
        ", amount2=" + amount2 +
        ", amount3=" + amount3 +
        ", amount4=" + amount4 +
        '}';
  }
}
