package com.dxzell.pocketchess.spigot.chess.game;

import com.dxzell.pocketchess.api.board.ChessBoard;
import com.dxzell.pocketchess.api.board.Square;
import com.dxzell.pocketchess.api.game.ChessGame;
import com.dxzell.pocketchess.api.move.Move;
import com.dxzell.pocketchess.api.move.MoveCalculator;
import com.dxzell.pocketchess.api.move.MoveResult;
import com.dxzell.pocketchess.api.move.MoveResultType;
import com.dxzell.pocketchess.api.piece.Piece;
import com.dxzell.pocketchess.api.piece.PieceColor;
import com.dxzell.pocketchess.common.board.ChessBoardImpl;
import com.dxzell.pocketchess.spigot.PocketChess;
import com.dxzell.pocketchess.spigot.chess.inventory.ChessInventory;
import com.dxzell.pocketchess.spigot.chess.inventory.ChessInventoryHighlighter;
import com.dxzell.pocketchess.spigot.chess.inventory.item.*;
import com.dxzell.pocketchess.spigot.config.MessageConfig;
import com.dxzell.pocketchess.spigot.config.SettingsConfig;
import com.dxzell.pocketchess.spigot.database.dao.StatsDAO;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/** Stores additional spigot related data for a chess game such as player inventories and more. */
@Getter
public final class SpigotChessGame {

  private final PocketChess plugin;
  private final SpigotChessGameService spigotChessGameService;
  private final ChessGame chessGame;
  private final StatsDAO statsDAO;
  private final ChessInventory whitePlayerInventory;
  private final ChessInventory blackPlayerInventory;
  private final DrawHandler drawHandler;
  private final SurrenderHandler surrenderHandler;
  private final ChessBoard chessBoard;
  private final ChessGameClock chessGameClock;
  private final ChessInventoryManager inventoryManager;
  private final TimeUnitItemBuilder timeUnitItemBuilder;
  private final ChessMenuItemBuilder chessMenuItemBuilder;
  private final PromotionItemBuilder promotionItemBuilder;
  private final ChessMoveHandler moveHandler;
  private final MessageConfig messageConfig;
  private final SettingsConfig settingsConfig;
  private final SoundPlayer soundPlayer;

  public SpigotChessGame(
      PocketChess plugin,
      SpigotChessGameService spigotChessGameService,
      ChessGame chessGame,
      StatsDAO statsDAO,
      UUID whitePlayerId,
      UUID blackPlayerId,
      MoveCalculator moveCalculator,
      PieceItemBuilder pieceItemBuilder,
      TimeUnitItemBuilder timeUnitItemBuilder,
      ChessMenuItemBuilder chessMenuItemBuilder,
      PromotionItemBuilder promotionItemBuilder,
      MessageConfig messageConfig,
      SettingsConfig settingsConfig,
      SoundPlayer soundPlayer) {
    this.plugin = plugin;
    this.spigotChessGameService = spigotChessGameService;
    this.chessGame = chessGame;
    this.statsDAO = statsDAO;
    this.chessBoard = chessGame.getChessBoard();
    this.timeUnitItemBuilder = timeUnitItemBuilder;
    this.chessMenuItemBuilder = chessMenuItemBuilder;
    this.promotionItemBuilder = promotionItemBuilder;
    this.messageConfig = messageConfig;
    this.settingsConfig = settingsConfig;
    this.soundPlayer = soundPlayer;

    whitePlayerInventory =
        new ChessInventory(
            whitePlayerId,
            this,
            moveCalculator,
            pieceItemBuilder,
            timeUnitItemBuilder,
            chessMenuItemBuilder);
    blackPlayerInventory =
        new ChessInventory(
            blackPlayerId,
            this,
            moveCalculator,
            pieceItemBuilder,
            timeUnitItemBuilder,
            chessMenuItemBuilder);

    chessGameClock = new ChessGameClock(plugin, this);

    inventoryManager = new ChessInventoryManager(this);

    moveHandler = new ChessMoveHandler(this, chessGame, moveCalculator, inventoryManager);

    drawHandler = new DrawHandler(this);

    surrenderHandler = new SurrenderHandler(this);

    chessGameClock.startTimeRunnable();

    sendGameMessage(messageConfig.getGameStarted());
  }

