package io.github.aparx.challenges.looping.scheduler;

import javax.validation.constraints.NotNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 15:55 CET, 01.08.2022
 * @since 1.0
 */
@FunctionalInterface
public interface DelegatedTask<T extends AbstractTask> {

    void onUpdate(@NotNull T task);

    default void onStop(@NotNull T task) {}
    default void onStart(@NotNull T task) {}
}
