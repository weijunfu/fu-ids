package io.github.weijunfu.id.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdUtil {

  private static final Logger log = LoggerFactory.getLogger(IdUtil.class);

  /**
   * 判断字符串是否是 HashIds 加密后的字符串
   * @param encoded HashIds 加密后的字符串
   * @param instance FuIds 实例
   * @return
   */
  public static Boolean isHashIds(String encoded, FuIds instance) {
    if(!StringUtil.hasText(encoded)) {    // 非空
      return false;
    }
    try {
      FuIds.getInstance().decode(encoded);
      return true;
    } catch (Exception e) {
      log.warn("FuIds encode error: encoded={}\t\terrMsg={}", encoded, e.getMessage());

      return false;
    }
  }

  /**
   * 判断字符串是否是 HashIds 加密后的字符串
   * @param encoded HashIds 加密后的字符串
   * @return
   */
  public static Boolean isHashIds(String encoded) {
    return isHashIds(encoded, FuIds.getInstance());
  }

  /**
   * 创建Snowflake（雪花）算法的生成器
   *
   * @param workerId     终端ID
   * @param datacenterId 数据中心ID
   * @return Snowflake
   */
  public static Snowflake getSnowflake(long workerId, long datacenterId) {
    return new Snowflake(workerId, datacenterId);
  }

  /**
   * 使用默认workerId 和 datacenterId 创建 Snowflake
   */
  public static Snowflake getSnowflake() {
    return new Snowflake();
  }

  /**
   * 快捷生成 Snowflake ID
   */
  public static long getSnowflakeNextId() {
    return new Snowflake().nextId();
  }

  /**
   * 快捷生成 Snowflake 字符串 ID
   */
  public static String getSnowflakeNextIdStr() {
    return new Snowflake().nextIdStr();
  }

}
