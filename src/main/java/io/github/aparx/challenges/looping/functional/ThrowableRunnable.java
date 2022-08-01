
package io.github.aparx.challenges.looping.functional;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 13:50 CET, 31.07.2022
 * @since 1.0
 */
@FunctionalInterface
public interface ThrowableRunnable {

    void run() throws Throwable;

    default @Nonnull ThrowableRunnable andThen(
            final @Nonnull ThrowableRunnable after) {
        Preconditions.checkNotNull(after);
        return () -> { run(); after.run(); };
    }
}
