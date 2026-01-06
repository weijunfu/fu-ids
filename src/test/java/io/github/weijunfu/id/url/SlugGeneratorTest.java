package io.github.weijunfu.id.url;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class SlugTest {

    @Test
    @DisplayName("测试生成Slug")
    void testGenerateSlug() {
        // 测试数据
        String[] testCases = {
                "Hello World!",
                "Java Programming 101",
                "Café & Restaurant - 最好的咖啡",
                "测试标题_2023",
                "A very long title that needs to be truncated for URL slug generation",
                "Special Characters: @#$%^&*()",
                "Mixed 测试 Title 123"
        };

        Arrays.stream(testCases).forEach(e -> {
            String slug = FuURL.toSlug(e);
            System.out.println(slug);
        });
    }

}