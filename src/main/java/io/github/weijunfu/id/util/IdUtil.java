package io.github.weijunfu.id.util;

import io.github.weijunfu.nanoid.NanoId;
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
   * 获取使用默认 workerId 和 datacenterId 的 Snowflake 单例
   */
  public static Snowflake getSnowflake() {
    return Snowflake.getInstance();
  }

  /**
   * 快捷生成 Snowflake ID
   */
  public static long getSnowflakeNextId() {
    return getSnowflake().nextId();
  }

  /**
   * 使用默认配置生成 Nano ID
   */
  public static String getNanoId() {
    return NanoId.randomNanoId();
  }

  /**
   * 使用默认字母表生成指定长度的 Nano ID
   *
   * @param size ID 长度
   */
  public static String getNanoId(int size) {
    return NanoId.randomNanoId(size);
  }

  /**
   * 使用自定义字母表和长度生成 Nano ID
   *
   * @param alphabet 自定义字母表
   * @param size     ID 长度
   */
  public static String getNanoId(String alphabet, int size) {
    return NanoId.randomNanoId(alphabet, size);
  }

}
