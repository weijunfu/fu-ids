package io.github.weijunfu.id.date;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试类
 */
class FuDateTest {

    @Test
    void test() {
        LocalDateTime now = LocalDateTime.of(LocalDate.of(2026, 1, 9), LocalTime.of(14, 30, 45));

        assertEquals("2026-01-09 14:30:45", FuDate.format(now));

        assertEquals("2026-01-09", FuDate.format(now.toLocalDate()));

        assertEquals("14:30:45", FuDate.format(now.toLocalTime()));

        assertEquals("2026-01", FuDate.format(now.toLocalDate(), "yyyy-MM"));
    }

}