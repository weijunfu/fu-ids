package io.github.weijunfu.id.util;

/**
 * String 工具类
 */
public class StringUtil {

  /**
   * 判断字符串是否有内容
   * @param str
   * @return
   */
  public static boolean hasText(String str) {
    return (str != null && !str.isEmpty() && containsText(str));
  }

  /**
   * 检查字符串是否含有文本
   * @param str
   * @return
   */
  private static boolean containsText(CharSequence str) {
    int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }


  public static boolean isNumeric(String str) {
    if (str == null || str.isEmpty()) {
      return false;
    }
    return str.matches("-?\\d+(\\.\\d+)?"); // 支持负数和小数
  }
}
