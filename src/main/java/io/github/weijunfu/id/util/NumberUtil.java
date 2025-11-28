package io.github.weijunfu.id.util;

import java.text.DecimalFormat;
import java.util.Collections;

public class NumberUtil {

  public static String format(Double value, int precision) {
    if (value == null) return null;

    // 参数边界检查
    if (precision < 0) {
      throw new IllegalArgumentException("Precision must be non-negative");
    }

    try {
      DecimalFormat df = new DecimalFormat(genDoublePattern(precision));
      return df.format(value);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid pattern generated for precision: " + precision, e);
    }
  }

  private static String genDoublePattern(int precision) {
    return "0." + "#".repeat(precision);
  }
}
