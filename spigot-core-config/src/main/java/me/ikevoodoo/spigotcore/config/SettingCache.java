package me.ikevoodoo.spigotcore.config;

import dev.dejvokep.boostedyaml.route.Route;

final class SettingCache {

    private final Route route;
    private Configuration config;
    private Object proxy;


    public SettingCache(Route route, Configuration config, Object proxy) {
        this.route = route;
        this.config = config;
        this.proxy = proxy;
    }

    public Route getRoute() {
        return route;
    }

    public Configuration getConfig() {
        return config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    public Object getProxy() {
        return proxy;
    }

    public void setProxy(Object proxy) {
        this.proxy = proxy;
    }
}
