package com.dxzell.pocketchess.spigot.command;

import com.dxzell.pocketchess.api.game.TimeMode;
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

  private final ChessCommandHandler chessCommandHandler;

  @Inject
  public ChessCommand(ChessCommandHandler chessCommandHandler) {
    this.chessCommandHandler = chessCommandHandler;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
    if (sender instanceof Player player) {
      if (command.getName().equalsIgnoreCase("chess")) {
        switch (args.length) {
          case 1 -> {
            if (args[0].equalsIgnoreCase("open")) {
              chessCommandHandler.handleOpen(player);
            } else if (args[0].equalsIgnoreCase("help")) {
              chessCommandHandler.sendHelp(player, "1");
            } else {
              chessCommandHandler.sendUsage(player);
            }
          }
          case 2 -> {
            if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("decline")) {
              chessCommandHandler.handleAcceptOrDecline(player, args[0], args[1]);
            } else if (args[0].equalsIgnoreCase("help")) {
              chessCommandHandler.sendHelp(player, args[1]);
            } else {
              chessCommandHandler.sendUsage(player);
            }
          }
          case 3 -> {
            if (args[0].equalsIgnoreCase("duel")) {
              chessCommandHandler.handleDuel(player, args[1], args[2]);
            } else {
              chessCommandHandler.sendUsage(player);
            }
          }
          default -> {
            chessCommandHandler.sendUsage(player);
          }
        }
      }
    }
    return false;
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
          args[0], Arrays.asList("duel", "open", "accept", "decline", "help"), new ArrayList<>());
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
