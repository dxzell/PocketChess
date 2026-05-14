package com.dxzell.pocketchess.spigot.chess.game;

import com.dxzell.pocketchess.spigot.config.SettingsConfig;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

/** Allows playing sounds for different chess game events. */
public final class SoundPlayer {

  private final SettingsConfig settingsConfig;

  @Inject
  public SoundPlayer(SettingsConfig settingsConfig) {
    this.settingsConfig = settingsConfig;
  }

  /**
   * Plays a check sound to the checked player and a move sound to the player who made the move.
   *
   * @param checkedPlayerId the id of the checked player
   * @param movedPlayerId the id of the player who made the move
   */
  public void playCheckMoveSound(UUID checkedPlayerId, UUID movedPlayerId) {
    playSound(ChessGameEvent.CHECK, checkedPlayerId);
    playSound(ChessGameEvent.CHECK, movedPlayerId);
  }

  /**
   * Plays a move sound to both players.
   *
   * @param firstPlayerId the id of the first player
   * @param secondPlayerId the id of the second player
   */
  public void playMoveSound(UUID firstPlayerId, UUID secondPlayerId) {
    playSound(ChessGameEvent.MOVE, firstPlayerId);
    playSound(ChessGameEvent.MOVE, secondPlayerId);
  }

  /**
   * Plays a win sound to the winner and a loss sound to the player who lost the game.
   *
   * @param winnerId the id of the winning player
   * @param loserId the id of the losing player
   */
  public void playWinLoseSounds(UUID winnerId, UUID loserId) {
    playSound(ChessGameEvent.WIN, winnerId);
    playSound(ChessGameEvent.LOSS, loserId);
  }

  /**
   * Plays a draw sound to both players.
   *
   * @param firstPlayerId the id of the first player
   * @param secondPlayerId the id of the second player
   */
  public void playDrawSound(UUID firstPlayerId, UUID secondPlayerId) {
    playSound(ChessGameEvent.DRAW, firstPlayerId);
    playSound(ChessGameEvent.DRAW, secondPlayerId);
  }

  /**
   * Plays the sound for the given chess game event to the given player.
   *
   * @param gameEvent the chess game event
   * @param playerId the id of the player
   */
  private void playSound(ChessGameEvent gameEvent, UUID playerId) {
    Player player = Bukkit.getPlayer(playerId);

    if (player == null) {
      return;
    }

    Sound sound = settingsConfig.getSound(gameEvent);

    if (sound == null) {
      return;
    }

    player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
  }
}
