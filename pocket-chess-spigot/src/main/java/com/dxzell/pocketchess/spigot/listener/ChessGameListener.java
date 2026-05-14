package com.dxzell.pocketchess.spigot.listener;

import com.dxzell.pocketchess.api.board.Square;
import com.dxzell.pocketchess.spigot.PocketChess;
import com.dxzell.pocketchess.spigot.chess.inventory.ChessInventoryPart;
import com.dxzell.pocketchess.spigot.chess.inventory.ChessInventorySlot;
import com.dxzell.pocketchess.spigot.chess.inventory.ChessInventoryUtil;
import com.dxzell.pocketchess.spigot.chess.game.SpigotChessGame;
import com.dxzell.pocketchess.spigot.chess.game.SpigotChessGameService;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class ChessGameListener implements Listener {

  private final PocketChess plugin;
  private final SpigotChessGameService gameService;

  @Inject
  public ChessGameListener(PocketChess plugin, SpigotChessGameService gameService) {
    this.plugin = plugin;
    this.gameService = gameService;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    Player player = e.getPlayer();

    Bukkit.getScheduler()
        .runTaskLater(
            plugin,
            () ->
                player.setResourcePack(
                    "https://github.com/dxzell/desktop-tutorial/releases/latest/download/Pocket.Chess.Pack.zip"),
            20L);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent e) {
    Player player = (Player) e.getView().getPlayer();

    SpigotChessGame spigotChessGame = gameService.getGameByPlayer(player.getUniqueId());

    if (e.getClickedInventory() != null
        && spigotChessGame != null
        && spigotChessGame.getInventoryManager().hasInventoryOpen(player.getUniqueId())) {
      Square square =
          ChessInventoryUtil.toSquare(
              new ChessInventorySlot(
                  e.getSlot(),
                  e.getClickedInventory().equals(player.getInventory())
                      ? ChessInventoryPart.LOWER
                      : ChessInventoryPart.UPPER),
              spigotChessGame.getColor(player.getUniqueId()));

      spigotChessGame.handleSquareClick(
          square, player.getUniqueId(), e.getCurrentItem(), e.getSlot());

      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onDamageTaken(EntityDamageEvent e) {
    if (e.getEntity() instanceof Player damagedPlayer) {
      if (damagedPlayer.getHealth() - e.getFinalDamage() <= 0) {
        giveItemsBack(damagedPlayer.getUniqueId());
      }
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent e) {
    giveItemsBack(e.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onWorldChange(PlayerChangedWorldEvent e) {
    giveItemsBack(e.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    giveItemsBack(e.getPlayer().getUniqueId());
  }

  /**
   * Checks whether player is in a game and if he is it puts the saved items back into his
   * inventory.
   *
   * @param playerId the uuid of the player
   */
  private void giveItemsBack(UUID playerId) {
    SpigotChessGame spigotChessGame = gameService.getGameByPlayer(playerId);

    if (spigotChessGame != null
        && spigotChessGame.getInventoryManager().hasInventoryOpen(playerId)) {
      spigotChessGame.getInventoryManager().giveItemsBack(playerId);
    }
  }
}
