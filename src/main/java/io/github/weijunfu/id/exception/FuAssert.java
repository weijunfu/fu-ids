package io.github.weijunfu.id.exception;

import java.util.Objects;

public class FuAssert {

    public static void notEmpty(String str, String message) {
        if(Objects.isNull(str) || str.trim().length() == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private FuAssert(){}
}
