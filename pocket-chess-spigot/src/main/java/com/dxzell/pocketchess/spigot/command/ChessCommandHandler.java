package com.dxzell.pocketchess.spigot.command;

import com.dxzell.pocketchess.api.game.TimeMode;
import com.dxzell.pocketchess.spigot.chess.game.SpigotChessGame;
import com.dxzell.pocketchess.spigot.chess.game.SpigotChessGameService;
import com.dxzell.pocketchess.spigot.chess.request.DuelRequestService;
import com.dxzell.pocketchess.spigot.config.MessageConfig;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/** Runs actions for different chess commands. */
public class ChessCommandHandler {

  private final MessageConfig messageConfig;
  private final SpigotChessGameService spigotChessGameService;
  private final DuelRequestService duelRequestService;

  @Inject
  public ChessCommandHandler(
      MessageConfig messageConfig,
      SpigotChessGameService spigotChessGameService,
      DuelRequestService duelRequestService) {
    this.messageConfig = messageConfig;
    this.spigotChessGameService = spigotChessGameService;
    this.duelRequestService = duelRequestService;
  }

  /**
   * Handles the command for opening the chess board inventory.
   *
   * @param player the player to open the board for
   */
  public void handleOpen(Player player) {
    SpigotChessGame spigotChessGame = spigotChessGameService.getGameByPlayer(player.getUniqueId());

    if (spigotChessGame != null) {
      spigotChessGame.getInventoryManager().openChessInventory(player.getUniqueId());
    } else {
      player.sendMessage(messageConfig.getNotPlaying());
    }
  }

  /**
   * Handles the command for reacting to a chess duel request.
   *
   * @param player the player who reacts
   * @param reaction either accept or decline
   * @param requestName the name of the requesting player
   */
  public void handleAcceptOrDecline(Player player, String reaction, String requestName) {
    Player duelRequestSender = Bukkit.getPlayer(requestName);
    if (duelRequestSender != null) {
      if (reaction.equalsIgnoreCase("accept")) {
        duelRequestService.acceptRequest(player.getUniqueId(), duelRequestSender.getUniqueId());
      } else {
        duelRequestService.declineRequest(player.getUniqueId(), duelRequestSender.getUniqueId());
      }
    } else {
      player.sendMessage(messageConfig.getDuelRequestSenderOffline());
    }
  }

  /**
   * Handles the command for requesting a duel.
   *
   * @param player the player who requests
   * @param requestName the name of the player to request to
   * @param timeMode the time mode to play
   */
  public void handleDuel(Player player, String requestName, String timeMode) {
    Player duelPlayer = Bukkit.getPlayer(requestName);
    if (duelPlayer != null) {
      if (TimeMode.containsMode(timeMode)) {
        duelRequestService.sendRequest(
            player.getUniqueId(),
            duelPlayer.getUniqueId(),
            player.getName(),
            duelPlayer.getName(),
            TimeMode.fromDisplayName(timeMode));

      } else {
        player.sendMessage(messageConfig.getInvalidTimeMode());
      }
    } else {
      player.sendMessage(messageConfig.getOpponentNotOnline());
    }
  }

  /** Sends an invalid argument command to the given player. */
  public void sendUsage(Player player) {
    player.sendMessage(messageConfig.getInvalidArgsCmd());
  }

  public String[] sendHelp(Player player, String indexString) {
    try {
      int index = Integer.parseInt(indexString);
      player.sendMessage(
          switch (index) {
            case 1 ->
                new String[] {
                  ChatColor.YELLOW
                      + "--------- "
                      + ChatColor.WHITE
                      + "Help: Index (1/1) "
                      + ChatColor.YELLOW
                      + "------------------",
                  ChatColor.GRAY + "Use /chess help <n> to get page n of help.",
                  ChatColor.GOLD
                      + "/chess duel <Player> <TimeMode>"
                      + ChatColor.GRAY
                      + ": "
                      + ChatColor.WHITE
                      + "Sends a chess duel with the given time mode to the given player.",
                  ChatColor.GOLD
                      + "/chess open"
                      + ChatColor.GRAY
                      + ": "
                      + ChatColor.WHITE
                      + "Opens the chess board if a game is currently being played.",
                  ChatColor.GOLD
                      + "/chess accept <Player>"
                      + ChatColor.GRAY
                      + ": "
                      + ChatColor.WHITE
                      + "Accepts a chess duel offer from the given player.",
                  ChatColor.GOLD
                      + "/chess decline <Player>"
                      + ChatColor.GRAY
                      + ": "
                      + ChatColor.WHITE
                      + "Declines a chess duel offer from the given player."
                };
            default -> new String[] {ChatColor.RED + "Invalid index"};
          });
    } catch (NumberFormatException ex) {
      player.sendMessage(ChatColor.RED + "Invalid index");
    }
    return new String[] {ChatColor.RED + "ERROR"};
  }
}