  /**
   * Validates whether an own piece was selected or a move was played.
   *
   * @param square the square that was clicked
   * @param playerId the id of the player who made the click
   * @param clickedItem the clicked item within the chess inventory
   * @param slot the clicked slot
   */
  public void handleSquareClick(Square square, UUID playerId, ItemStack clickedItem, int slot) {
    Piece piece = chessBoard.getPiece(square);
    Square selectedSquare = chessGame.getSelectedPieceSquare(playerId);

    if (moveHandler.handlePromotionSelection(
        (ChessBoardImpl) chessBoard,
        inventoryManager,
        promotionItemBuilder,
        clickedItem,
        slot,
        playerId)) {
      return;
    }

    if (chessMenuItemBuilder.isChessMenuItem(clickedItem, ChessMenuItemType.DRAW)) {
      drawHandler.handleDrawClick(playerId);
      return;
    }

    if (chessMenuItemBuilder.isChessMenuItem(clickedItem, ChessMenuItemType.SURRENDER)) {
      surrenderHandler.handleSurrenderClick(playerId);
      return;
    }

    if (piece != null) {
      if (piece.color() == getColor(playerId)) {
        handleSelectPiece(square, playerId);
      } else if (selectedSquare != null) {
        if (chessGame.getCurrentTurn().equals(playerId)) {
          handleMove(square, playerId);
        } else {
          inventoryManager.updateInfo(messageConfig.getNotYourTurn(), playerId);
        }
      } else {
        inventoryManager.updateInfo(messageConfig.getNoPieceSelected(), playerId);
      }
    } else {
      if (selectedSquare != null) {
        if (chessGame.getCurrentTurn().equals(playerId)) {
          handleMove(square, playerId);
        } else {
          inventoryManager.updateInfo(messageConfig.getNotYourTurn(), playerId);
        }
      } else {
        inventoryManager.updateInfo(messageConfig.getNoPieceSelected(), playerId);
      }
    }
  }

  /**
   * Tries to play the move and updates the chess inventories when move was successful.
   *
   * @param destination the destination square the player wants to move his selected piece to
   * @param playerId the id of the player who played the move
   */
  private void handleMove(Square destination, UUID playerId) {
    MoveResult moveResult = chessGame.makeMove(destination, playerId);
    Move move = new Move(null, chessGame.getSelectedPieceSquare(playerId), destination);

    if (moveResult.type() == MoveResultType.SUCCESS) {
      moveHandler.handleSuccessfulMove(moveResult, move, playerId, messageConfig);
    } else {
      inventoryManager.updateInfo(messageConfig.getInvalidSquare(), playerId);
    }
  }

  /**
   * Selects and highlights the piece on the specified square.
   *
   * @param clickedSquare the square to highlight
   * @param playerId the id of the player who selected a piece
   */
  private void handleSelectPiece(Square clickedSquare, UUID playerId) {
    Square beforeSelectedSquare = chessGame.getSelectedPieceSquare(playerId);
    ChessInventory playersChessInventory = inventoryManager.getPlayersChessInventory(playerId);

    if (beforeSelectedSquare != null && beforeSelectedSquare.equals(clickedSquare)) {
      unselectPiece(playersChessInventory.getChessInventoryHighlighter(), playerId, clickedSquare);
    } else {
      selectPiece(
          playersChessInventory.getChessInventoryHighlighter(),
          playerId,
          clickedSquare,
          beforeSelectedSquare);
    }
    inventoryManager.removePromotionPieceItems(playerId);
  }

