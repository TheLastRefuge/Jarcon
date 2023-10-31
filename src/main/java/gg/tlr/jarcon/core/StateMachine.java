package gg.tlr.jarcon.core;

import java.util.function.BiConsumer;

public class StateMachine<T extends Enum<?>> {
    protected final BiConsumer<T, T>[] transitions;
    protected final int             size;
    protected       T               state;

    public StateMachine(Class<T> clazz, T initial) {
        this.size = clazz.getEnumConstants().length;
        this.transitions = new BiConsumer[size * size];
        this.state = initial;
    }

    public synchronized T get() {
        return state;
    }

    public synchronized boolean set(T target) {
        var transition = transitions[state.ordinal() * size + target.ordinal()];

        if (transition != null) {

            transition.accept(state, target);
            state = target;

            return true;
        }

        return false;
    }

    public synchronized void add(T a, T b, BiConsumer<T, T> transition) {
        transitions[a.ordinal() * size + b.ordinal()] = transition;
    }

    public synchronized void add(T a, T b, Runnable transition) {
        add(a, b, (x, y) -> transition.run());
    }

    public synchronized void add(T a, T b) {
        add(a, b, () -> {});
    }

}
