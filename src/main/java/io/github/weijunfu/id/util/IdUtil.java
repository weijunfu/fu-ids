package io.github.weijunfu.id.util;

public class IdUtil {

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
