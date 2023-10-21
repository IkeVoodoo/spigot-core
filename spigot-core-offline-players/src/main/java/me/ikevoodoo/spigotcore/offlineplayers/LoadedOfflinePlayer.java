package me.ikevoodoo.spigotcore.offlineplayers;

import dev.refinedtech.spigotcore.nbt.NBTTagContainer;
import dev.refinedtech.spigotcore.nbt.nbt.NBTReader;
import dev.refinedtech.spigotcore.nbt.nbt.NBTWriter;
import dev.refinedtech.spigotcore.nbt.nbt.tags.ByteTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.DoubleTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.FloatTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.IntTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.LongTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.StringTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.list.CompoudTag;
import dev.refinedtech.spigotcore.nbt.nbt.tags.list.ListTag;
import me.ikevoodoo.spigotcore.offlineplayers.attributes.AttributeDefaults;
import me.ikevoodoo.spigotcore.offlineplayers.attributes.OfflineAttributeInstance;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class LoadedOfflinePlayer implements Player {

    private static final AttributeModifier.Operation[] ATTRIBUTE_OPERATIONS = AttributeModifier.Operation.values();

    private final UUID uuid;
    private final NBTTagContainer container;
    private final PersistentDataContainer persistentDataContainer;
    private final UUID worldId;
    private final Vector position;
    private final Map<Attribute, OfflineAttributeInstance> attributeInstances = new EnumMap<>(Attribute.class);
    private float yaw;
    private float pitch;
    private Vector velocity;
    private boolean onGround;

    private LoadedOfflinePlayer(NBTTagContainer container) {
        this.uuid = container.readUUID("UUID");

        this.container = container;

        this.persistentDataContainer = container.asBukkitPersistentDataContainer();

        this.worldId = new UUID(
                container.<LongTag>getTag("WorldUUIDMost").getValue(),
                container.<LongTag>getTag("WorldUUIDLeast").getValue()
        );

        this.position = container.readDoubleVector("Pos");
        this.velocity = container.readDoubleVector("Motion");

        var rot = container.<ListTag<FloatTag>>getTag("Rotation").getValue();
        this.yaw = rot.get(0).getValue();
        this.pitch = rot.get(1).getValue();

        this.onGround = container.<ByteTag>getTag("OnGround").getValue() == 1;

        this.loadAttributes();
    }

    @Nullable
    public static LoadedOfflinePlayer loadFrom(OfflinePlayer offlinePlayer) throws IOException {
        var file = getUUIDFile(offlinePlayer.getUniqueId());

        if (file == null) return null;

        return new LoadedOfflinePlayer(NBTReader.readNbt(file));
    }

    public boolean save() throws IOException {
        var file = getUUIDFile(this.uuid);

        if (file == null) return false;

        this.container.writeDoubleVector("Pos", this.position);
        this.container.writeDoubleVector("Motion", this.velocity);

        var rot = this.container.<ListTag<FloatTag>>getTag("Rotation").getValue();
        rot.set(0, new FloatTag(null, this.yaw));
        rot.set(1, new FloatTag(null, this.pitch));

        this.writeAttributes();

        NBTWriter.writeNbt(file, this.container);
        return true;
    }

    //region Implemented Methods
    @NotNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public void setDisplayName(@Nullable String name) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public String getPlayerListName() {
        return null;
    }

    @Override
    public void setPlayerListName(@Nullable String name) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public String getPlayerListHeader() {
        return null;
    }

    @Nullable
    @Override
    public String getPlayerListFooter() {
        return null;
    }

    @Override
    public void setPlayerListHeader(@Nullable String header) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPlayerListFooter(@Nullable String footer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPlayerListHeaderFooter(@Nullable String header, @Nullable String footer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCompassTarget(@NotNull Location loc) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Location getCompassTarget() {
        return null;
    }

    @Nullable
    @Override
    public InetSocketAddress getAddress() {
        return null;
    }

    @Override
    public boolean isConversing() {
        return false;
    }

    @Override
    public void acceptConversationInput(@NotNull String input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean beginConversation(@NotNull Conversation conversation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation, @NotNull ConversationAbandonedEvent details) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendRawMessage(@NotNull String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendRawMessage(@Nullable UUID sender, @NotNull String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void kickPlayer(@Nullable String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void chat(@NotNull String msg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean performCommand(@NotNull String command) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Location getLocation() {
        return this.position.toLocation(this.getWorld());
    }

    @Nullable
    @Override
    public Location getLocation(@Nullable Location loc) {
        if (loc == null) return null;

        loc.subtract(loc);

        var ownLoc = this.getLocation();
        loc.add(ownLoc);

        loc.setYaw(ownLoc.getYaw());
        loc.setPitch(ownLoc.getPitch());

        loc.setDirection(ownLoc.getDirection());

        return loc;
    }

    @Override
    public void setVelocity(@NotNull Vector velocity) {
        Objects.requireNonNull(velocity, "Velocity cannot be null!");

        this.velocity = velocity;
    }

    @NotNull
    @Override
    public Vector getVelocity() {
        return this.velocity;
    }

    @Override
    public double getHeight() {
        return 0;
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @NotNull
    @Override
    public BoundingBox getBoundingBox() {
        return null;
    }

    @Override
    public boolean isOnGround() {
        return this.onGround;
    }

    @Override
    public boolean isInWater() {
        return false;
    }

    @NotNull
    @Override
    public World getWorld() {
        var world = Bukkit.getWorld(this.worldId);
        if (world == null) {
            throw new IllegalStateException("Player is in no world!");
        }

        return world;
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public boolean teleport(@NotNull Location location) {
        return false;
    }

    @Override
    public boolean teleport(@NotNull Location location, @NotNull PlayerTeleportEvent.TeleportCause cause) {
        return false;
    }

    @Override
    public boolean teleport(@NotNull Entity destination) {
        return false;
    }

    @Override
    public boolean teleport(@NotNull Entity destination, @NotNull PlayerTeleportEvent.TeleportCause cause) {
        return false;
    }

    @NotNull
    @Override
    public List<Entity> getNearbyEntities(double x, double y, double z) {
        var location = this.getLocation();
        var collection = location.getWorld().getNearbyEntities(location, x, y, z);

        if (collection instanceof List<Entity> list) {
            return list;
        }

        return new ArrayList<>(collection);
    }

    @Override
    public int getEntityId() {
        return 0;
    }

    @Override
    public int getFireTicks() {
        return 0;
    }

    @Override
    public int getMaxFireTicks() {
        return 0;
    }

    @Override
    public void setFireTicks(int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setVisualFire(boolean fire) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isVisualFire() {
        return false;
    }

    @Override
    public int getFreezeTicks() {
        return 0;
    }

    @Override
    public int getMaxFreezeTicks() {
        return 0;
    }

    @Override
    public void setFreezeTicks(int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDead() {
        return false;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void sendMessage(@NotNull String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendMessage(@NotNull String... messages) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String... messages) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Server getServer() {
        return null;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public void setPersistent(boolean persistent) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Entity getPassenger() {
        return null;
    }

    @Override
    public boolean setPassenger(@NotNull Entity passenger) {
        return false;
    }

    @NotNull
    @Override
    public List<Entity> getPassengers() {
        return null;
    }

    @Override
    public boolean addPassenger(@NotNull Entity passenger) {
        return false;
    }

    @Override
    public boolean removePassenger(@NotNull Entity passenger) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean eject() {
        return false;
    }

    @Override
    public float getFallDistance() {
        return 0;
    }

    @Override
    public void setFallDistance(float distance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLastDamageCause(@Nullable EntityDamageEvent event) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public EntityDamageEvent getLastDamageCause() {
        return null;
    }

    @NotNull
    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public int getTicksLived() {
        return 0;
    }

    @Override
    public void setTicksLived(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void playEffect(@NotNull EntityEffect type) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public EntityType getType() {
        return null;
    }

    @Override
    public boolean isInsideVehicle() {
        return false;
    }

    @Override
    public boolean leaveVehicle() {
        return false;
    }

    @Nullable
    @Override
    public Entity getVehicle() {
        return null;
    }

    @Override
    public void setCustomNameVisible(boolean flag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public void setGlowing(boolean flag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isGlowing() {
        return false;
    }

    @Override
    public void setInvulnerable(boolean flag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInvulnerable() {
        return false;
    }

    @Override
    public boolean isSilent() {
        return false;
    }

    @Override
    public void setSilent(boolean flag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasGravity() {
        return false;
    }

    @Override
    public void setGravity(boolean gravity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPortalCooldown() {
        return 0;
    }

    @Override
    public void setPortalCooldown(int cooldown) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Set<String> getScoreboardTags() {
        return null;
    }

    @Override
    public boolean addScoreboardTag(@NotNull String tag) {
        return false;
    }

    @Override
    public boolean removeScoreboardTag(@NotNull String tag) {
        return false;
    }

    @NotNull
    @Override
    public PistonMoveReaction getPistonMoveReaction() {
        return null;
    }

    @NotNull
    @Override
    public BlockFace getFacing() {
        return null;
    }

    @NotNull
    @Override
    public Pose getPose() {
        return null;
    }

    @Override
    public boolean isSneaking() {
        return false;
    }

    @Override
    public void setSneaking(boolean sneak) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSprinting() {
        return false;
    }

    @Override
    public void setSprinting(boolean sprinting) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSleepingIgnored(boolean isSleeping) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSleepingIgnored() {
        return false;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public boolean isBanned() {
        return false;
    }

    @Override
    public boolean isWhitelisted() {
        return false;
    }

    @Override
    public void setWhitelisted(boolean value) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Player getPlayer() {
        return this;
    }

    @Override
    public long getFirstPlayed() {
        return 0;
    }

    @Override
    public long getLastPlayed() {
        return 0;
    }

    @Override
    public boolean hasPlayedBefore() {
        return false;
    }

    @Nullable
    @Override
    public Location getBedSpawnLocation() {
        return null;
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, int amount) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, int amount) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, int newValue) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int amount) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int amount) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, @NotNull Material material, int newValue) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int amount) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBedSpawnLocation(@Nullable Location location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBedSpawnLocation(@Nullable Location location, boolean force) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void playNote(@NotNull Location loc, byte instrument, byte note) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void playNote(@NotNull Location loc, @NotNull Instrument instrument, @NotNull Note note) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void playSound(@NotNull Location location, @NotNull Sound sound, float volume, float pitch) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void playSound(@NotNull Location location, @NotNull String sound, float volume, float pitch) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void playSound(@NotNull Location location, @NotNull Sound sound, @NotNull SoundCategory category, float volume, float pitch) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void playSound(@NotNull Location location, @NotNull String sound, @NotNull SoundCategory category, float volume, float pitch) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stopSound(@NotNull Sound sound) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stopSound(@NotNull String sound) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stopSound(@NotNull Sound sound, @Nullable SoundCategory category) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stopSound(@NotNull String sound, @Nullable SoundCategory category) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stopAllSounds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void playEffect(@NotNull Location loc, @NotNull Effect effect, int data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void playEffect(@NotNull Location loc, @NotNull Effect effect, @Nullable T data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean breakBlock(@NotNull Block block) {
        return false;
    }

    @Override
    public void sendBlockChange(@NotNull Location loc, @NotNull Material material, byte data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendBlockChange(@NotNull Location loc, @NotNull BlockData block) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendBlockDamage(@NotNull Location loc, float progress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean sendChunkChange(@NotNull Location loc, int sx, int sy, int sz, @NotNull byte[] data) {
        return false;
    }

    @Override
    public void sendSignChange(@NotNull Location loc, @Nullable String[] lines) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendSignChange(@NotNull Location loc, @Nullable String[] lines, @NotNull DyeColor dyeColor) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendSignChange(@NotNull Location loc, @Nullable String[] lines, @NotNull DyeColor dyeColor, boolean hasGlowingText) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendMap(@NotNull MapView map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInventory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPlayerTime(long time, boolean relative) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getPlayerTime() {
        return 0;
    }

    @Override
    public long getPlayerTimeOffset() {
        return 0;
    }

    @Override
    public boolean isPlayerTimeRelative() {
        return false;
    }

    @Override
    public void resetPlayerTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPlayerWeather(@NotNull WeatherType type) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public WeatherType getPlayerWeather() {
        return null;
    }

    @Override
    public void resetPlayerWeather() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void giveExp(int amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void giveExpLevels(int amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getExp() {
        return 0;
    }

    @Override
    public void setExp(float exp) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public void setLevel(int level) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTotalExperience() {
        return 0;
    }

    @Override
    public void setTotalExperience(int exp) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendExperienceChange(float progress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendExperienceChange(float progress, int level) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getAllowFlight() {
        return false;
    }

    @Override
    public void setAllowFlight(boolean flight) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void hidePlayer(@NotNull Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void hidePlayer(@NotNull Plugin plugin, @NotNull Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showPlayer(@NotNull Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showPlayer(@NotNull Plugin plugin, @NotNull Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canSee(@NotNull Player player) {
        return false;
    }

    @Override
    public boolean isFlying() {
        return false;
    }

    @Override
    public void setFlying(boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFlySpeed(float value) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWalkSpeed(float value) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFlySpeed() {
        return 0;
    }

    @Override
    public float getWalkSpeed() {
        return 0;
    }

    @Override
    public void setTexturePack(@NotNull String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setResourcePack(@NotNull String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setResourcePack(@NotNull String url, @NotNull byte[] hash) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Scoreboard getScoreboard() {
        return null;
    }

    @Override
    public void setScoreboard(@NotNull Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHealthScaled() {
        return false;
    }

    @Override
    public void setHealthScaled(boolean scale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHealthScale(double scale) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getHealthScale() {
        return 0;
    }

    @Nullable
    @Override
    public Entity getSpectatorTarget() {
        return null;
    }

    @Override
    public void setSpectatorTarget(@Nullable Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetTitle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, @Nullable T data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, @Nullable T data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, @Nullable T data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, @Nullable T data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public AdvancementProgress getAdvancementProgress(@NotNull Advancement advancement) {
        return null;
    }

    @Override
    public int getClientViewDistance() {
        return 0;
    }

    @Override
    public int getPing() {
        return 0;
    }

    @NotNull
    @Override
    public String getLocale() {
        return null;
    }

    @Override
    public void updateCommands() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void openBook(@NotNull ItemStack book) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Spigot spigot() {
        return null;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    @NotNull
    @Override
    public String getName() {
        return null;
    }

    @NotNull
    @Override
    public PlayerInventory getInventory() {
        return null;
    }

    @NotNull
    @Override
    public Inventory getEnderChest() {
        return null;
    }

    @NotNull
    @Override
    public MainHand getMainHand() {
        return null;
    }

    @Override
    public boolean setWindowProperty(@NotNull InventoryView.Property prop, int value) {
        return false;
    }

    @NotNull
    @Override
    public InventoryView getOpenInventory() {
        return null;
    }

    @Nullable
    @Override
    public InventoryView openInventory(@NotNull Inventory inventory) {
        return null;
    }

    @Nullable
    @Override
    public InventoryView openWorkbench(@Nullable Location location, boolean force) {
        return null;
    }

    @Nullable
    @Override
    public InventoryView openEnchanting(@Nullable Location location, boolean force) {
        return null;
    }

    @Override
    public void openInventory(@NotNull InventoryView inventory) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public InventoryView openMerchant(@NotNull Villager trader, boolean force) {
        return null;
    }

    @Nullable
    @Override
    public InventoryView openMerchant(@NotNull Merchant merchant, boolean force) {
        return null;
    }

    @Override
    public void closeInventory() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public ItemStack getItemInHand() {
        return null;
    }

    @Override
    public void setItemInHand(@Nullable ItemStack item) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public ItemStack getItemOnCursor() {
        return null;
    }

    @Override
    public void setItemOnCursor(@Nullable ItemStack item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasCooldown(@NotNull Material material) {
        return false;
    }

    @Override
    public int getCooldown(@NotNull Material material) {
        return 0;
    }

    @Override
    public void setCooldown(@NotNull Material material, int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSleepTicks() {
        return 0;
    }

    @Override
    public boolean sleep(@NotNull Location location, boolean force) {
        return false;
    }

    @Override
    public void wakeup(boolean setSpawnLocation) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Location getBedLocation() {
        return null;
    }

    @NotNull
    @Override
    public GameMode getGameMode() {
        return null;
    }

    @Override
    public void setGameMode(@NotNull GameMode mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public boolean isHandRaised() {
        return false;
    }

    @Nullable
    @Override
    public ItemStack getItemInUse() {
        return null;
    }

    @Override
    public int getExpToLevel() {
        return 0;
    }

    @Override
    public float getAttackCooldown() {
        return 0;
    }

    @Override
    public boolean discoverRecipe(@NotNull NamespacedKey recipe) {
        return false;
    }

    @Override
    public int discoverRecipes(@NotNull Collection<NamespacedKey> recipes) {
        return 0;
    }

    @Override
    public boolean undiscoverRecipe(@NotNull NamespacedKey recipe) {
        return false;
    }

    @Override
    public int undiscoverRecipes(@NotNull Collection<NamespacedKey> recipes) {
        return 0;
    }

    @Override
    public boolean hasDiscoveredRecipe(@NotNull NamespacedKey recipe) {
        return false;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getDiscoveredRecipes() {
        return null;
    }

    @Nullable
    @Override
    public Entity getShoulderEntityLeft() {
        return null;
    }

    @Override
    public void setShoulderEntityLeft(@Nullable Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Entity getShoulderEntityRight() {
        return null;
    }

    @Override
    public void setShoulderEntityRight(@Nullable Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean dropItem(boolean dropAll) {
        return false;
    }

    @Override
    public float getExhaustion() {
        return 0;
    }

    @Override
    public void setExhaustion(float value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getSaturation() {
        return 0;
    }

    @Override
    public void setSaturation(float value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFoodLevel() {
        return 0;
    }

    @Override
    public void setFoodLevel(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSaturatedRegenRate() {
        return 0;
    }

    @Override
    public void setSaturatedRegenRate(int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getUnsaturatedRegenRate() {
        return 0;
    }

    @Override
    public void setUnsaturatedRegenRate(int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStarvationRate() {
        return 0;
    }

    @Override
    public void setStarvationRate(int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getEyeHeight() {
        return 0;
    }

    @Override
    public double getEyeHeight(boolean ignorePose) {
        return 0;
    }

    @NotNull
    @Override
    public Location getEyeLocation() {
        return null;
    }

    @NotNull
    @Override
    public List<Block> getLineOfSight(@Nullable Set<Material> transparent, int maxDistance) {
        return null;
    }

    @NotNull
    @Override
    public Block getTargetBlock(@Nullable Set<Material> transparent, int maxDistance) {
        return null;
    }

    @NotNull
    @Override
    public List<Block> getLastTwoTargetBlocks(@Nullable Set<Material> transparent, int maxDistance) {
        return null;
    }

    @Nullable
    @Override
    public Block getTargetBlockExact(int maxDistance) {
        return null;
    }

    @Nullable
    @Override
    public Block getTargetBlockExact(int maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    @Nullable
    @Override
    public RayTraceResult rayTraceBlocks(double maxDistance) {
        return null;
    }

    @Nullable
    @Override
    public RayTraceResult rayTraceBlocks(double maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    @Override
    public int getRemainingAir() {
        return 0;
    }

    @Override
    public void setRemainingAir(int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaximumAir() {
        return 0;
    }

    @Override
    public void setMaximumAir(int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getArrowCooldown() {
        return 0;
    }

    @Override
    public void setArrowCooldown(int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getArrowsInBody() {
        return 0;
    }

    @Override
    public void setArrowsInBody(int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaximumNoDamageTicks() {
        return 0;
    }

    @Override
    public void setMaximumNoDamageTicks(int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getLastDamage() {
        return 0;
    }

    @Override
    public void setLastDamage(double damage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNoDamageTicks() {
        return 0;
    }

    @Override
    public void setNoDamageTicks(int ticks) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Player getKiller() {
        return null;
    }

    @Override
    public boolean addPotionEffect(@NotNull PotionEffect effect) {
        return false;
    }

    @Override
    public boolean addPotionEffect(@NotNull PotionEffect effect, boolean force) {
        return false;
    }

    @Override
    public boolean addPotionEffects(@NotNull Collection<PotionEffect> effects) {
        return false;
    }

    @Override
    public boolean hasPotionEffect(@NotNull PotionEffectType type) {
        return false;
    }

    @Nullable
    @Override
    public PotionEffect getPotionEffect(@NotNull PotionEffectType type) {
        return null;
    }

    @Override
    public void removePotionEffect(@NotNull PotionEffectType type) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        return null;
    }

    @Override
    public boolean hasLineOfSight(@NotNull Entity other) {
        return false;
    }

    @Override
    public boolean getRemoveWhenFarAway() {
        return false;
    }

    @Override
    public void setRemoveWhenFarAway(boolean remove) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public EntityEquipment getEquipment() {
        return null;
    }

    @Override
    public void setCanPickupItems(boolean pickup) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getCanPickupItems() {
        return false;
    }

    @Override
    public boolean isLeashed() {
        return false;
    }

    @NotNull
    @Override
    public Entity getLeashHolder() throws IllegalStateException {
        return null;
    }

    @Override
    public boolean setLeashHolder(@Nullable Entity holder) {
        return false;
    }

    @Override
    public boolean isGliding() {
        return false;
    }

    @Override
    public void setGliding(boolean gliding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSwimming() {
        return false;
    }

    @Override
    public void setSwimming(boolean swimming) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRiptiding() {
        return false;
    }

    @Override
    public boolean isSleeping() {
        return false;
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    @Override
    public void setAI(boolean ai) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasAI() {
        return false;
    }

    @Override
    public void attack(@NotNull Entity target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void swingMainHand() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void swingOffHand() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCollidable(boolean collidable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    @NotNull
    @Override
    public Set<UUID> getCollidableExemptions() {
        return null;
    }

    @Nullable
    @Override
    public <T> T getMemory(@NotNull MemoryKey<T> memoryKey) {
        return null;
    }

    @Override
    public <T> void setMemory(@NotNull MemoryKey<T> memoryKey, @Nullable T memoryValue) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public EntityCategory getCategory() {
        return null;
    }

    @Override
    public void setInvisible(boolean invisible) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    @Nullable
    @Override
    public AttributeInstance getAttribute(@NotNull Attribute attribute) {
        return this.attributeInstances.get(attribute);
    }

    @Override
    public void damage(double amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void damage(double amount, @Nullable Entity source) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getHealth() {
        return 0;
    }

    @Override
    public void setHealth(double health) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getAbsorptionAmount() {
        return 0;
    }

    @Override
    public void setAbsorptionAmount(double amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getMaxHealth() {
        return 0;
    }

    @Override
    public void setMaxHealth(double health) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetMaxHealth() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public String getCustomName() {
        return null;
    }

    @Override
    public void setCustomName(@Nullable String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMetadata(@NotNull String metadataKey, @NotNull MetadataValue newMetadataValue) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public List<MetadataValue> getMetadata(@NotNull String metadataKey) {
        return null;
    }

    @Override
    public boolean hasMetadata(@NotNull String metadataKey) {
        return false;
    }

    @Override
    public void removeMetadata(@NotNull String metadataKey, @NotNull Plugin owningPlugin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPermissionSet(@NotNull String name) {
        return false;
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission perm) {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull String name) {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull Permission perm) {
        return false;
    }

    @NotNull
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        return null;
    }

    @NotNull
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return null;
    }

    @Nullable
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
        return null;
    }

    @Nullable
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        return null;
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment attachment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recalculatePermissions() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return null;
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean value) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return this.persistentDataContainer;
    }

    @Override
    public void sendPluginMessage(@NotNull Plugin source, @NotNull String channel, @NotNull byte[] message) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Set<String> getListeningPluginChannels() {
        return null;
    }

    @NotNull
    @Override
    public <T extends Projectile> T launchProjectile(@NotNull Class<? extends T> projectile) {
        return null;
    }

    @NotNull
    @Override
    public <T extends Projectile> T launchProjectile(@NotNull Class<? extends T> projectile, @Nullable Vector velocity) {
        return null;
    }
    //endregion

    private static File getUUIDFile(UUID id) {
        var worlds = Bukkit.getWorlds();

        for (var world : worlds) {
            var playerDataFolder = new File(world.getWorldFolder(), "playerdata");
            if (!playerDataFolder.isDirectory()) continue;

            var playerFile = new File(playerDataFolder, id + ".dat");
            if (!playerFile.isFile()) continue;

            return playerFile;
        }

        return null;
    }

    private void loadAttributes() {
        var attributeList = this.container.<ListTag<CompoudTag>>getTag("Attributes");
        if(attributeList == null) return;

        for (var compound : attributeList) {
            var attributeContainer = compound.getAsContainer();

            var baseValue = attributeContainer.<DoubleTag>getTag("Base").getValue();
            var name = NamespacedKey
                    .fromString(attributeContainer.<StringTag>getTag("Name").getValue())
                    .getKey()
                    .replace('.', '_')
                    .toUpperCase(Locale.ROOT);
            var attribute = Attribute.valueOf(name);

            var instance = new OfflineAttributeInstance(
                    AttributeDefaults.getDefault(attribute),
                    attribute,
                    baseValue
            );

            if (attributeContainer.hasTag("Modifiers")) {
                var attributeModifiers = attributeContainer.<ListTag<CompoudTag>>getTag("Modifiers");

                for (var modifier : attributeModifiers) {
                    var modifierContainer = modifier.getAsContainer();

                    var modifierId = modifierContainer.readUUID("UUID");
                    if (modifierId == null) continue;

                    var modifierName = modifierContainer.<StringTag>getTag("Name").getValue();
                    var operation = ATTRIBUTE_OPERATIONS[modifierContainer.<IntTag>getTag("Operation").getValue()];
                    var amount = modifierContainer.<DoubleTag>getTag("Amount").getValue();

                    var modifierInstance = new AttributeModifier(
                            modifierId,
                            modifierName,
                            amount,
                            operation
                    );

                    instance.addModifier(modifierInstance);
                }
            }

            this.attributeInstances.put(attribute, instance);
        }
    }

    private void writeAttributes() {
        var attributeList = this.container.<ListTag<CompoudTag>>getTag("Attributes");
        if(attributeList == null) return;

        var outputList = new ArrayList<CompoudTag>();

        for (var attribute : this.attributeInstances.values()) {
            var attributeContainer = new CompoudTag(null, new HashMap<>());

            outputList.add(attributeContainer);

            attributeContainer.add(new DoubleTag("Base", attribute.getBaseValue()));
            attributeContainer.add(new StringTag("Name", attribute.getAttribute().getKey().toString()));

            var modifiers = attribute.getModifiers();
            if (modifiers.isEmpty()) continue;

            var modifierList = new ListTag<CompoudTag>("Modifiers", new ArrayList<>());
            attributeContainer.add(modifierList);

            for (var modifier : modifiers) {
                var container = new CompoudTag(null, new HashMap<>());

                container.writeUUID("UUID", modifier.getUniqueId());

                container.add(new StringTag("Name", modifier.getName()));
                container.add(new IntTag("Operation", modifier.getOperation().ordinal()));
                container.add(new DoubleTag("Amount", modifier.getAmount()));
            }
        }

        attributeList.setValue(outputList);
    }
}
