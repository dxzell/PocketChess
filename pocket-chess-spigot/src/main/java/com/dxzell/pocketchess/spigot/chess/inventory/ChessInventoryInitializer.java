package com.dxzell.pocketchess.spigot.chess.inventory;

import com.dxzell.pocketchess.api.game.ChessGame;
import com.dxzell.pocketchess.api.piece.Piece;
import com.dxzell.pocketchess.api.piece.PieceColor;
import com.dxzell.pocketchess.api.piece.PieceType;
import com.dxzell.pocketchess.spigot.chess.inventory.item.ChessMenuItemBuilder;
import com.dxzell.pocketchess.spigot.chess.inventory.item.ChessMenuItemType;
import com.dxzell.pocketchess.spigot.chess.inventory.item.PieceItemBuilder;
import com.dxzell.pocketchess.spigot.chess.inventory.time.TimeUnit;
import com.dxzell.pocketchess.spigot.chess.inventory.item.TimeUnitItemBuilder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

/** Initializes the chess inventory. */
public final class ChessInventoryInitializer {

  private ChessInventoryInitializer() {}

  /**
   * Puts the chess inventory in a starting state.
   *
   * @param chessInventory the inventory representing the chess board and more
   */
  public static void initialize(ChessInventory chessInventory) {
    setPawns(
        chessInventory.getPieceItemBuilder(),
        chessInventory.getUpperInv(),
        chessInventory.getLowerInv(),
        chessInventory.getColor());

    setUpperInvRemainingPieces(
        chessInventory.getPieceItemBuilder(),
        chessInventory.getUpperInv(),
        chessInventory.getColor());

    setUpperInvTimeUnitItems(
        chessInventory.getSpigotChessGame().getChessGame(),
        chessInventory.getPlayerId(),
        chessInventory.getUpperInv(),
        chessInventory.getTimeUnitItemBuilder());

    setLowerInvRemainingItems(chessInventory.getLowerInv(), chessInventory.getChessMenuItemBuilder());

    setLowerInvRemainingPieces(
        chessInventory.getPieceItemBuilder(),
        chessInventory.getLowerInv(),
        chessInventory.getColor());
  }

  /**
   * Sets the pawns in both lower and upper inventory.
   *
   * @param pieceItemBuilder to build piece items
   * @param upperInv the upper inventory of the player
   * @param lowerInv the lower inventory of the player as an ItemStack array
   * @param color the color of the associated player
   */
  private static void setPawns(
      PieceItemBuilder pieceItemBuilder,
      Inventory upperInv,
      ItemStack[] lowerInv,
      PieceColor color) {
    for (int i = 9; i <= 16; i++) {
      upperInv.setItem(
          i,
          pieceItemBuilder.createPieceItem(
              new Piece(PieceType.PAWN, PieceColor.getOtherColor(color))));
      lowerInv[i] = pieceItemBuilder.createPieceItem(new Piece(PieceType.PAWN, color));
    }
  }

  /**
   * Sets the remaining pieces in the upper inventory.
   *
   * @param pieceItemBuilder to build the piece items
   * @param upperInv the upper inventory of the player
   * @param color the color of the associated player
   */
  private static void setUpperInvRemainingPieces(
      PieceItemBuilder pieceItemBuilder, Inventory upperInv, PieceColor color) {
    PieceColor otherColor = PieceColor.getOtherColor(color);
    List<PieceType> pieceTypes =
        List.of(
            PieceType.ROOK,
            PieceType.KNIGHT,
            PieceType.BISHOP,
            color == PieceColor.WHITE ? PieceType.QUEEN : PieceType.KING,
            color == PieceColor.WHITE ? PieceType.KING : PieceType.QUEEN,
            PieceType.BISHOP,
            PieceType.KNIGHT,
            PieceType.ROOK);
    for (int i = 0; i < pieceTypes.size(); i++) {
      upperInv.setItem(
          i, pieceItemBuilder.createPieceItem(new Piece(pieceTypes.get(i), otherColor)));
    }
  }

  /**
   * Sets the time unit items in the upper inventory.
   *
   * @param chessGame the associated chess game
   * @param playerId the id of the associated player
   * @param upperInv the upper inventory of the player
   * @param timeUnitItemBuilder to build the time unit items
   */
  private static void setUpperInvTimeUnitItems(
      ChessGame chessGame,
      UUID playerId,
      Inventory upperInv,
      TimeUnitItemBuilder timeUnitItemBuilder) {
    long timeLeftMillis = chessGame.getTimeLeftMillis(playerId);

    int hours = (int) (timeLeftMillis / 3600000);
    long remaining = timeLeftMillis % 3600000;
    int minutes = (int) (remaining / 60000);
    remaining = remaining % 60000;
    int seconds = (int) (remaining / 1000);

    upperInv.setItem(8, timeUnitItemBuilder.createTimeUnitItem(TimeUnit.HOUR, hours));
    upperInv.setItem(17, timeUnitItemBuilder.createTimeUnitItem(TimeUnit.MINUTE, minutes));
    upperInv.setItem(26, timeUnitItemBuilder.createTimeUnitItem(TimeUnit.SECOND, seconds));
    upperInv.setItem(35, timeUnitItemBuilder.createTimeUnitItem(TimeUnit.HOUR, hours));
    upperInv.setItem(44, timeUnitItemBuilder.createTimeUnitItem(TimeUnit.MINUTE, minutes));
    upperInv.setItem(53, timeUnitItemBuilder.createTimeUnitItem(TimeUnit.SECOND, seconds));
  }

  /**
   * Sets the remaining pieces in the lower inventory.
   *
   * @param pieceItemBuilder to build piece items
   * @param lowerInv the lower inventory of the player as an ItemStack array
   * @param color the color of the associated player
   */
  private static void setLowerInvRemainingPieces(
      PieceItemBuilder pieceItemBuilder, ItemStack[] lowerInv, PieceColor color) {
    lowerInv[18] = pieceItemBuilder.createPieceItem(new Piece(PieceType.ROOK, color));
    lowerInv[25] = pieceItemBuilder.createPieceItem(new Piece(PieceType.ROOK, color));
    lowerInv[19] = pieceItemBuilder.createPieceItem(new Piece(PieceType.KNIGHT, color));
    lowerInv[24] = pieceItemBuilder.createPieceItem(new Piece(PieceType.KNIGHT, color));
    lowerInv[20] = pieceItemBuilder.createPieceItem(new Piece(PieceType.BISHOP, color));
    lowerInv[23] = pieceItemBuilder.createPieceItem(new Piece(PieceType.BISHOP, color));
    lowerInv[21] =
        pieceItemBuilder.createPieceItem(
            new Piece(color == PieceColor.WHITE ? PieceType.QUEEN : PieceType.KING, color));
    lowerInv[22] =
        pieceItemBuilder.createPieceItem(
            new Piece(color == PieceColor.WHITE ? PieceType.KING : PieceType.QUEEN, color));
  }

  /**
   * Sets the remaining lower inventory items such as info, surrender, draw items.
   *
   * @param lowerInv the lower inventory of the player as an ItemStack array
   * @param infoItemBuilder to build the info item
   */
  private static void setLowerInvRemainingItems(
      ItemStack[] lowerInv, ChessMenuItemBuilder infoItemBuilder) {
    lowerInv[17] = infoItemBuilder.createChessMenuItem(ChessMenuItemType.INFO);
    lowerInv[27] = infoItemBuilder.createChessMenuItem(ChessMenuItemType.SURRENDER);
    lowerInv[34] = infoItemBuilder.createChessMenuItem(ChessMenuItemType.DRAW);
  }
}
