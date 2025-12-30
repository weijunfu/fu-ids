package io.github.weijunfu.id.string;

import java.util.Objects;

public class FuString {

    public static boolean isEmpty(String str) {
        return Objects.isNull(str) || str.isEmpty();
    }

    private FuString() {}
}
