package io.github.weijunfu.id.url;

import java.util.Locale;
import java.util.regex.Pattern;

public class FuURL {

    private static final Pattern NORMALIZE = Pattern.compile(
            "[\\s]+|[^\\p{Alnum}a-z0-9\\-]+|-{2,}|^-|-$"
    );

    /**
     * 将标题字符串转换为URL友好的slug格式
     *
     * @param title 输入的标题字符串
     * @return 转换后的slug字符串
     * @throws IllegalArgumentException 如果输入的标题字符串为null
     * @author weijunfu
     *
     */
    public static String toSlug(String title) {
        if (title == null || title.isBlank()) {
            return "";
        }

        return NORMALIZE.matcher(title.toLowerCase(Locale.ROOT))
                .replaceAll(match -> {
                    String m = match.group();
                    return m.startsWith("-") ? "" : "-";
                })
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }
}
