package me.ikevoodoo.spigotcore.ticking;

import me.ikevoodoo.spigotcore.ticking.list.TickList;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class Ticker<T> {

    private final Map<Integer, TickList<T>> tickLists = new HashMap<>();
    private final Consumer<T> elementTicker;
    private int currentTick;
    private int scheduledId = -1;

    public Ticker(Consumer<T> elementTicker) {
        this.elementTicker = elementTicker;
    }

    public void addElement(int ticks, T element) {
        this.tickLists.computeIfAbsent(ticks, t -> new TickList<>(this.elementTicker, t)).addElement(element);
    }

    public void removeElement(int ticks, T element) {
        var tickList = this.tickLists.get(ticks);
        if (tickList == null) return;

        tickList.removeElement(element);
    }

    public void changeTickInterval(int ticks, int newTicks) {
        var tickList = this.tickLists.remove(ticks);
        if (tickList == null) return;

        tickList.setTickInterval(newTicks);

        this.tickLists.put(newTicks, tickList);
    }

    public int getCurrentTick() {
        return this.currentTick;
    }

    public void tick() {
        this.currentTick++;

        for (var value : this.tickLists.values()) {
            if (value.shouldTick(this.currentTick)) {
                value.tick();
            }
        }
    }

    public void scheduleTick(Plugin plugin) {
        if (this.scheduledId != -1) this.cancelScheduledTick();

        this.scheduledId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 1, 1);
    }

    public void cancelScheduledTick() {
        if (this.scheduledId == -1) return;

        Bukkit.getScheduler().cancelTask(this.scheduledId);
        this.scheduledId = -1;
    }

}
