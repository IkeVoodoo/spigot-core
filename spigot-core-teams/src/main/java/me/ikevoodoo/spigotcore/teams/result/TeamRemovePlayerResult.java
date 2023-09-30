package me.ikevoodoo.spigotcore.teams.result;

public enum TeamRemovePlayerResult {

    SELF_NOT_IN_TEAM,
    OTHER_NOT_IN_TEAM,
    INSUFFICIENT_PERMISSIONS,

    /**
     * Whenever a player has permission, and they're trying to remove someone with a higher role than them.
     * */
    OTHER_HAS_HIGHER_PERMISSIONS,

    /**
     * Whenever a team tried to remove its owner.
     * */
    OTHER_IS_OWNER,

    SUCCESS

}
