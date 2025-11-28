package io.github.weijunfu.id.time;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 系统时钟
 * 使用单一后台线程周期性更新时钟，减少 System.currentTimeMillis() 调用开销
 *
 */
public class SystemClock {

  /** 单例 */
  private static final SystemClock INSTANCE = new SystemClock(1);

  /** 当前时间戳 */
  private volatile long now;

  /** 定时更新间隔（毫秒） */
  private final long period;

  /**
   * 构造：启动后台更新线程
   */
  private SystemClock(long period) {
    this.period = period;
    this.now = System.currentTimeMillis();
    scheduleClockUpdating();
  }

  /**
   * 获取当前时间戳（毫秒）
   */
  public static long now() {
    return INSTANCE.now;
  }

  /**
   * 获取当前时间的字符串格式
   */
  public static String nowDate() {
    return LocalDateTime.now().toString();
  }

  /**
   * 启动后台线程，每隔 period 毫秒更新一次 now
   */
  private void scheduleClockUpdating() {
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "system-clock");
      t.setDaemon(true); // 守护线程
      return t;
    });

    scheduler.scheduleAtFixedRate(() -> now = System.currentTimeMillis(),
        period, period, TimeUnit.MILLISECONDS);
  }

  @Override
  public String toString() {
    return String.valueOf(now);
  }
}

