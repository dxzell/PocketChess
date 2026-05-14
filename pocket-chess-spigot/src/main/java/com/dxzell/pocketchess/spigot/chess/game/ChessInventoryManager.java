package com.dxzell.pocketchess.spigot.chess.game;

import com.dxzell.pocketchess.api.board.Square;
import com.dxzell.pocketchess.api.game.ChessGame;
import com.dxzell.pocketchess.api.move.Move;
import com.dxzell.pocketchess.api.piece.Piece;
import com.dxzell.pocketchess.api.piece.PieceColor;
import com.dxzell.pocketchess.spigot.chess.inventory.ChessInventory;
import com.dxzell.pocketchess.spigot.chess.inventory.ChessInventoryHighlighter;
import com.dxzell.pocketchess.spigot.chess.inventory.ChessInventoryUpdater;
import com.dxzell.pocketchess.spigot.chess.inventory.item.DrawItemType;
import com.dxzell.pocketchess.spigot.chess.inventory.item.TimeUnitItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

/** Manages both chess inventories of a chess game. */
public final class ChessInventoryManager {

  private final TimeUnitItemBuilder timeUnitItemBuilder;
  private final ChessInventory whitePlayerInventory;
  private final ChessInventoryUpdater whiteInventoryUpdater;
  private final ChessInventoryUpdater blackInventoryUpdater;
  private final ChessInventoryHighlighter whiteInventoryHighlighter;
  private final ChessInventoryHighlighter blackInventoryHighlighter;
  private final ChessInventory blackPlayerInventory;
  private final UUID whitePlayerId;
  private final UUID blackPlayerId;

  public ChessInventoryManager(SpigotChessGame spigotChessGame) {
    timeUnitItemBuilder = spigotChessGame.getTimeUnitItemBuilder();
    whitePlayerInventory = spigotChessGame.getWhitePlayerInventory();
    blackPlayerInventory = spigotChessGame.getBlackPlayerInventory();
    whitePlayerId = whitePlayerInventory.getPlayerId();
    blackPlayerId = blackPlayerInventory.getPlayerId();

    whiteInventoryUpdater = whitePlayerInventory.getChessInventoryUpdater();
    blackInventoryUpdater = blackPlayerInventory.getChessInventoryUpdater();

    whiteInventoryHighlighter = whitePlayerInventory.getChessInventoryHighlighter();
    blackInventoryHighlighter = blackPlayerInventory.getChessInventoryHighlighter();
  }

  /**
   * Opens the chess inventory for the specified player.
   *
   * @param playerId the id of the player
   */
  public void openChessInventory(UUID playerId) {
    Player player = Bukkit.getPlayer(playerId);
    if (player != null) {
      getPlayersChessInventory(playerId).openInventory();
    }
  }

  /**
   * @param playerId the id of the requested player
   * @return whether the given player has currently opened the chess inventory
   */
  public boolean hasInventoryOpen(UUID playerId) {
    Player player = Bukkit.getPlayer(playerId);

    if (player != null) {
      return whitePlayerInventory.getUpperInv().equals(player.getOpenInventory().getTopInventory())
          || blackPlayerInventory.getUpperInv().equals(player.getOpenInventory().getTopInventory());
    }
    return false;
  }

  /**
   * Closes the given players chess inventory if open.
   *
   * @param playerId the id of the player
   */
  private void closeInventory(UUID playerId) {
    Player player = Bukkit.getPlayer(playerId);
    if (player != null
        && player
            .getOpenInventory()
            .getTopInventory()
            .equals(
                playerId.equals(whitePlayerId)
                    ? whitePlayerInventory.getUpperInv()
                    : blackPlayerInventory.getUpperInv())) {
      player.closeInventory();
    }
  }

  /** Closes both chess inventories if they are currently opened. */
  public void closeInventories() {
    closeInventory(whitePlayerId);
    closeInventory(blackPlayerId);
  }

  /**
   * Adds the promotion piece items to the given players inventory.
   *
   * @param playerId the id of the player
   */
  public void addPromotionPieceItems(UUID playerId) {
    getPlayersChessInventory(playerId).getChessInventoryUpdater().addPromotionPieces();
  }

  /**
   * Removes the promotion piece items to the given players inventory.
   *
   * @param playerId the id of the player
   */
  public void removePromotionPieceItems(UUID playerId) {
    getPlayersChessInventory(playerId).getChessInventoryUpdater().removePromotionPieces();
  }

  /** Resets the available moves in both chess inventories. */
  public void resetAvailableMoves() {
    whiteInventoryHighlighter.unhighlightAvailableMoves();
    blackInventoryHighlighter.unhighlightAvailableMoves();
  }

