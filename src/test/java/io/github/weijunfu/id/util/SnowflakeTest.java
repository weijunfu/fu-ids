package io.github.weijunfu.id.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnowflakeTest {

  private static final long DEFAULT_EPOCH = 1609459200000L;
  private static final long TEST_TIMESTAMP = DEFAULT_EPOCH + 1000L;

  @Test
  void defaultConstructorUsesFixedDefaults() {
    Snowflake snowflake = new FixedTimeSnowflake();

    assertEquals(expectedId(1L, 1L, DEFAULT_EPOCH), snowflake.nextId());
  }

  @Test
  void twoArgumentConstructorUsesDefaultEpoch() {
    Snowflake snowflake = new FixedTimeSnowflake(2L, 3L);

    assertEquals(expectedId(2L, 3L, DEFAULT_EPOCH), snowflake.nextId());
  }

  @Test
  void threeArgumentConstructorUsesExplicitValues() {
    long customEpoch = DEFAULT_EPOCH - 1000L;
    Snowflake snowflake = new FixedTimeSnowflake(4L, 5L, customEpoch);

    assertEquals(expectedId(4L, 5L, customEpoch), snowflake.nextId());
  }

  private static long expectedId(long workerId, long datacenterId, long epoch) {
    return ((TEST_TIMESTAMP - epoch) << 22)
        | (datacenterId << 17)
        | (workerId << 12);
  }

  private static final class FixedTimeSnowflake extends Snowflake {

    private FixedTimeSnowflake() {
      super();
    }

    private FixedTimeSnowflake(long workerId, long datacenterId) {
      super(workerId, datacenterId);
    }

    private FixedTimeSnowflake(long workerId, long datacenterId, long epoch) {
      super(workerId, datacenterId, epoch);
    }

    @Override
    protected long timeGen() {
      return TEST_TIMESTAMP;
    }
  }
}
