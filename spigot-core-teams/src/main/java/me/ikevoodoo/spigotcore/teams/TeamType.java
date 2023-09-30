package me.ikevoodoo.spigotcore.teams;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class TeamType {

    @NotNull
    public abstract String getId();

    public abstract int getOwnerPermissionLevel();
    public abstract int getDefaultPermissionLevel();

    public abstract boolean canAlly(TeamType otherType);

    /**
     * An admin can add, remove, promote and demote players.
     * <p>
     * Promoting and demoting is limited to one-below current permission level.
     * */
    public abstract boolean isAdmin(int permissionLevel);

    public abstract String getPermissionLevelName(int permissionLevel);

    public String getPlayerName(UUID id, int permissionLevel) {
        if (Bukkit.getPlayer(id) == null) {
            return getOfflinePlayerName(id, permissionLevel);
        }

        return getOnlinePlayerName(id, permissionLevel);
    }

    public abstract String getOnlinePlayerName(UUID id, int permissionLevel);
    public abstract String getOfflinePlayerName(UUID id, int permissionLevel);

    protected final String getUUIDName(UUID id) {
        var offline = Bukkit.getOfflinePlayer(id);
        var online = offline.getPlayer();
        if (online != null) return online.getDisplayName();

        return offline.getName();
    }

}
