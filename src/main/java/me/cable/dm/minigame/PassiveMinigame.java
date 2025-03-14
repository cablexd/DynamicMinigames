package me.cable.dm.minigame;

/*
    A minigame which does not change states because it is always running.
 */
public abstract class PassiveMinigame extends Minigame {

    /*
        Start the passive minigame.
     */
    public void start() {
        // empty
    }

    /*
        Stop the passive minigame.
     */
    public void stop() {
        // empty
    }

    public void tick() {
        // empty
    }
}
