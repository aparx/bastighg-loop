package io.github.aparx.challenges.looping;

import lombok.Getter;
import lombok.Setter;

/**
 * @author aparx (Vinzent Zeband)
 * @version 06:23 CET, 01.08.2022
 * @since 1.0
 */
public final class PluginMagics {

    @Getter @Setter
    private boolean isDebugMode;

    @Getter @Setter
    private State state;

    public boolean isState(State test) {
        return getState() == test;
    }

    public enum State {
        PRE_LOAD,
        POST_LOAD
    }

}