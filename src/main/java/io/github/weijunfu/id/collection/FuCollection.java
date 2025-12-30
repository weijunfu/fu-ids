package io.github.weijunfu.id.collection;

import java.util.Collection;
import java.util.Objects;

public class FuCollection {

    public static <T> Boolean isEmpty(T ...t) {
        return Objects.isNull(t) || t.length == 0;
    }

    public static <T> Boolean isNotEmpty(T ...t) {
        return Objects.nonNull(t) && t.length > 0;
    }

    public static <T> Boolean isEmpty(Collection<T> collection) {
        return Objects.isNull(collection) || collection.isEmpty();
    }

    public static <T> Boolean isNotEmpty(Collection<T> collection) {
        return Objects.nonNull(collection) && !collection.isEmpty();
    }

    private FuCollection(){}

}
