package io.github.aparx.challenges.looping.functional;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 13:50 CET, 31.07.2022
 * @since 1.0
 */
@FunctionalInterface
public interface ThrowableConsumer<T> {

    void consume(T t) throws Throwable;

    default @Nonnull ThrowableConsumer<T> andThen(
            final @Nonnull ThrowableConsumer<T> after) {
        Preconditions.checkNotNull(after);
        return (T t) -> { consume(t); after.consume(t); };
    }
}
