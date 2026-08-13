package io.github.weijunfu.id.util;

import java.util.concurrent.TimeUnit;

public class Snowflake {

  // =================== 基础常量 ===================
  private static final long DEFAULT_EPOCH = 1609459200000L; // 起始时间戳（2021-01-01 00:00:00 UTC），可自定义
  private static final long DEFAULT_WORKER_ID = 1L;
  private static final long DEFAULT_DATACENTER_ID = 1L;
  private static final long WORKER_ID_BITS = 5L;
  private static final long DATACENTER_ID_BITS = 5L;
  private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS); // 31
  private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS); // 31
  private static final long SEQUENCE_BITS = 12L;

  private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
  private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
  private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
  private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS); // 4095

  // =================== 实例变量 ===================
  private final long workerId;
  private final long datacenterId;
  private final long epoch;
  private long sequence = 0L;
  private long lastTimestamp = -1L;

  /**
   * 使用默认 workerId、datacenterId 和 epoch 创建生成器。
   */
  public Snowflake() {
    this(DEFAULT_WORKER_ID, DEFAULT_DATACENTER_ID, DEFAULT_EPOCH);
  }

  /**
   * 构造函数
   *
   * @param workerId     工作机器ID (0~31)
   * @param datacenterId 数据中心ID (0~31)
   */
  public Snowflake(long workerId, long datacenterId) {
    this(workerId, datacenterId, DEFAULT_EPOCH);
  }

  /**
   * 构造函数
   *
   * @param workerId     工作机器ID (0~31)
   * @param datacenterId 数据中心ID (0~31)
   * @param epoch        起始时间戳（13位毫秒数）
   */
  public Snowflake(long workerId, long datacenterId, long epoch) {
    if (workerId > MAX_WORKER_ID || workerId < 0) {
      throw new IllegalArgumentException("workerId 不能大于 " + MAX_WORKER_ID + " 或小于 0");
    }
    if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
      throw new IllegalArgumentException("datacenterId 不能大于 " + MAX_DATACENTER_ID + " 或小于 0");
    }
    this.workerId = workerId;
    this.datacenterId = datacenterId;
    this.epoch = epoch;
  }

  /**
   * 生成下一个ID（线程安全）
   */
  public synchronized long nextId() {
    long timestamp = timeGen();

    // 时钟回拨处理
    if (timestamp < lastTimestamp) {
      long offset = lastTimestamp - timestamp;
      if (offset <= 5) {
        try {
          // 等待时间追上（最多等待5ms）
          TimeUnit.MILLISECONDS.sleep(offset);
          timestamp = timeGen();
          if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID：" + timestamp);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("等待时钟恢复被中断", e);
        }
      } else {
        throw new RuntimeException("时钟回拨过大，拒绝生成ID：" + timestamp);
      }
    }

    if (lastTimestamp == timestamp) {
      // 同一毫秒内，序列号自增
      sequence = (sequence + 1) & SEQUENCE_MASK;
      if (sequence == 0) {
        // 序列号溢出，等待下一毫秒
        timestamp = tilNextMillis(lastTimestamp);
      }
    } else {
      // 新的一毫秒，重置序列号
      sequence = 0L;
    }

    lastTimestamp = timestamp;

    return ((timestamp - this.epoch) << TIMESTAMP_LEFT_SHIFT)
        | (datacenterId << DATACENTER_ID_SHIFT)
        | (workerId << WORKER_ID_SHIFT)
        | sequence;
  }

  /**
   * 阻塞到下一个毫秒，直到获得新的时间戳
   */
  private long tilNextMillis(long lastTimestamp) {
    long timestamp = timeGen();
    while (timestamp <= lastTimestamp) {
      timestamp = timeGen();
    }
    return timestamp;
  }

  /**
   * 返回当前时间戳（毫秒）
   */
  protected long timeGen() {
    return System.currentTimeMillis();
  }

  // =================== 测试用例 ===================
  public static void main(String[] args) {
    Snowflake idGen = new Snowflake(1, 1);

    // 多线程测试
    for (int i = 0; i < 10; i++) {
      new Thread(() -> {
        for (int j = 0; j < 1000; j++) {
          System.out.println(idGen.nextId());
        }
      }).start();
    }
  }
}
