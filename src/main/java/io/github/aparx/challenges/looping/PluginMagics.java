package io.github.aparx.challenges.looping;

import io.github.aparx.challenges.looping.logger.DebugLogger;
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
    private PluginState state;

    @Getter @Setter
    private DebugLogger debugLogger;

    volatile private GameState gameState;

    public boolean isState(PluginState test) {
        return getState() == test;
    }

    public synchronized void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public synchronized GameState getGameState() {
        return gameState;
    }

    public boolean isGameState(GameState state) {
        return getGameState() == state;
    }

    public enum GameState {
        STARTED,
        STOPPED,
        PAUSED
    }

    public enum PluginState {
        PRE_LOAD,
        POST_LOAD
    }

}
