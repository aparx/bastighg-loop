package io.github.aparx.challenges.looping.scheduler;

import com.google.common.base.Preconditions;

import javax.validation.constraints.NotNull;
import java.util.function.Consumer;

/**
 * @author aparx (Vinzent Zeband)
 * @version 15:55 CET, 01.08.2022
 * @since 1.0
 */
public interface DelegatedTask<T extends AbstractTask> {

    static <T extends AbstractTask> DelegatedTask<T> ofUpdate(
            final @NotNull Consumer<T> action) {
        Preconditions.checkNotNull(action);
        return new DelegatedTask<>() {
            @Override
            public void onUpdate(T task) {
                action.accept(task);
            }
        };
    }

    static <T extends AbstractTask> DelegatedTask<T> ofStop(
            final @NotNull Consumer<T> action) {
        Preconditions.checkNotNull(action);
        return new DelegatedTask<>() {
            @Override
            public void onStop(T task) {
                action.accept(task);
            }
        };
    }

    static <T extends AbstractTask> DelegatedTask<T> ofStart(
            final @NotNull Consumer<T> action) {
        Preconditions.checkNotNull(action);
        return new DelegatedTask<>() {
            @Override
            public void onStart(T task) {
                action.accept(task);
            }
        };
    }

    default void onUpdate(@NotNull T task) {}

    default void onStop(@NotNull T task) {}
    default void onStart(@NotNull T task) {}
}