  /**
   * Unhighlights and unselects the piece if it got clicked again.
   *
   * @param chessInventoryHighlighter the players chess inventory highlighter
   * @param playerId the player who made the click
   * @param clickedSquare the clicked square
   */
  private void unselectPiece(
      ChessInventoryHighlighter chessInventoryHighlighter, UUID playerId, Square clickedSquare) {
    chessInventoryHighlighter.unhighlightSelectedPiece(clickedSquare);
    chessInventoryHighlighter.unhighlightAvailableMoves();

    chessGame.unselectPiece(playerId);
  }

  /**
   * Highlights and selects the piece.
   *
   * @param chessInventoryHighlighter the players chess inventory highlighter
   * @param playerId the player who made the click
   * @param clickedSquare the clicked square
   * @param beforeSelectedSquare the square that was selected before
   */
  private void selectPiece(
      ChessInventoryHighlighter chessInventoryHighlighter,
      UUID playerId,
      Square clickedSquare,
      Square beforeSelectedSquare) {
    chessInventoryHighlighter.highlightSelectedPiece(beforeSelectedSquare, clickedSquare);
    chessInventoryHighlighter.highlightAvailableMoves(clickedSquare);
    chessGame.selectPiece(clickedSquare, playerId);
  }

  /** Ends the game by giving the players back the saved items. */
  public void endGame(UUID winnerId) {
    adjustStats(winnerId);
    chessGame.endGame(winnerId);
    spigotChessGameService.removeGame(chessGame.getGameId());
    inventoryManager.closeInventories();
    inventoryManager.cancelInfoTasks();
    whitePlayerInventory.giveBackItems();
    blackPlayerInventory.giveBackItems();
    chessGameClock.stopRunnable();

    if (winnerId != null) {
      sendGameMessage(
          ChatColor.GREEN
              + "The winner of the game is: "
              + ChatColor.GOLD
              + Bukkit.getOfflinePlayer(winnerId).getName());
      soundPlayer.playWinLoseSounds(winnerId, getOtherPlayerId(winnerId));
    } else {
      sendGameMessage(ChatColor.GRAY + "The game ended in a draw.");
      soundPlayer.playDrawSound(chessGame.getWhitePlayerId(), chessGame.getBlackPlayerId());
    }
  }

  private void adjustStats(UUID winnerId) {
    if (winnerId != null) {
      addWinLose(winnerId, getOtherPlayerId(winnerId));
    } else {
      addDraw(chessGame.getWhitePlayerId(), chessGame.getBlackPlayerId());
    }
  }

  private void addWinLose(UUID winnerId, UUID loserId) {
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            plugin,
            () -> {
              statsDAO.incrementWins(winnerId);
              statsDAO.incrementLosses(loserId);
            });
  }

  private void addDraw(UUID whitePlayerId, UUID blackPlayerId) {
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            plugin,
            () -> {
              statsDAO.incrementDraws(whitePlayerId);
              statsDAO.incrementDraws(blackPlayerId);
            });
  }

  /**
   * @param playerId the id of the player
   * @return the players color
   */
  public PieceColor getColor(UUID playerId) {
    return chessGame.getColor(playerId);
  }

  /**
   * @param playerId the id of the player
   * @return the id of the other player
   */
  public UUID getOtherPlayerId(UUID playerId) {
    return playerId.equals(chessGame.getWhitePlayerId())
        ? chessGame.getBlackPlayerId()
        : chessGame.getWhitePlayerId();
  }

  /**
   * Sends the given message to both players of the game.
   *
   * @param message the message to send
   */
  public void sendGameMessage(String message) {
    Player whitePlayer = Bukkit.getPlayer(chessGame.getWhitePlayerId());
    if (whitePlayer != null) {
      whitePlayer.sendMessage(message);
    }

    Player blackPlayer = Bukkit.getPlayer(chessGame.getBlackPlayerId());
    if (blackPlayer != null) {
      blackPlayer.sendMessage(message);
    }
  }
}
