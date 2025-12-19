package io.github.weijunfu.id.time;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 时间工具类
 */
public class TimeUtil {

  /**
   * 获取指定日期 00:00:00时辰的时间戳
   * @param localDate 日期
   * @param zoneId 时区
   * @return
   */
  public static Long getTimeMillis(LocalDate localDate, ZoneId zoneId) {
    return localDate.atStartOfDay().atZone(zoneId).toInstant().toEpochMilli();
  }

  /**
   * 获取当前时间戳
   * @return
   */
  public static Long getCurrentTimeMillis(){
    return getTimeMillis(LocalDate.now(), ZoneId.systemDefault());
  }

  private TimeUtil(){}
}
