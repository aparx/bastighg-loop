package io.github.aparx.challenges.looping.functional;

/**
 * @author aparx (Vinzent Zeband)
 * @version 08:47 CET, 01.08.2022
 * @since 1.0
 */
public class StatePauseable implements IStatePauseable {

    volatile private boolean paused;

    @Override
    public synchronized final boolean isPaused() {
        return paused;
    }

    @Override
    public synchronized final void setPaused(boolean paused) {
        if (paused == this.paused) return;
        this.paused = paused;   // assign first for override
        if (paused) onPause(); else onResume();
    }

    protected void onResume() {
        // Called when the module is resumed
    }

    protected void onPause() {
        // Called when the module is paused
    }

}