  /**
   * Highlights the available moves for the player that did not make the move.
   *
   * @param chessGame the associated chess game
   */
  public void highlightOtherColorsAvailableMoves(ChessGame chessGame) {
    if (chessGame.getCurrentTurn().equals(chessGame.getWhitePlayerId())) {
      blackInventoryHighlighter.highlightAvailableMoves(
          chessGame.getSelectedPieceSquare(chessGame.getBlackPlayerId()));
    } else {
      whiteInventoryHighlighter.highlightAvailableMoves(
          chessGame.getSelectedPieceSquare(chessGame.getWhitePlayerId()));
    }
  }

  /** Highlights the last played move in both inventories. */
  public void highlightLastPlayedMove() {
    whitePlayerInventory.getChessInventoryHighlighter().highlightLastPlayedMove();
    blackPlayerInventory.getChessInventoryHighlighter().highlightLastPlayedMove();
  }

  /**
   * Unhighlights the time unit item.
   *
   * @param currentTurn the id of the player to make a move
   */
  public void unhighlightInventoryTimeTexture(UUID currentTurn) {
    timeUnitItemBuilder.unhighlightTimeUnits(
        whitePlayerInventory, !whitePlayerId.equals(currentTurn));
    timeUnitItemBuilder.unhighlightTimeUnits(
        blackPlayerInventory, !blackPlayerId.equals(currentTurn));
  }

  /** Unhighlights the selected piece in both chess inventories. */
  public void unhighlightSelectedPiece(Square destination) {
    whiteInventoryHighlighter.unhighlightSelectedPiece(destination);
    blackInventoryHighlighter.unhighlightSelectedPiece(destination);
  }

  /**
   * Updates the chess inventories of both players with the played move.
   *
   * @param move the move that was played
   */
  public void updateInventoryChessBoard(Move move) {
    whiteInventoryUpdater.updateChessBoard(move);
    blackInventoryUpdater.updateChessBoard(move);
  }

  /**
   * Updates the chess inventories of both players with the new time.
   *
   * @param milliseconds the new time in milliseconds
   */
  public void updateInventoryTime(long milliseconds) {
    whiteInventoryUpdater.updateTime(milliseconds);
    blackInventoryUpdater.updateTime(milliseconds);
  }

  /**
   * Removes the piece item from the specified square.
   *
   * @param square the square to remove the piece from
   */
  public void removePieceFromInventories(Square square) {
    whitePlayerInventory.removePiece(square);
    blackPlayerInventory.removePiece(square);
  }

  /**
   * Sets a piece item on the specified square in both inventories.
   *
   * @param square the square to set the piece item on
   * @param piece the piece to set
   */
  public void setPieceInInventories(Square square, Piece piece) {
    whitePlayerInventory.setPiece(square, piece);
    blackPlayerInventory.setPiece(square, piece);
  }

  /**
   * @param square the square to get the piece from
   * @param color the color of the player
   * @return the piece item, or null if square is empty
   */
  @Nullable
  public ItemStack getPieceItemFromInventory(Square square, PieceColor color) {
    return color == PieceColor.WHITE
        ? whitePlayerInventory.getPiece(square)
        : blackPlayerInventory.getPiece(square);
  }

  /**
   * Highlights the draw item of the given players chess inventory.
   *
   * @param drawItemType the type of the draw item
   * @param playerId the id of the player
   */
  public void highlightDrawItem(DrawItemType drawItemType, UUID playerId) {
    getPlayersChessInventory(playerId).getChessInventoryHighlighter().highlightDrawItem(drawItemType);
  }

  /**
   * Highlights the surrender item of the given players chess inventory.
   *
   * @param playerId the id of the player
   *  @param highlight whether the surrender item should be highlighted or unhighlighted
   */
  public void highlightSurrenderItem(UUID playerId, boolean highlight) {
    getPlayersChessInventory(playerId).getChessInventoryHighlighter().highlightSurrenderItem(highlight);
  }

  /**
   * Gives back the saved items to the specified player.
   *
   * @param playerId the id of the player
   */
  public void giveItemsBack(UUID playerId) {
    getPlayersChessInventory(playerId).giveBackItems();
  }

  /**
   * @param playerId the id of the player
   * @return the chess inventory associated to the given player
   */
  public ChessInventory getPlayersChessInventory(UUID playerId) {
    return playerId.equals(whitePlayerId) ? whitePlayerInventory : blackPlayerInventory;
  }

  /**
   * Updates the info item with the given text for the given player.
   *
   * @param message the message to show in the item lore
   * @param playerId the id of the player to show the message to
   */
  public void updateInfo(String message, UUID playerId) {
    getPlayersChessInventory(playerId).getChessInventoryUpdater().updateInfo(message);
  }

  /** Resets the info messages in both inventories. */
  public void resetInfo() {
    whitePlayerInventory.getChessInventoryUpdater().resetInfo();
    blackPlayerInventory.getChessInventoryUpdater().resetInfo();
  }

  /** Cancels the info tasks for both inventories. */
  public void cancelInfoTasks() {
    whitePlayerInventory.getChessInventoryUpdater().cancelInfoTask();
    blackPlayerInventory.getChessInventoryUpdater().cancelInfoTask();
  }
}
