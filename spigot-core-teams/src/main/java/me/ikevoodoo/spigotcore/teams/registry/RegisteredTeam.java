package me.ikevoodoo.spigotcore.teams.registry;

import me.ikevoodoo.spigotcore.teams.Team;
import me.ikevoodoo.spigotcore.teams.members.TeamMember;
import me.ikevoodoo.spigotcore.teams.result.TeamAddPlayerResult;
import me.ikevoodoo.spigotcore.teams.result.TeamRemovePlayerResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RegisteredTeam extends Team {

    private final TeamRegistry registry;
    private final Team team;

    protected RegisteredTeam(TeamRegistry registry, Team team) {
        super(registry, team.getTeamType(), team.getOwner().getUuid());
        this.registry = registry;
        this.team = team;
    }

    @Override
    public boolean isOwner(UUID other) {
        return this.team.isOwner(other);
    }

    @Override
    public int getMaxSize() {
        return this.team.getMaxSize();
    }

    @Override
    public @NotNull TeamMember getOwner() {
        return this.team.getOwner();
    }

    @Override
    public void transferParty(@NotNull UUID newOwner) {
        var previous = this.team.getOwner().getUuid();
        this.team.transferParty(newOwner);
        this.registry.transferOwner(previous, newOwner); // Update the registry with the relevant information.
    }

    @Override
    public void disband() {
        this.team.disband();
        this.registry.unregisterTeam(this);
    }

    @Override
    public int getTeamSize() {
        return this.team.getTeamSize();
    }

    @Override
    public TeamAddPlayerResult addPlayer(UUID adding, UUID toAdd) {
        var otherTeam = this.registry.getTeam(toAdd);
        if (otherTeam != null && otherTeam != this) {
            return TeamAddPlayerResult.ALREADY_IN_OTHER_TEAM;
        }

        var result = this.team.addPlayer(adding, toAdd);
        if (result == TeamAddPlayerResult.SUCCESS) {
            this.registry.addMember(toAdd, this);
        }

        return result;
    }

    @Override
    public TeamRemovePlayerResult removePlayer(UUID removing, UUID toRemove) {
        var result = this.team.removePlayer(removing, toRemove);
        if (result == TeamRemovePlayerResult.SUCCESS) {
            this.registry.removeMember(toRemove);
        }

        return result;
    }

    @Override
    public boolean hasMember(UUID id) {
        return this.team.hasMember(id);
    }

    @Override
    public TeamMember getMember(UUID id) {
        return this.team.getMember(id);
    }

    @Override
    public void setMemberPermissionLevel(UUID id, int permissionLevel) {
        this.team.setMemberPermissionLevel(id, permissionLevel);
    }

    @Override
    public Map<UUID, TeamMember> getMembers() {
        return this.team.getMembers();
    }

    @Override
    public List<List<TeamMember>> getMembersSorted() {
        return this.team.getMembersSorted();
    }
}
