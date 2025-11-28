package io.github.weijunfu.id.util;

import io.github.weijunfu.id.time.SystemClock;

public class Snowflake {

  /** 默认开始时间截 (2020-01-01) */
  private final long epoch = 1577836800000L;

  /** 机器ID所占位数 */
  private final long workerIdBits = 5L;

  /** 数据中心ID所占位数 */
  private final long datacenterIdBits = 5L;

  /** 最大机器ID */
  private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

  /** 最大数据中心ID */
  private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

  /** 序列在ID中占的位数 */
  private final long sequenceBits = 12L;

  /** 机器ID向左移12位 */
  private final long workerIdShift = sequenceBits;

  /** 数据中心ID向左移17位 */
  private final long datacenterIdShift = sequenceBits + workerIdBits;

  /** 时间戳向左移22位 */
  private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

  /** 4095 - 序列掩码 */
  private final long sequenceMask = -1L ^ (-1L << sequenceBits);

  /** 工作机器ID */
  private long workerId;

  /** 数据中心ID */
  private long datacenterId;

  /** 每毫秒自增序列 */
  private long sequence = 0L;

  /** 上次生成ID的时间 */
  private long lastTimestamp = -1L;

  public Snowflake() {
    this(0, 0);
  }

  public Snowflake(long workerId, long datacenterId) {
    if (workerId > maxWorkerId || workerId < 0) {
      throw new IllegalArgumentException(
          String.format("Worker Id can't be greater than %d or less than 0", maxWorkerId));
    }
    if (datacenterId > maxDatacenterId || datacenterId < 0) {
      throw new IllegalArgumentException(
          String.format("Datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
    }
    this.workerId = workerId;
    this.datacenterId = datacenterId;
  }

  /** 主方法：生成下一个 ID */
  public synchronized long nextId() {
    long timestamp = timeGen();

    // 时钟回拨
    if (timestamp < lastTimestamp) {
      throw new IllegalStateException(
          String.format("Clock moved backwards. Refusing for %d milliseconds",
              lastTimestamp - timestamp));
    }

    if (timestamp == lastTimestamp) {
      // 同一毫秒内自增
      sequence = (sequence + 1) & sequenceMask;
      if (sequence == 0) {
        // 序列溢出，等待下一毫秒
        timestamp = tilNextMillis(lastTimestamp);
      }
    } else {
      // 新毫秒重置序列
      sequence = 0L;
    }

    lastTimestamp = timestamp;

    return ((timestamp - epoch) << timestampLeftShift)
        | (datacenterId << datacenterIdShift)
        | (workerId << workerIdShift)
        | sequence;
  }

  public String nextIdStr() {
    return Long.toString(nextId());
  }

  private long tilNextMillis(long lastTimestamp) {
    long timestamp = timeGen();
    while (timestamp <= lastTimestamp) {
      timestamp = timeGen();
    }
    return timestamp;
  }

  /** 使用 SystemClock — 性能更好 */
  private long timeGen() {
    return SystemClock.now();
  }

}
