package fr.azuxul.pacman.entity;

import fr.azuxul.pacman.GameManager;
import fr.azuxul.pacman.PacMan;
import fr.azuxul.pacman.player.PlayerPacMan;
import fr.azuxul.pacman.powerup.PowerupEffectType;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.World;
import net.samagames.api.games.Status;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Gomme entity
 *
 * @author Azuxul
 * @version 1.0
 */
public class Gomme extends EntityArmorStand {

    private final boolean droopedByPlayer;
    private final boolean big;
    private final int gommeValue;

    /**
     * Class constructor and
     * spawn gomme
     *
     * @param world       world of entity
     * @param x           X location
     * @param y           Y location
     * @param z           Z location
     * @param gommeDrooped if true, gommes was remove after 30s,
     *                    the gomme not subtract of global gomme number
     *                    when is catch and spawn effect is special
     */
    public Gomme(World world, double x, double y, double z, boolean gommeDrooped) {
        super(world);

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        Vector velocity = null;
        droopedByPlayer = gommeDrooped;
        big = false;
        gommeValue = 1;

        c(nbtTagCompound); // Init nbtTagCompound

        nbtTagCompound.setBoolean("Small", true); // Set Small
        nbtTagCompound.setBoolean("NoGravity", !gommeDrooped); // Set NoGravity
        nbtTagCompound.setBoolean("Invulnerable", true); // Set Invulnerable
        nbtTagCompound.setBoolean("Invisible", true); // Set Invisible
        nbtTagCompound.setInt("DisabledSlots", 31); // Disable slots

        f(nbtTagCompound); // Set nbtTagCompound

        setEquipment(4, CraftItemStack.asNMSCopy(new ItemStack(Material.GOLD_BLOCK))); // Set helmet

        if (gommeDrooped) { // If is drooped by player

            GameManager gameManager = PacMan.getGameManager();

            // Kill gomme after 600 ticks (30s)
            gameManager.getServer().getScheduler().runTaskLater(gameManager.getPlugin(), () -> this.getBukkitEntity().remove(), 600L);

            // Get randomly velocity
            velocity = new Vector(2 + RandomUtils.nextInt(4), 5 + RandomUtils.nextInt(4), 2 + RandomUtils.nextInt(3)).multiply(RandomUtils.nextBoolean() ? 0.1 : -0.1);
        }

        spawn(world, x, y, z, velocity); // Spawn
    }

    /**
     * Class constructor and
     * spawn big gomme
     *
     * @param world       world of entity
     * @param x           X location
     * @param y           Y location
     * @param z           Z location
     * @param gommeDrooped if true, gommes was remove after 30s,
     *                    the gomme not subtract of global gomme number
     *                    when is catch and spawn effect is special
     * @param gommeValue   number of gomme added to player gomme count
     *                    when is picked up
     */
    public Gomme(World world, double x, double y, double z, boolean gommeDrooped, int gommeValue) {
        super(world);

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        Vector velocity = null;
        droopedByPlayer = gommeDrooped;
        big = true;
        this.gommeValue = gommeValue;

        c(nbtTagCompound); // Init nbtTagCompound

        nbtTagCompound.setBoolean("Small", false); // Set Normal
        nbtTagCompound.setBoolean("NoGravity", !gommeDrooped); // Set NoGravity
        nbtTagCompound.setBoolean("Invulnerable", true); // Set Invulnerable
        nbtTagCompound.setBoolean("Invisible", true); // Set Invisible
        nbtTagCompound.setInt("DisabledSlots", 31); // Disable slots

        f(nbtTagCompound); // Set nbtTagCompound

        setEquipment(4, CraftItemStack.asNMSCopy(new ItemStack(Material.EMERALD_BLOCK))); // Set helmet

        if (gommeDrooped) { // If is drooped by player

            GameManager gameManager = PacMan.getGameManager();

            // Kill gomme after 600 ticks (30s)
            gameManager.getServer().getScheduler().runTaskLater(gameManager.getPlugin(), () -> this.getBukkitEntity().remove(), 600L);

            // Get randomly velocity
            velocity = new Vector(2 + RandomUtils.nextInt(4), 5 + RandomUtils.nextInt(4), 2 + RandomUtils.nextInt(3)).multiply(RandomUtils.nextBoolean() ? 0.1 : -0.1);
        }

        spawn(world, x, y, z, velocity); // Spawn
    }

