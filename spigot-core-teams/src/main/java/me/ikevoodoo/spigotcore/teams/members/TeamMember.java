package me.ikevoodoo.spigotcore.teams.members;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

public class TeamMember {

    private final UUID uuid;
    private final int maxPermissionLevel;
    private final boolean lockPermissionLevel;
    private int permissionLevel;

    @ApiStatus.Internal
    public TeamMember(UUID uuid, int maxPermissionLevel, boolean lockPermissionLevel, int permissionLevel) {
        this.uuid = uuid;
        this.maxPermissionLevel = maxPermissionLevel;
        this.lockPermissionLevel = lockPermissionLevel;
        this.permissionLevel = permissionLevel;
    }


    public int getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(int permissionLevel) {
        if (this.lockPermissionLevel) return;

        this.permissionLevel = Math.max(Math.min(permissionLevel, this.maxPermissionLevel), 0);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void sendMessage(String message) {
        var player = Bukkit.getPlayer(this.uuid);
        if (player == null) return;

        player.sendMessage(message);
    }

    @Override
    public String toString() {
        return "TeamMember[" +
                "uuid=" + uuid +
                ", maxPermissionLevel=" + maxPermissionLevel +
                ", lockPermissionLevel=" + lockPermissionLevel +
                ", permissionLevel=" + permissionLevel +
                ']';
    }
}
