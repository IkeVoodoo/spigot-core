package me.ikevoodoo.spigotcore.teams;

import me.ikevoodoo.spigotcore.teams.members.TeamMember;
import me.ikevoodoo.spigotcore.teams.result.TeamAddPlayerResult;
import me.ikevoodoo.spigotcore.teams.result.TeamRemovePlayerResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class Team {

    private TeamMember owner;
    private final Map<UUID, TeamMember> members = new HashMap<>();
    private final Map<UUID, TeamMember> unmodifiableMembers = Collections.unmodifiableMap(this.members);
    private final TeamType teamType;

    protected Team(@NotNull TeamType teamType, @NotNull UUID owner) {
        this.teamType = teamType;
        Objects.requireNonNull(owner, "Cannot set owner to null!");

        this.setOwner(owner);
    }

    public final TeamType getTeamType() {
        return this.teamType;
    }

    public boolean isOwner(UUID other) {
        return this.owner.getUuid().equals(other);
    }

    @NotNull
    public TeamMember getOwner() {
        return this.owner;
    }

    public void transferParty(@NotNull UUID newOwner) {
        Objects.requireNonNull(newOwner, "Cannot set newOwner to null!");

        this.setOwner(newOwner);
    }

    public abstract void disband();

    public int getTeamSize() {
        return this.members.size();
    }

    public abstract int getMaxSize();

    public TeamAddPlayerResult addPlayer(UUID adding, UUID toAdd) {
        if (adding == toAdd) {
            return TeamAddPlayerResult.CANNOT_ADD_SELF;
        }

        var member = this.getMember(adding);
        if (member == null) {
            return TeamAddPlayerResult.SELF_NOT_IN_TEAM;
        }

        if (!getTeamType().isAdmin(member.getPermissionLevel())) {
            return TeamAddPlayerResult.INSUFFICIENT_PERMISSIONS;
        }

        if (this.hasMember(toAdd)) {
            return TeamAddPlayerResult.ALREADY_IN_SAME_TEAM;
        }

        if (this.members.size() + 1 > this.getMaxSize()) {
            return TeamAddPlayerResult.PLAYER_LIMIT_REACHED;
        }

        this.members.put(toAdd, new TeamMember(toAdd, this.getTeamType().getOwnerPermissionLevel() - 1, false, getTeamType().getDefaultPermissionLevel()));
        return TeamAddPlayerResult.SUCCESS;
    }
    
    public TeamRemovePlayerResult removePlayer(UUID removing, UUID toRemove) {
        if (toRemove == getOwner().getUuid()) {
            return TeamRemovePlayerResult.OTHER_IS_OWNER;
        }

        var member = this.getMember(removing);
        if (member == null) {
            return TeamRemovePlayerResult.SELF_NOT_IN_TEAM;
        }

        var toRemoveMember = this.getMember(toRemove);
        if (toRemoveMember == null) {
            return TeamRemovePlayerResult.OTHER_NOT_IN_TEAM;
        }

        if (!getTeamType().isAdmin(member.getPermissionLevel())) {
            return TeamRemovePlayerResult.INSUFFICIENT_PERMISSIONS;
        }

        if (toRemoveMember.getPermissionLevel() > member.getPermissionLevel()) {
            return TeamRemovePlayerResult.OTHER_HAS_HIGHER_PERMISSIONS;
        }

        this.members.remove(toRemove);
        return TeamRemovePlayerResult.SUCCESS;
    }

    /**
     * @implNote The following method will always return true if <code>id == {@link #getOwner()}</code>
     * */
    public boolean hasMember(UUID id) {
        return this.isOwner(id) || this.members.containsKey(id);
    }

    public TeamMember getMember(UUID id) {
        if (this.isOwner(id)) return this.owner;

        return this.members.get(id);
    }

    public void setMemberPermissionLevel(UUID id, int permissionLevel) {
        var member = this.getMember(id);
        if (member == null) return;

        member.setPermissionLevel(permissionLevel);
    }

    public Map<UUID, TeamMember> getMembers() {
        return this.unmodifiableMembers;
    }

    public List<List<TeamMember>> getMembersSorted() {
        var memberLists = new ArrayList<List<TeamMember>>();
        for (int i = 0; i < this.getTeamType().getOwnerPermissionLevel() + 1; i++) {
            memberLists.add(new ArrayList<>());
        }

        for (var member : this.members.values()) {
            memberLists.get(member.getPermissionLevel()).add(member);
        }

        memberLists.get(this.owner.getPermissionLevel()).add(this.owner);

        return memberLists;
    }

    private void setOwner(UUID owner) {
        this.owner = new TeamMember(owner, this.getTeamType().getOwnerPermissionLevel() - 1, true, this.getTeamType().getOwnerPermissionLevel());
    }

}
