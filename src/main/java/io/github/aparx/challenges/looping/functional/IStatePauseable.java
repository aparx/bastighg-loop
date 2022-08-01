package io.github.aparx.challenges.looping.functional;

/**
 * @author aparx (Vinzent Zeband)
 * @version 08:51 CET, 01.08.2022
 * @since 1.0
 */
public interface IStatePauseable {

    boolean isPaused();

    void setPaused(boolean paused);

}
