package com.dxzell.pocketchess.spigot.chess.inventory;

import com.dxzell.pocketchess.api.board.Square;
import com.dxzell.pocketchess.api.piece.PieceColor;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/** Allows translations between chess board squares and chess inventory slots. */
public final class ChessInventoryUtil {

  private ChessInventoryUtil() {}

  /**
   * Translates the given square into the corresponding slot in the correct inventory part.
   *
   * @param square the position on the chess board
   * @param color the player's color which determines the board orientation
   * @return the matching slot index and inventory part
   */
  public static ChessInventorySlot toInventorySlot(Square square, PieceColor color) {
    return switch (color) {
      case WHITE -> {
        ChessInventoryPart part =
            square.getRowIndex() <= 1 ? ChessInventoryPart.LOWER : ChessInventoryPart.UPPER;
        int slot =
            (part == ChessInventoryPart.LOWER ? 9 : 0)
                + square.getColumnIndex()
                + (((part == ChessInventoryPart.LOWER ? 1 : 7) - square.getRowIndex()) * 9);
        yield new ChessInventorySlot(slot, part);
      }
      case BLACK -> {
        ChessInventoryPart part =
            square.getRowIndex() >= 6 ? ChessInventoryPart.LOWER : ChessInventoryPart.UPPER;
        int slot =
            (part == ChessInventoryPart.LOWER ? 9 : 0)
                + 7
                - square.getColumnIndex()
                + ((square.getRowIndex() - (part == ChessInventoryPart.LOWER ? 6 : 0)) * 9);
        yield new ChessInventorySlot(slot, part);
      }
    };
  }

  /**
   * Translates the given inventory slot into the corresponding square on the chess board.
   *
   * @param inventorySlot the slot and inventory part
   * @param color the player's color which determines the board orientation in the inventory
   * @return the matching square on the chess board
   */
  @Nullable
  public static Square toSquare(ChessInventorySlot inventorySlot, PieceColor color) {
    int slot = inventorySlot.slot();
    ChessInventoryPart part = inventorySlot.part();

    // Checks whether clicked slot in the inventory is on the chess board or outside
    boolean invalidLower = part == ChessInventoryPart.LOWER && (slot < 9 || slot > 26);
    boolean invalidUpper = part == ChessInventoryPart.UPPER && (slot < 0 || slot > 53);

    if (invalidLower || invalidUpper) {
      return null;
    }

    // Each inventory row has 9 columns but only 8 are needed -> returns null if slot is at column 9
    if (slot % 9 == 8) {
      return null;
    }

    int rowIndex;
    int columnIndex;

    switch (color) {
      case WHITE -> {
        if (part == ChessInventoryPart.LOWER) {
          int relativeSlot = slot - 9;
          columnIndex = relativeSlot % 9;
          rowIndex = 1 - (relativeSlot / 9);
        } else {
          columnIndex = slot % 9;
          rowIndex = 7 - (slot / 9);
        }
      }

      case BLACK -> {
        if (part == ChessInventoryPart.LOWER) {
          int relativeSlot = slot - 9;
          columnIndex = 7 - (relativeSlot % 9);
          rowIndex = 6 + (relativeSlot / 9);
        } else {
          columnIndex = 7 - (slot % 9);
          rowIndex = slot / 9;
        }
      }

      default -> {
        return null;
      }
    }

    char row = (char) ('1' + rowIndex);
    char column = (char) ('A' + columnIndex);

    return new Square(row, column);
  }

  /**
   *
   *
   * @param square the square of the selected piece
   * @param color the color of the piece
   * @return the item of the requested piece, or null when there is no piece on the given square
   */
  @Nullable
  public static ItemStack getPieceItemFromSquare(
          ChessInventory chessInventory, Square square, PieceColor color) {
    ChessInventorySlot inventorySlot = ChessInventoryUtil.toInventorySlot(square, color);

    if (inventorySlot.part() == ChessInventoryPart.UPPER) {
      return chessInventory.getUpperInv().getItem(inventorySlot.slot());
    } else {
      return chessInventory.getLowerInv()[inventorySlot.slot()];
    }
  }
}
