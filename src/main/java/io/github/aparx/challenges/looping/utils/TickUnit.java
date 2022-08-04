package io.github.aparx.challenges.looping.utils;

/**
 * @author aparx (Vinzent Zeband)
 * @version 17:13 CET, 04.08.2022
 * @since 1.0
 */
public enum TickUnit {

    TICK(20, 1),
    SECOND(60, 20 * TICK.getTimeSecondFactor()),
    MINUTE(60, 60 * SECOND.getTimeSecondFactor()),
    HOUR(24, 60 * MINUTE.getTimeSecondFactor()),
    DAY(7, 24 * HOUR.getTimeSecondFactor()),
    WEEK(4, 7 * DAY.getTimeSecondFactor()),
    MONTH(12, 4 * WEEK.getTimeSecondFactor()),
    YEAR(10, 12 * MONTH.getTimeSecondFactor()),
    CENTURY(100, 10 * YEAR.getTimeSecondFactor()),
    MILLENNIUM(1000, 10 * CENTURY.getTimeSecondFactor());

    private final long moduloDivisor, timeSecondFactor;

    TickUnit(long moduloDivisor, long timeSecondFactor) {
        this.moduloDivisor = moduloDivisor;
        this.timeSecondFactor = timeSecondFactor;
    }

    public long convert(long time, TickUnit newTime) {
        return convert(time, newTime, false);
    }

    public long convert(long time, TickUnit newTime, boolean clocked) {
        if (newTime == null) newTime = this;
        final long value;
        if (newTime.ordinal() > ordinal()) {
            value = time / newTime.timeSecondFactor;
        } else if (newTime.ordinal() < ordinal()) {
            value = time * newTime.timeSecondFactor;
        } else value = time;
        // Modulo the final value if wanted
        return clocked ? value % newTime.moduloDivisor : value;
    }

    public long getModuloDivisor() {
        return moduloDivisor;
    }

    public long getTimeSecondFactor() {
        return timeSecondFactor;
    }

}