package io.github.weijunfu.id.util;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * Nano ID 生成器。
 *
 * <p>默认生成 21 位 URL 安全随机 ID，并使用拒绝采样保证自定义字母表中每个字符
 * 被选中的概率一致。</p>
 */
public final class NanoId {

  /** Nano ID 官方 URL 安全字母表。 */
  public static final String DEFAULT_ALPHABET =
      "useandom-26T198340PX75pxJACKVERYMINDBUSHWOLF_GQZbfghjklqvwyzrict";

  /** 默认 ID 长度，使用 64 字符字母表时可提供 126 bit 随机性。 */
  public static final int DEFAULT_SIZE = 21;

  private static final int RANDOM_BYTE_VALUES = 256;
  private static final int MAX_ALPHABET_SIZE = 256;
  private static final double STEP_FACTOR = 1.6D;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final int[] DEFAULT_SYMBOLS = DEFAULT_ALPHABET.codePoints().toArray();

  private NanoId() {
  }

  /**
   * 使用默认字母表和默认长度生成 Nano ID。
   *
   * @return 21 位 URL 安全 Nano ID
   */
  public static String randomNanoId() {
    return randomNanoId(SECURE_RANDOM, DEFAULT_SYMBOLS, DEFAULT_SIZE);
  }

  /**
   * 使用默认字母表生成指定长度的 Nano ID。
   *
   * @param size ID 长度，不能小于 0
   * @return Nano ID；长度为 0 时返回空字符串
   */
  public static String randomNanoId(int size) {
    return randomNanoId(SECURE_RANDOM, DEFAULT_SYMBOLS, size);
  }

  /**
   * 使用自定义字母表和长度生成 Nano ID。
   *
   * @param alphabet 由 1 至 256 个不重复 Unicode 字符组成的字母表
   * @param size     ID 长度，按 Unicode 字符数计算，不能小于 0
   * @return Nano ID；长度为 0 时返回空字符串
   */
  public static String randomNanoId(String alphabet, int size) {
    return randomNanoId(SECURE_RANDOM, alphabet, size);
  }

  static String randomNanoId(RandomGenerator random, String alphabet, int size) {
    return randomNanoId(random, validateAndGetSymbols(alphabet), size);
  }

  private static String randomNanoId(RandomGenerator random, int[] symbols, int size) {
    if (random == null) {
      throw new IllegalArgumentException("random 不能为空");
    }
    if (size < 0) {
      throw new IllegalArgumentException("size 不能小于 0");
    }
    if (size == 0) {
      return "";
    }

    // 仅接收能被字母表长度整除的随机值区间，避免取模偏差。
    int safeByteCutoff = RANDOM_BYTE_VALUES - (RANDOM_BYTE_VALUES % symbols.length);
    int step = calculateStep(size, safeByteCutoff);
    byte[] bytes = new byte[step];
    StringBuilder id = new StringBuilder(size);
    int idLength = 0;

    while (idLength < size) {
      random.nextBytes(bytes);
      for (byte value : bytes) {
        int unsignedValue = value & 0xFF;
        if (unsignedValue < safeByteCutoff) {
          id.appendCodePoint(symbols[unsignedValue % symbols.length]);
          idLength++;
          if (idLength == size) {
            return id.toString();
          }
        }
      }
    }

    return id.toString();
  }

  private static int[] validateAndGetSymbols(String alphabet) {
    if (alphabet == null || alphabet.isEmpty()) {
      throw new IllegalArgumentException("alphabet 不能为空");
    }

    int[] symbols = alphabet.codePoints().toArray();
    if (symbols.length > MAX_ALPHABET_SIZE) {
      throw new IllegalArgumentException("alphabet 不能超过 " + MAX_ALPHABET_SIZE + " 个字符");
    }

    Set<Integer> uniqueSymbols = new HashSet<>(symbols.length);
    for (int symbol : symbols) {
      if (!uniqueSymbols.add(symbol)) {
        throw new IllegalArgumentException("alphabet 不能包含重复字符");
      }
    }
    return symbols;
  }

  private static int calculateStep(int size, int safeByteCutoff) {
    // 一次预取略多的随机字节，减少拒绝采样触发额外随机源调用的概率。
    long step = (long) Math.ceil(STEP_FACTOR * RANDOM_BYTE_VALUES * size / safeByteCutoff);
    if (step > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("size 过大");
    }
    return Math.max((int) step, 1);
  }
}
