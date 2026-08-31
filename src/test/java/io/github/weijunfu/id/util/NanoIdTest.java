package io.github.weijunfu.id.util;

import io.github.weijunfu.nanoid.NanoId;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NanoIdTest {

  @Test
  void generatesDefaultUrlSafeNanoId() {
    String id = NanoId.randomNanoId();

    assertEquals(64, NanoId.DEFAULT_ALPHABET.codePoints().distinct().count());
    assertEquals(NanoId.DEFAULT_SIZE, id.length());
    assertTrue(id.matches("[A-Za-z0-9_-]{21}"));
  }

  @Test
  void generatesNanoIdWithCustomAlphabetAndSize() {
    String id = NanoId.randomNanoId("0123456789abcdef", 32);

    assertEquals(32, id.length());
    assertTrue(id.matches("[0-9a-f]{32}"));
  }

  @Test
  void supportsUnicodeAlphabetByCodePoint() {
    String id = NanoId.randomNanoId("甲乙丙😀", 12);

    assertEquals(12, id.codePointCount(0, id.length()));
    Set<Integer> alphabet = new HashSet<>(Set.of(
        "甲".codePointAt(0), "乙".codePointAt(0), "丙".codePointAt(0), "😀".codePointAt(0)));
    assertTrue(id.codePoints().allMatch(alphabet::contains));
  }

  @Test
  void validatesArguments() {
    assertEquals("", NanoId.randomNanoId(0));
    assertThrows(IllegalArgumentException.class, () -> NanoId.randomNanoId(-1));
    assertThrows(IllegalArgumentException.class, () -> NanoId.randomNanoId(null, 1));
    assertThrows(IllegalArgumentException.class, () -> NanoId.randomNanoId("", 1));
    assertThrows(IllegalArgumentException.class, () -> NanoId.randomNanoId("aab", 1));
    assertThrows(IllegalArgumentException.class, () -> NanoId.randomNanoId("a".repeat(257), 1));
  }

  @Test
  void idUtilExposesNanoIdShortcuts() {
    assertEquals(NanoId.DEFAULT_SIZE, IdUtil.getNanoId().length());
    assertEquals(8, IdUtil.getNanoId(8).length());
    assertTrue(IdUtil.getNanoId("01", 16).matches("[01]{16}"));
  }

  @Test
  void generatesNanoIdsConcurrently() throws Exception {
    int threadCount = 8;
    int idsPerThread = 500;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    List<Callable<List<String>>> tasks = IntStream.range(0, threadCount)
        .mapToObj(ignored -> (Callable<List<String>>) () -> IntStream.range(0, idsPerThread)
            .mapToObj(index -> NanoId.randomNanoId())
            .toList())
        .toList();

    try {
      Set<String> ids = new HashSet<>();
      for (var future : executor.invokeAll(tasks)) {
        ids.addAll(future.get());
      }
      assertEquals(threadCount * idsPerThread, ids.size());
    } finally {
      executor.shutdownNow();
    }
  }

}
