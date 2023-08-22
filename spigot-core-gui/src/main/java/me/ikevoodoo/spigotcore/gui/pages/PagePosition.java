package me.ikevoodoo.spigotcore.gui.pages;

public record PagePosition(int x, int y) {

    public static PagePosition topLeft() {
        return new PagePosition(0, 0);
    }

    public static PagePosition bottomLeft() {
        return new PagePosition(0, Integer.MAX_VALUE);
    }

    public static PagePosition topRight() {
        return new PagePosition(Integer.MAX_VALUE, 0);
    }

    public static PagePosition bottomRight() {
        return new PagePosition(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }



}
