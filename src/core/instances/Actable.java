package core.instances;

public interface Actable {
    /**
     * Performs an action using variable delta (in seconds) that has elapsed.
     * Actions should use delta linearly for best results.
     * @param delta 
     */
    void action(float delta);
}
