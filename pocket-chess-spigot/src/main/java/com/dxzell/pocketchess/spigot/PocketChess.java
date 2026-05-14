package com.dxzell.pocketchess.spigot;

import com.dxzell.pocketchess.common.ChessModule;
import com.dxzell.pocketchess.spigot.chess.game.SpigotChessGameService;
import com.dxzell.pocketchess.spigot.command.ChessCommand;
import com.dxzell.pocketchess.spigot.database.DatabaseManager;
import com.dxzell.pocketchess.spigot.database.dao.StatsDAO;
import com.dxzell.pocketchess.spigot.listener.ChessGameListener;
import com.dxzell.pocketchess.spigot.listener.DatabaseListener;
import com.dxzell.pocketchess.spigot.module.ConfigModule;
import com.dxzell.pocketchess.spigot.module.DatabaseModule;
import com.dxzell.pocketchess.spigot.module.SpigotModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class PocketChess extends JavaPlugin {

  @Getter private static Injector injector;

  @Override
  public void onEnable() {
    injector =
        Guice.createInjector(
            new ChessModule(), new SpigotModule(this), new ConfigModule(), new DatabaseModule());

    // Commands
    getCommand("chess").setExecutor(injector.getInstance(ChessCommand.class));
    getCommand("chess").setTabCompleter(injector.getInstance(ChessCommand.class));

    // Handlers
    Bukkit.getPluginManager().registerEvents(injector.getInstance(ChessGameListener.class), this);
    Bukkit.getPluginManager().registerEvents(injector.getInstance(DatabaseListener.class), this);

    // Database
    DatabaseManager databaseManager = injector.getInstance(DatabaseManager.class);
    databaseManager.initialize();

    createStatsEntries();
  }

  @Override
  public void onDisable() {
    // Database
    DatabaseManager databaseManager = injector.getInstance(DatabaseManager.class);
    databaseManager.shutdown();

    // End the running chess games
    SpigotChessGameService gameService = injector.getInstance(SpigotChessGameService.class);
    gameService.endAllGames();
  }

  private void createStatsEntries() {
    StatsDAO statsDAO = injector.getInstance(StatsDAO.class);

    UUID[] onlinePlayerIds =
            Bukkit.getOnlinePlayers().stream()
                    .map(Entity::getUniqueId)
                    .toArray(UUID[]::new);

    Bukkit.getScheduler()
            .runTaskAsynchronously(
                    this,
                    () -> {
                      for (UUID playerId : onlinePlayerIds) {
                        statsDAO.createPlayerEntry(playerId);
                      }
                    });
  }
}
