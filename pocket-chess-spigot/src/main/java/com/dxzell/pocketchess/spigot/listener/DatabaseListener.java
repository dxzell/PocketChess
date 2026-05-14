package com.dxzell.pocketchess.spigot.listener;

import com.dxzell.pocketchess.spigot.PocketChess;
import com.dxzell.pocketchess.spigot.database.dao.StatsDAO;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class DatabaseListener implements Listener {

  private final PocketChess plugin;
  private final StatsDAO statsDAO;

  @Inject
  public DatabaseListener(PocketChess plugin, StatsDAO statsDAO) {
    this.plugin = plugin;
    this.statsDAO = statsDAO;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    UUID playerId = e.getPlayer().getUniqueId();

    Bukkit.getScheduler()
        .runTaskAsynchronously(
            plugin,
            () -> {
              statsDAO.createPlayerEntry(playerId);
            });
  }
}
