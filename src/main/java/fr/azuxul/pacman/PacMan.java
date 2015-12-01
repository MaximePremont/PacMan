package fr.azuxul.pacman;

import fr.azuxul.pacman.entity.Coin;
import fr.azuxul.pacman.event.PlayerEvent;
import fr.azuxul.pacman.player.PlayerPacMan;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Main class of PacMan plugin for SpigotMC 1.8.8-R0.1-SNAPSHOT
 *
 * @author Azuxul
 * @version 1.0
 */
public class PacMan extends JavaPlugin {

    private static GameManager gameManager;

    public static GameManager getGameManager() {
        return gameManager;
    }

    @Override
    public void onEnable() {

        gameManager = new GameManager(getLogger(), this, getServer()); // Register GameManager

        gameManager.updatePlayerNb(false); // Update player nb

        // Register events
        gameManager.getServer().getPluginManager().registerEvents(new PlayerEvent(), this);

        // Register timer
        gameManager.getServer().getScheduler().scheduleSyncRepeatingTask(this, gameManager.getTimer(), 0l, 20l);

        // Register entity
        registerEntity("Coin", 54, Coin.class);

        // Add players in playerPacManList
        getServer().getOnlinePlayers().forEach(player -> gameManager.getPlayerPacManList().add(new PlayerPacMan(player.getUniqueId(), player.getDisplayName())));

        getServer().getWorlds().get(0).setSpawnLocation(0, 73, 0); // Set spawn location
        getServer().getWorlds().get(0).setDifficulty(Difficulty.PEACEFUL); // Set difficulty

        mapInitialisation();
    }

    /**
     * Initialise the map :
     * Replace gold block with coins
     */
    private void mapInitialisation() {

        World world = ((CraftWorld) getServer().getWorlds().get(0)).getHandle();

        // Replace gold block with coins
        int globalCoins = 0;
        for (int x = -100; x <= 100; x++) {
            for (int z = -100; z <= 100; z++) {
                Block block = getServer().getWorlds().get(0).getBlockAt(x, 71, z); // Get block

                if (block.getType().equals(Material.GOLD_BLOCK)) { // If is gold block
                    block.setType(Material.AIR); // Set air

                    // Spawn coin normal coin

                    double xAdd = x < 0 ? 0.5 : -0.5; // Add to x for adjust location
                    double zAdd = z < 0 ? 0.5 : -0.5; // Add to z for adjust location

                    new Coin(world, x + xAdd, 70.7, z + zAdd, false);
                    globalCoins++;
                }
            }
        }
        gameManager.setGlobalCoins(globalCoins); // Set global coins
    }

    /**
     * Register entity
     *
     * @param name  entity name
     * @param id    entity id
     * @param clazz entity class
     */
    private void registerEntity(String name, int id, Class clazz) {

        // put entity details in maps of EntityTypes class
        ((Map) getPrivateFieldOfEntityTypes("c", null)).put(name, clazz);
        ((Map) getPrivateFieldOfEntityTypes("d", null)).put(clazz, name);
        ((Map) getPrivateFieldOfEntityTypes("e", null)).put(id, clazz);
        ((Map) getPrivateFieldOfEntityTypes("f", null)).put(clazz, id);
        ((Map) getPrivateFieldOfEntityTypes("g", null)).put(name, id);
    }

    /**
     * Get returned object of private field
     *
     * @param fieldName fieldName
     * @param object    parameter of field
     * @return returned object by field
     */
    private Object getPrivateFieldOfEntityTypes(String fieldName, Object object) {

        Field field;
        Object returnObject = null;

        try {

            field = net.minecraft.server.v1_8_R3.EntityTypes.class.getDeclaredField(fieldName);

            field.setAccessible(true);
            returnObject = field.get(object);

        } catch (NoSuchFieldException | IllegalAccessException e) {

            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                gameManager.getLogger().throwing(stackTraceElement.getClassName(), stackTraceElement.getMethodName(), e.getCause());
            }
        }

        return returnObject;
    }
}
