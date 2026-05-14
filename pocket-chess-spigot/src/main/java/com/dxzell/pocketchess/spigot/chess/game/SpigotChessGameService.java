package com.dxzell.pocketchess.spigot.chess.game;

import com.dxzell.pocketchess.api.game.ChessGameService;
import com.dxzell.pocketchess.api.game.ChessGame;
import com.dxzell.pocketchess.api.game.GameCreationResult;
import com.dxzell.pocketchess.api.game.GameCreationResultType;
import com.dxzell.pocketchess.api.game.TimeMode;
import com.dxzell.pocketchess.api.move.MoveCalculator;
import com.dxzell.pocketchess.spigot.PocketChess;
import com.dxzell.pocketchess.spigot.chess.inventory.item.ChessMenuItemBuilder;
import com.dxzell.pocketchess.spigot.chess.inventory.item.PieceItemBuilder;
import com.dxzell.pocketchess.spigot.chess.inventory.item.PromotionItemBuilder;
import com.dxzell.pocketchess.spigot.chess.inventory.item.TimeUnitItemBuilder;
import com.dxzell.pocketchess.spigot.config.MessageConfig;
import com.dxzell.pocketchess.spigot.config.SettingsConfig;
import com.dxzell.pocketchess.spigot.database.dao.StatsDAO;
import com.google.inject.Inject;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SpigotChessGameService {

  private final Map<UUID, SpigotChessGame> spigotGames = new HashMap<>();
  private final PocketChess plugin;
  private final ChessGameService chessService;
  private final StatsDAO statsDAO;
  private final MoveCalculator moveCalculator;
  private final PieceItemBuilder pieceItemBuilder;
  private final TimeUnitItemBuilder timeUnitItemBuilder;
  private final ChessMenuItemBuilder infoItemBuilder;
  private final PromotionItemBuilder promotionItemBuilder;
  private final MessageConfig messageConfig;
  private final SettingsConfig settingsConfig;
  private final SoundPlayer soundPlayer;

  @Inject
  public SpigotChessGameService(
      PocketChess plugin,
      ChessGameService chessService,
      StatsDAO statsDAO,
      MoveCalculator moveCalculator,
      PieceItemBuilder pieceItemBuilder,
      TimeUnitItemBuilder timeUnitItemBuilder,
      ChessMenuItemBuilder infoItemBuilder,
      PromotionItemBuilder promotionItemBuilder,
      MessageConfig messageConfig,
      SettingsConfig settingsConfig,
      SoundPlayer soundPlayer) {
    this.plugin = plugin;
    this.chessService = chessService;
    this.statsDAO = statsDAO;
    this.moveCalculator = moveCalculator;
    this.pieceItemBuilder = pieceItemBuilder;
    this.timeUnitItemBuilder = timeUnitItemBuilder;
    this.infoItemBuilder = infoItemBuilder;
    this.promotionItemBuilder = promotionItemBuilder;
    this.messageConfig = messageConfig;
    this.settingsConfig = settingsConfig;
    this.soundPlayer = soundPlayer;
  }

  /**
   * Creates a chess game and additionally a spigot chess game which includes the inventories and
   * more.
   *
   * @param whitePlayer the player who starts with white
   * @param blackPlayer the player who starts with black
   * @return the result of the game creation attempt
   */
  public GameCreationResult createGame(Player whitePlayer, Player blackPlayer, TimeMode timeMode) {
    if (whitePlayer == null || blackPlayer == null) {
      throw new IllegalArgumentException(
          "Player objects must not be null. They need to be checked before calling this method.");
    }

    GameCreationResult result =
        chessService.createGame(whitePlayer.getUniqueId(), blackPlayer.getUniqueId(), timeMode);
    if (result.type() == GameCreationResultType.SUCCESS) {
      ChessGame game = result.game();
      spigotGames.put(
          game.getGameId(),
          new SpigotChessGame(
              plugin,
              this,
              game,
              statsDAO,
              whitePlayer.getUniqueId(),
              blackPlayer.getUniqueId(),
              moveCalculator,
              pieceItemBuilder,
              timeUnitItemBuilder,
              infoItemBuilder,
              promotionItemBuilder,
              messageConfig,
              settingsConfig,
              soundPlayer));
    }
    return result;
  }

  /**
   * Ends a running and unfinished game. There will be no winner or loser.
   *
   * @param gameId the id of the requested game
   * @return true if the game was ended, or false if the game wasn't found
   */
  public boolean endGameById(UUID gameId) {
    SpigotChessGame spigotGame = spigotGames.get(gameId);
    if (spigotGame != null && chessService.endGameById(gameId)) {
      spigotGame.endGame(null);
      spigotGames.remove(gameId);
      return true;
    }
    return false;
  }

  /**
   * Ends a running and unfinished game. There will be no winner or loser.
   *
   * @param playerId a player id from the requested game (either black or white)
   * @return true if the game was ended, or false if no game was played by this player
   */
  public boolean endGameByPlayerId(UUID playerId) {
    SpigotChessGame spigotGame = getGameByPlayer(playerId);
    if (spigotGame != null && chessService.endGameByPlayer(playerId)) {
      spigotGame.endGame(null);
      spigotGames.remove(spigotGame.getChessGame().getGameId());
      return true;
    }
    return false;
  }

  public void removeGame(UUID gameId) {
    spigotGames.remove(gameId);
  }

  public List<SpigotChessGame> getGames() {
    return spigotGames.values().stream().toList();
  }

  /** Ends all currently running games. */
  public void endAllGames() {
    for (SpigotChessGame spigotChessGame : spigotGames.values()) {
      spigotChessGame.endGame(null);
    }
    spigotGames.clear();
  }

  /**
   * @param playerId a player id from the requested game (either black or white)
   * @return the played game, or null if not existing
   */
  public SpigotChessGame getGameByPlayer(UUID playerId) {
    for (SpigotChessGame spigotGame : spigotGames.values()) {
      if (spigotGame.getChessGame().getBlackPlayerId().equals(playerId)
          || spigotGame.getChessGame().getWhitePlayerId().equals(playerId)) {
        return spigotGame;
      }
    }

    return null;
  }

  /**
   * @param playerId the id of the player
   * @return whether the player is currently playing a chess game
   */
  public boolean isPlaying(UUID playerId) {
    return chessService.isPlaying(playerId);
  }
}
