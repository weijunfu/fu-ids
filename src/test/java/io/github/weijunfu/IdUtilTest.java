package io.github.weijunfu;

import io.github.weijunfu.id.util.FuIds;
import io.github.weijunfu.id.util.IdUtil;
import org.junit.jupiter.api.Test;

public class IdUtilTest {

  @Test
  void isHashIds() {
    String encode = FuIds.getInstance().encode(1234567L);
    System.out.println(encode);

    Boolean flag = IdUtil.isHashIds(encode);
    System.out.println(flag);

    Long decode = FuIds.getInstance().decode(encode);
    System.out.println(decode);
  }

  @Test
  void isHashIds2() {
    String encode = "xxx";
    System.out.println(encode);

    Boolean flag = IdUtil.isHashIds(encode);
    System.out.println(flag);
  }
}
