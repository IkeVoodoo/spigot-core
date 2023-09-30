package me.ikevoodoo.spigotcore.teams.registry;

import me.ikevoodoo.spigotcore.teams.Team;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TeamRegistry {

    private final Map<UUID, Team> ownerToTeam = new HashMap<>();
    private final Map<UUID, Team> memberToTeam = new HashMap<>();

    public void registerTeam(Team team) {
        if (team instanceof RegisteredTeam) return;

        if (this.ownerToTeam.containsKey(team.getOwner().getUuid())) {
            throw new IllegalStateException("A player cannot own two teams in the same registry!");
        }

        var registeredTeam = new RegisteredTeam(this, team);
        this.ownerToTeam.put(team.getOwner().getUuid(), registeredTeam);

        for (var member : registeredTeam.getMembers().keySet()) {
            this.memberToTeam.put(member, registeredTeam);
        }
    }

    public boolean unregisterTeam(Team team) {
        var removed = this.ownerToTeam.remove(team.getOwner().getUuid());
        if (removed == null) return false;

        // We're forced to use removeAll with a collection as otherwise this will only remove the first element with
        // the matching value.
        this.memberToTeam.values().removeAll(Collections.singleton(removed));
        return true;
    }

    @Nullable
    public Team getTeam(UUID user) {
        var team = this.ownerToTeam.get(user);
        if (team != null) return team;

        return this.memberToTeam.get(user);
    }

    @NotNull
    public List<Team> getTeams() {
        return this.ownerToTeam.values().stream().toList();
    }

    @Nullable
    public Team getTeam(Player user) {
        return this.getTeam(user.getUniqueId());
    }

    @ApiStatus.Internal
    public void transferOwner(UUID from, UUID to) {
        this.ownerToTeam.put(to, this.ownerToTeam.remove(from));
    }

    @ApiStatus.Internal
    public void addMember(UUID member, RegisteredTeam team) {
        this.memberToTeam.put(member, team);
    }

    @ApiStatus.Internal
    public void removeMember(UUID member) {
        this.memberToTeam.remove(member);
    }

}
