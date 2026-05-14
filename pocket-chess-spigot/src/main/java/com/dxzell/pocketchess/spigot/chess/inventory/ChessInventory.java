package com.dxzell.pocketchess.spigot.chess.inventory;

import com.dxzell.pocketchess.api.board.ChessBoard;
import com.dxzell.pocketchess.api.board.Square;
import com.dxzell.pocketchess.api.move.MoveCalculator;
import com.dxzell.pocketchess.api.piece.Piece;
import com.dxzell.pocketchess.api.piece.PieceColor;
import com.dxzell.pocketchess.spigot.chess.inventory.item.ChessMenuItemBuilder;
import com.dxzell.pocketchess.spigot.chess.inventory.item.PieceItemBuilder;
import com.dxzell.pocketchess.spigot.chess.inventory.item.TimeUnitItemBuilder;
import com.dxzell.pocketchess.spigot.chess.game.SpigotChessGame;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Represents a players chess inventory which consists of the newly created upper inventory and the
 * lower part which is the players inventory. Both combined are displaying the whole chess board and
 * more.
 */
@Getter
public final class ChessInventory {

  private final SpigotChessGame spigotChessGame;
  private final ChessBoard chessBoard;
  private final PieceColor color;
  private final UUID playerId;
  private final Inventory upperInv;
  private final ItemStack[] lowerInv = new ItemStack[36];
  private ItemStack[] playerItems;

  private final ChessInventoryUpdater chessInventoryUpdater;
  private final ChessInventoryHighlighter chessInventoryHighlighter;
  private final ChessMenuItemBuilder chessMenuItemBuilder;
  private final PieceItemBuilder pieceItemBuilder;
  private final TimeUnitItemBuilder timeUnitItemBuilder;
  private final MoveCalculator moveCalculator;

  public ChessInventory(
      UUID playerId,
      SpigotChessGame spigotChessGame,
      MoveCalculator moveCalculator,
      PieceItemBuilder pieceItemBuilder,
      TimeUnitItemBuilder timeUnitItemBuilder,
      ChessMenuItemBuilder chessMenuItemBuilder) {
    this.moveCalculator = moveCalculator;
    this.pieceItemBuilder = pieceItemBuilder;
    this.timeUnitItemBuilder = timeUnitItemBuilder;
    this.chessMenuItemBuilder = chessMenuItemBuilder;
    this.spigotChessGame = spigotChessGame;
    this.playerId = playerId;
    this.upperInv = Bukkit.createInventory(null, 54, "§f月日");

    color = spigotChessGame.getColor(playerId);
    chessBoard = spigotChessGame.getChessBoard();
    chessInventoryUpdater = new ChessInventoryUpdater(spigotChessGame, this, playerId);
    chessInventoryHighlighter =
        new ChessInventoryHighlighter(
            spigotChessGame,
            this,
            chessInventoryUpdater,
            chessBoard,
            pieceItemBuilder,
            moveCalculator,
            playerId,
            color);

    ChessInventoryInitializer.initialize(this);
    openInventory();
  }

  /** Opens the chess inventory by synchronizing upper and lower inventory. */
  public void openInventory() {
    Player player = Bukkit.getPlayer(playerId);
    if (player == null) {
      return;
    }
    playerItems = player.getInventory().getStorageContents();
    player.getInventory().setStorageContents(lowerInv);
    player.openInventory(upperInv);
  }

  /** Puts the saved items back into the players inventory. */
  public void giveBackItems() {
    Player player = Bukkit.getPlayer(playerId);

    if (player != null && playerItems != null) {
      player.getInventory().setStorageContents(playerItems);
      playerItems = null;
    }
  }

  /**
   * @param square the square to get the piece from
   * @return the item of the piece, or square was empty
   */
  @Nullable
  public ItemStack getPiece(Square square) {
    ChessInventorySlot inventorySlot = ChessInventoryUtil.toInventorySlot(square, color);
    if (inventorySlot.part() == ChessInventoryPart.UPPER) {
      return upperInv.getItem(inventorySlot.slot());
    } else {
     return lowerInv[inventorySlot.slot()];
    }
  }

  /**
   * Removes the piece from the given square
   *
   * @param square the square with the piece to remove
   */
  public void removePiece(Square square) {
    ChessInventorySlot inventorySlot = ChessInventoryUtil.toInventorySlot(square, color);
    if (inventorySlot.part() == ChessInventoryPart.UPPER) {
      upperInv.setItem(inventorySlot.slot(), null);
    } else {
      lowerInv[inventorySlot.slot()] = null;
      chessInventoryUpdater.updateLowerPart();
    }
  }

  /**
   * Adds a piece on the given square.
   *
   * @param square the square to set the piece on
   * @param piece the piece item to set
   */
  public void setPiece(Square square, Piece piece) {
    ChessInventorySlot inventorySlot = ChessInventoryUtil.toInventorySlot(square, color);
    if (inventorySlot.part() == ChessInventoryPart.UPPER) {
      upperInv.setItem(inventorySlot.slot(), pieceItemBuilder.createPieceItem(piece));
    } else {
      lowerInv[inventorySlot.slot()] = pieceItemBuilder.createPieceItem(piece);
      chessInventoryUpdater.updateLowerPart();
    }
  }
}
