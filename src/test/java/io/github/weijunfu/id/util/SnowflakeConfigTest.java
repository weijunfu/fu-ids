package io.github.weijunfu.id.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnowflakeConfigTest {

  @Test
  void loadYamlConfigSupportsNestedSnowflakeConfig() throws Exception {
    String yaml = """
        fu-ids:
          snowflake:
            epoch: 1609459200000L
            workerId: 2
            datacenter-id: 3
        """;

    Properties props = Snowflake.loadYamlConfig(toInputStream(yaml));

    assertEquals("1609459200000L", props.getProperty("fu-ids.snowflake.epoch"));
    assertEquals("2", props.getProperty("fu-ids.snowflake.workerId"));
    assertEquals("3", props.getProperty("fu-ids.snowflake.datacenter-id"));
  }

  @Test
  void loadYamlConfigSupportsFlatKeysCommentsAndQuotedValues() throws Exception {
    String yaml = """
        # Snowflake settings
        fu-ids.snowflake.epoch: "1700000000000" # custom epoch
        fu-ids.snowflake.worker-id: '4'
        fu-ids.snowflake.datacenterId: 5
        """;

    Properties props = Snowflake.loadYamlConfig(toInputStream(yaml));

    assertEquals("1700000000000", props.getProperty("fu-ids.snowflake.epoch"));
    assertEquals("4", props.getProperty("fu-ids.snowflake.worker-id"));
    assertEquals("5", props.getProperty("fu-ids.snowflake.datacenterId"));
  }

  private static ByteArrayInputStream toInputStream(String value) {
    return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
  }
}
