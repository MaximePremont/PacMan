package fr.azuxul.pacman;

import fr.azuxul.pacman.player.PlayerPacMan;
import fr.azuxul.pacman.scoreboard.ScoreboardPacMan;
import fr.azuxul.pacman.timer.TimerPacMan;
import net.samagames.api.games.Game;
import net.samagames.api.games.themachine.messages.ITemplateManager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Logger;

/**
 * GameManager for PacMan plugin
 *
 * @author Azuxul
 * @version 1.0
 */
public class GameManager extends Game<PlayerPacMan> {

    private final Server server;
    private final Logger logger;
    private final Plugin plugin;
    private final TimerPacMan timer;
    private final ScoreboardPacMan scoreboard;
    private final List<PlayerPacMan> playerPacManList;
    private final Map<Location, Boolean> boosterLocations;
    private int remainingGlobalCoins, globalCoins;

    /**
     * Class constructor
     *
     * @param logger          plugin logger
     * @param server          server
     * @param gameCodeName    unique id for game
     * @param gameName        game name
     * @param gameDescription game description
     * @param gamePlayerClass game players class
     */
    public GameManager(Logger logger, Plugin plugin, Server server, String gameCodeName, String gameName, String gameDescription, Class<PlayerPacMan> gamePlayerClass) {

        super(gameCodeName, gameName, gameDescription, gamePlayerClass);

        this.server = server;
        this.logger = logger;
        this.plugin = plugin;
        this.scoreboard = new ScoreboardPacMan(ChatColor.YELLOW + "Pac-Man", this);
        this.timer = new TimerPacMan(this);
        this.playerPacManList = new ArrayList<>();
        this.boosterLocations = new HashMap<>();
    }

    /**
     * Get map of booster spawn location
     * Key is location
     * Value is boolean of isSpawn
     *
     * @return boosterLocations
     */
    public Map<Location, Boolean> getBoosterLocations() {
        return boosterLocations;
    }

    /**
     * Get list of playerPacMan
     *
     * @return playerListPacMan
     */
    public List<PlayerPacMan> getPlayerPacManList() {
        return playerPacManList;
    }

    /**
     * Get game timer
     *
     * @return timer
     */
    public TimerPacMan getTimer() {
        return timer;
    }

    /**
     * Get game scoreboard
     *
     * @return scoreboard
     */
    public ScoreboardPacMan getScoreboard() {
        return scoreboard;
    }

    /**
     * Get server
     *
     * @return server
     */
    public Server getServer() {
        return server;
    }

    /**
     * Get logger of plugin
     *
     * @return logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Get plugin
     *
     * @return plugin
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Get number of global coins remaining
     *
     * @return remainingGlobalCoins
     */
    public int getRemainingGlobalCoins() {
        return remainingGlobalCoins;
    }

    /**
     * Set number of global coins remaining
     *
     * @param remainingGlobalCoins global coins remaining
     */
    public void setRemainingGlobalCoins(int remainingGlobalCoins) {
        this.remainingGlobalCoins = remainingGlobalCoins;
    }

    /**
     * Set number of global coins
     *
     * @param globalCoins global coins
     */
    public void setGlobalCoins(int globalCoins) {
        this.setRemainingGlobalCoins(globalCoins);
        this.globalCoins = globalCoins;
    }

    /**
     * Start the game
     */
    @SuppressWarnings("deprecation")
    @Override
    public void startGame() {

        super.startGame();

        Location spawn = new Location(getServer().getWorlds().get(0), 0, 78, 0);

        for (Player player : server.getOnlinePlayers()) {
            player.teleport(spawn); // Teleport player to spawn
            player.setGameMode(GameMode.ADVENTURE); // Set player gamemode
        }
    }

    /**
     * Set end of game
     */
    public void end() {

        ITemplateManager templateManager = getCoherenceMachine().getTemplateManager();
        List<PlayerPacMan> winners = new ArrayList<>();

        timer.setToZero(); // Set timer to zero

        // Sort playerPacManList
        Collections.sort(playerPacManList);

        int playerPacManListSize = playerPacManList.size();

        // Get winners
        for (int i = 1; i <= 3; i++) {
            if (winners.size() >= playerPacManListSize)
                break;

            winners.add(playerPacManList.get(playerPacManListSize - i));
        }

        // Add coins to players
        for (PlayerPacMan playerPacMan : playerPacManList) {

            int percentOfCoins = 0;

            if (playerPacMan.getGameCoins() > 0) {
                percentOfCoins = playerPacMan.getGameCoins() * 100 / globalCoins; // Calculate percent of player coins
                int coins = percentOfCoins / 5; // Calculate coins for player

                playerPacMan.addCoins(coins, percentOfCoins + "% des piéces récupérer");
            }

            if (winners.contains(playerPacMan)) {
                if (winners.indexOf(playerPacMan) == 0) {
                    playerPacMan.addCoins(30, "Partie gagnée");
                    playerPacMan.addStars(2, "Partie gagnée");
                } else {
                    playerPacMan.addCoins(15, "Términer dans le classement");
                    playerPacMan.addStars(1, "Términer dans le classement");
                }
            }

            if (percentOfCoins >= 35) {
                playerPacMan.addStars(1, "Plus de 35% de coins récupérer");
            }
        }

        // Display winners
        if (!winners.isEmpty()) { // If winners is not empty
            if (winners.size() < 3) { // If winners size < 3

                PlayerPacMan winner = winners.get(0); // Get winner

                templateManager.getPlayerWinTemplate().execute(winner.getPlayerIfOnline(), winner.getGameCoins()); // Display player win template
            } else {

                PlayerPacMan winner = winners.get(0), second = winners.get(1), third = winners.get(2); // Get winners

                templateManager.getPlayerLeaderboardWinTemplate().execute(winner.getPlayerIfOnline(), second.getPlayerIfOnline(), third.getPlayerIfOnline(), winner.getGameCoins(), second.getGameCoins(), third.getGameCoins()); // Display players leadboard template
            }
        }

        this.handleGameEnd();
    }
}