    /**
     * Spawn this gomme
     *
     * @param world    world to spawn
     * @param x        x location
     * @param y        y location
     * @param z        z location
     * @param velocity velocity
     */
    private void spawn(World world, double x, double y, double z, Vector velocity) {

        this.setPosition(x, y, z); // Set location
        world.addEntity(this); // Spawn

        if (velocity != null) {
            this.getBukkitEntity().setVelocity(velocity); // Set velocity
            this.setPosition(x, y - 0.3, z);
            this.setGravity(false);
        }
    }

    /**
     * Get if gomme was drooped by player
     *
     * @return droopedByPlayer
     */
    private boolean isDroopedByPlayer() {

        return droopedByPlayer;
    }

    /**
     * Detect collides with entity human
     *
     * @param entityHuman collided human
     */
    @Override
    public void d(EntityHuman entityHuman) {

        Player player = (Player) entityHuman.getBukkitEntity();
        Location playerLocation = player.getLocation();
        GameManager gameManager = PacMan.getGameManager();
        Status status = gameManager.getStatus();
        double distanceAtGomme = this.getBukkitEntity().getLocation().distance(playerLocation); // Calculate distance

        // If IN_GAME, player game mode is not to spectator, gomme is alive and distance at gommes is <= 0.65 or player has gommes magnet booster
        if (status.equals(Status.IN_GAME) && !player.getGameMode().equals(GameMode.SPECTATOR) && this.isAlive()) {

            PlayerPacMan playerPacMan = gameManager.getPlayer(player.getUniqueId());

            if (distanceAtGomme <= 0.65 || big) {

                addGommeToPlayer(player, playerPacMan);

            } else if (playerPacMan.getActiveBooster() != null && playerPacMan.getActiveBooster().equals(PowerupEffectType.GOMME_MAGNET)) {

                attractGomme(playerLocation);
            }
        }
    }

    private void addGommeToPlayer(Player player, PlayerPacMan playerPacMan) {

        GameManager gameManager = PacMan.getGameManager();
        Location playerLocation = player.getLocation();

        die(); // Kill gomme

        player.playNote(playerLocation, Instrument.PIANO, new Note(22));

        playerPacMan.setGomme(playerPacMan.getGomme() + (playerPacMan.getActiveBooster() != null && playerPacMan.getActiveBooster().equals(PowerupEffectType.DOUBLE_GOMMES) ? gommeValue * 2 : gommeValue)); // Add gommes to player

        // Send scoreboard to player
        gameManager.getScoreboard().sendScoreboardToPlayer(player);

        if (!this.isDroopedByPlayer()) { // If gomme was not drooped by player

            // Set global gomme
            int globalGommes = gameManager.getGommeManager().getRemainingGlobalGommes() - gommeValue;
            gameManager.getGommeManager().setRemainingGlobalGommes(globalGommes);
        }
    }

    private void attractGomme(Location location) {

        Vector vector = location.toVector().subtract(new Vector(locX, locY, locZ)).multiply(1.1);

        setGravity(true);
        getBukkitEntity().setVelocity(vector);
        setGravity(false);
    }

    @Override
    public boolean equals(Object compareObject) {

        if (compareObject == null) {
            throw new NullPointerException("The compared object can not be null");
        } else if (this == compareObject)
            return true;
        if (!(compareObject instanceof Gomme))
            return false;
        if (!super.equals(compareObject))
            return false;

        Gomme gomme = (Gomme) compareObject;

        return isDroopedByPlayer() == gomme.isDroopedByPlayer();

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isDroopedByPlayer() ? 1 : 0);
        return result;
    }
}