package com.dxzell.pocketchess.spigot.command;

import com.dxzell.pocketchess.spigot.chess.game.SpigotChessGame;
import com.dxzell.pocketchess.spigot.chess.game.SpigotChessGameService;
import com.dxzell.pocketchess.api.game.TimeMode;
import com.dxzell.pocketchess.spigot.chess.request.DuelRequestService;
import com.dxzell.pocketchess.spigot.config.MessageConfig;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ChessCommand implements CommandExecutor, TabCompleter {

  private final SpigotChessGameService spigotChessGameService;
  private final DuelRequestService duelRequestService;
  private final MessageConfig messageConfig;

  @Inject
  public ChessCommand(
      SpigotChessGameService spigotChessGameService,
      DuelRequestService duelRequestService,
      MessageConfig messageConfig) {
    this.spigotChessGameService = spigotChessGameService;
    this.duelRequestService = duelRequestService;
    this.messageConfig = messageConfig;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
    if (sender instanceof Player player) {
      if (command.getName().equalsIgnoreCase("chess")) {
        switch (args.length) {
          case 1 -> {
            if (args[0].equalsIgnoreCase("open")) {
              handleOpen(player);
            }
          }
          case 2 -> {
            if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("decline")) {
              handleAcceptOrDecline(player, args[0], args[1]);
            }
          }
          case 3 -> {
            if (args[0].equalsIgnoreCase("duel")) {
              handleDuel(player, args[1], args[2]);
            }
          }
        }
      }
    }
    return false;
  }

  /**
   * Handles the command for opening the chess board inventory.
   *
   * @param player the player to open the board for
   */
  private void handleOpen(Player player) {
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
  private void handleAcceptOrDecline(Player player, String reaction, String requestName) {
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
  private void handleDuel(Player player, String requestName, String timeMode) {
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

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command cmd, String label, String[] args) {
    List<String> list = new ArrayList<>();
    List<String> otherPlayerNames =
        Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(playerName -> !playerName.equals(sender.getName()))
            .toList();
    if (args.length == 1) {
      return StringUtil.copyPartialMatches(
          args[0], Arrays.asList("duel", "open", "accept", "decline"), new ArrayList<>());
    } else if (args.length == 2 && args[0].equalsIgnoreCase("duel")) {
      return StringUtil.copyPartialMatches(args[1], otherPlayerNames, new ArrayList<>());
    } else if (args.length == 3 && args[0].equalsIgnoreCase("duel")) {
      return StringUtil.copyPartialMatches(
          args[2], TimeMode.getAllDisplayNames(), new ArrayList<>());
    } else if (args.length == 2
        && (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("decline"))) {
      return StringUtil.copyPartialMatches(args[1], otherPlayerNames, new ArrayList<>());
    }
    return list;
  }
}
