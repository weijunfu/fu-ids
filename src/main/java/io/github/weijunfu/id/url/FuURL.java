package io.github.weijunfu.id.url;

import java.util.regex.Pattern;

public class FuURL {

    private static final Pattern SPACE = Pattern.compile("\\s+");
    private static final Pattern INVALID = Pattern.compile("[^\\p{IsHan}a-z0-9\\-]");
    private static final Pattern MULTI_DASH = Pattern.compile("-{2,}");
    private static final Pattern EDGE_DASH = Pattern.compile("^-|-$");

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
        if (title == null) {
            throw new IllegalArgumentException("Title cannot be null");
        }

        String s = title.toLowerCase();
        s = SPACE.matcher(s).replaceAll("-");
        s = INVALID.matcher(s).replaceAll("");
        s = MULTI_DASH.matcher(s).replaceAll("-");
        s = EDGE_DASH.matcher(s).replaceAll("");
        return s;
    }

    private FuURL() {}
}
