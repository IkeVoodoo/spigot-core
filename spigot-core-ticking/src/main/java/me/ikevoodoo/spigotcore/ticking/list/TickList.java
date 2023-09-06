package me.ikevoodoo.spigotcore.ticking.list;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public final class TickList<T> {

    private final List<T> elements = new LinkedList<>();
    private final Consumer<T> ticker;
    private int tickInterval;

    public TickList(Consumer<T> ticker, int tickInterval) {
        this.ticker = ticker;
        this.tickInterval = tickInterval;
    }

    public int getTickInterval() {
        return this.tickInterval;
    }

    public void setTickInterval(int tickInterval) {
        this.tickInterval = tickInterval;
    }

    public boolean shouldTick(int currentTick) {
        return currentTick % this.getTickInterval() == 0;
    }

    public void tick() {
        for (var element : this.elements) {
            this.ticker.accept(element);
        }
    }

    public void addElement(T element) {
        this.elements.add(element);
    }

    public void removeElement(T element) {
        this.elements.remove(element);
    }

}
