package io.github.weijunfu.id.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Snowflake {

  private final static Logger log = LoggerFactory.getLogger(Snowflake.class);

  // =================== 基础配置常量 ===================
  private static final String APPLICATION_PROPERTIES = "application.properties";
  private static final String APPLICATION_YML = "application.yml";
  private static final String APPLICATION_YAML = "application.yaml";
  private static final String FU_IDS_EPOCH = "fu-ids.snowflake.epoch";
  private static final String FU_IDS_WORKER_ID = "fu-ids.snowflake.workerId";
  private static final String FU_IDS_WORKER_ID_KEBAB = "fu-ids.snowflake.worker-id";
  private static final String FU_IDS_DATACENTER_ID = "fu-ids.snowflake.datacenterId";
  private static final String FU_IDS_DATACENTER_ID_KEBAB = "fu-ids.snowflake.datacenter-id";

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
  private static final Properties CONFIG = loadConfig();
  private static final long CONFIG_EPOCH = loadFromConfig(FU_IDS_EPOCH, DEFAULT_EPOCH);
  private static final long CONFIG_WORKER_ID = loadFromConfig(DEFAULT_WORKER_ID, FU_IDS_WORKER_ID, FU_IDS_WORKER_ID_KEBAB);
  private static final long CONFIG_DATACENTER_ID = loadFromConfig(DEFAULT_DATACENTER_ID, FU_IDS_DATACENTER_ID, FU_IDS_DATACENTER_ID_KEBAB);

  // =================== 实例变量 ===================
  private final long workerId;
  private final long datacenterId;
  private final long epoch;
  private long sequence = 0L;
  private long lastTimestamp = -1L;

  /**
   * 构造函数
   * 从配置文件中加载 epoch、workerId、datacenterId
   */
  public Snowflake() {
    this(CONFIG_WORKER_ID, CONFIG_DATACENTER_ID, CONFIG_EPOCH);
  }

  /**
   * 构造函数
   *
   * @param workerId     工作机器ID (0~31)
   * @param datacenterId 数据中心ID (0~31)
   */
  public Snowflake(long workerId, long datacenterId) {
    this(workerId, datacenterId, CONFIG_EPOCH);
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

  private static Properties loadConfig() {
    Properties props = new Properties();
    loadYamlConfig(props, APPLICATION_YML);
    loadYamlConfig(props, APPLICATION_YAML);
    loadPropertiesConfig(props);
    return props;
  }

  private static void loadPropertiesConfig(Properties props) {
    try (InputStream inputStream = Snowflake.class.getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES)) {
      if (inputStream != null) {
        props.load(inputStream);
      }
    } catch (IOException e) {
      log.warn("fu-ids加载配置文件[{}]失败，使用默认 Snowflake 配置", APPLICATION_PROPERTIES, e);
    }
  }

  private static void loadYamlConfig(Properties props, String fileName) {
    try (InputStream inputStream = Snowflake.class.getClassLoader().getResourceAsStream(fileName)) {
      if (inputStream != null) {
        props.putAll(loadYamlConfig(inputStream));
      }
    } catch (IOException e) {
      log.warn("fu-ids加载配置文件[{}]失败，使用默认 Snowflake 配置", fileName, e);
    }
  }

  static Properties loadYamlConfig(InputStream inputStream) throws IOException {
    Properties props = new Properties();
    List<Integer> indents = new ArrayList<>();
    List<String> keys = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#") || "---".equals(trimmed) || "...".equals(trimmed)
            || trimmed.startsWith("-")) {
          continue;
        }

        int separator = trimmed.indexOf(':');
        if (separator <= 0) {
          continue;
        }

        int indent = countLeadingSpaces(line);
        while (!indents.isEmpty() && indents.get(indents.size() - 1) >= indent) {
          indents.remove(indents.size() - 1);
          keys.remove(keys.size() - 1);
        }

        String key = unquoteYamlValue(trimmed.substring(0, separator).trim());
        String rawValue = trimmed.substring(separator + 1);
        String rawValueTrimmed = rawValue.trim();
        if (rawValueTrimmed.isEmpty() || rawValueTrimmed.startsWith("#")) {
          indents.add(indent);
          keys.add(key);
          continue;
        }

        String value = unquoteYamlValue(stripInlineComment(rawValue).trim());
        props.setProperty(buildYamlPropertyKey(keys, key), value);
      }
    }

    return props;
  }

  private static int countLeadingSpaces(String line) {
    int count = 0;
    while (count < line.length() && line.charAt(count) == ' ') {
      count++;
    }
    return count;
  }

  private static String buildYamlPropertyKey(List<String> parentKeys, String key) {
    if (parentKeys.isEmpty()) {
      return key;
    }

    StringBuilder builder = new StringBuilder();
    for (String parentKey : parentKeys) {
      if (builder.length() > 0) {
        builder.append('.');
      }
      builder.append(parentKey);
    }
    return builder.append('.').append(key).toString();
  }

  private static String stripInlineComment(String value) {
    boolean inSingleQuote = false;
    boolean inDoubleQuote = false;
    for (int i = 0; i < value.length(); i++) {
      char current = value.charAt(i);
      if (current == '\'' && !inDoubleQuote) {
        inSingleQuote = !inSingleQuote;
      } else if (current == '"' && !inSingleQuote && (i == 0 || value.charAt(i - 1) != '\\')) {
        inDoubleQuote = !inDoubleQuote;
      } else if (current == '#' && !inSingleQuote && !inDoubleQuote
          && (i == 0 || Character.isWhitespace(value.charAt(i - 1)))) {
        return value.substring(0, i);
      }
    }
    return value;
  }

  private static String unquoteYamlValue(String value) {
    if (value.length() < 2) {
      return value;
    }

    char first = value.charAt(0);
    char last = value.charAt(value.length() - 1);
    if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }

  private static long loadFromConfig(String key, long defaultValue) {
    return loadFromConfig(defaultValue, key);
  }

  private static long loadFromConfig(long defaultValue, String... keys) {
    String key = null;
    String value = null;
    for (String currentKey : keys) {
      String currentValue = CONFIG.getProperty(currentKey);
      if (currentValue != null && !currentValue.trim().isEmpty()) {
        key = currentKey;
        value = currentValue;
        break;
      }
    }

    if (value == null || value.trim().isEmpty()) {
      return defaultValue;
    }

    value = stripInlineComment(value).trim();
    if (value.endsWith("L") || value.endsWith("l")) {
      value = value.substring(0, value.length() - 1);
    }

    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      log.warn("fu-ids配置项[{}]值非法: {}，使用默认值: {}", key, value, defaultValue);
      return defaultValue;
    }
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
