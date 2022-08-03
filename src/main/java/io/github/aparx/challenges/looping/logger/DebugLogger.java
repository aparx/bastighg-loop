package io.github.aparx.challenges.looping.logger;

import com.google.common.base.Preconditions;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author aparx (Vinzent Zeband)
 * @version 17:19 CET, 03.08.2022
 * @since 1.0
 */
public abstract class DebugLogger {

    /* DebugLogger factory methods */

    @NotNull
    public static DebugLogger of(
            final @NotNull Logger delegatingLogger,
            final @NotNull BooleanSupplier debugSupplier) {
        Preconditions.checkNotNull(debugSupplier);
        return new DebugLogger(delegatingLogger) {
            @Override
            public boolean isDebugMode() {
                return debugSupplier.getAsBoolean();
            }
        };
    }

    /* DebugLogger implementation */

    @NotNull @Getter
    private final Logger logger;

    public DebugLogger(Logger logger) {
        this.logger = Preconditions.checkNotNull(logger);
    }

    abstract public boolean isDebugMode();


    public void log(Level level, Supplier<?> msgSupplier, Object... args) {
        if (isDebugMode()) {
            logger.log(level, objectToString(msgSupplier), args);
        }
    }

    public void warning(Supplier<?> msgSupplier) {
        if (isDebugMode()) {
            logger.info(() -> objectToString(msgSupplier));
        }
    }

    public void info(Supplier<?> msgSupplier) {
        if (isDebugMode()) {
            logger.info(() -> objectToString(msgSupplier));
        }
    }

    public void warning(Supplier<?> msgSupplier, Object... args) {
        warning(() -> formatObjectToString(msgSupplier, args));
    }

    public void info(Supplier<?> msgSupplier, Object... args) {
        info(() -> formatObjectToString(msgSupplier, args));
    }

    private static String typeErasedArrayToString(Object o) {
        if (!o.getClass().isArray()) return "[]";
        final int n = Array.getLength(o);
        if (n == 0) return "[]";
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (int i = 0; i < n; ) {
            builder.append(Array.get(o, i));
            if (++i == n) break;
            else builder.append(',');
        }
        return builder.append(']').toString();
    }

    private static String objectToString(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof CharSequence)
            return ((CharSequence) obj).toString();
        if (obj instanceof Supplier<?>)
            obj = ((Supplier<?>) obj).get();
        Class<?> type = obj.getClass();
        if (type.isArray()) {
            return typeErasedArrayToString(obj);
        }
        return Objects.toString(obj);
    }

    private static String formatObjectToString(Object obj, Object... args) {
        return String.format(objectToString(obj), args);
    }

}
