package com.dxzell.pocketchess.spigot.chess.inventory;

import com.dxzell.pocketchess.api.board.ChessBoard;
import com.dxzell.pocketchess.api.board.Square;
import com.dxzell.pocketchess.api.move.Move;
import com.dxzell.pocketchess.api.move.MoveCalculator;
import com.dxzell.pocketchess.api.piece.Piece;
import com.dxzell.pocketchess.api.piece.PieceColor;
import com.dxzell.pocketchess.spigot.chess.game.SpigotChessGame;
import com.dxzell.pocketchess.spigot.chess.inventory.item.DrawItemType;
import com.dxzell.pocketchess.spigot.chess.inventory.piece.PieceHighlightType;
import com.dxzell.pocketchess.spigot.chess.inventory.item.PieceItemBuilder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Handles piece and square highlights in a chess inventory. */
public final class ChessInventoryHighlighter {

  private final SpigotChessGame spigotChessGame;
  private final ChessInventory chessInventory;
  private final ChessInventoryUpdater chessInventoryUpdater;
  private final ChessBoard chessBoard;
  private final PieceItemBuilder pieceItemBuilder;
  private final MoveCalculator moveCalculator;
  private final UUID playerId;
  private final PieceColor color;
  private Move lastPlayedMove;
  private List<Square> availableSquares = new ArrayList<>();

  public ChessInventoryHighlighter(
      SpigotChessGame spigotChessGame,
      ChessInventory chessInventory,
      ChessInventoryUpdater chessInventoryUpdater,
      ChessBoard chessBoard,
      PieceItemBuilder pieceItemBuilder,
      MoveCalculator moveCalculator,
      UUID playerId,
      PieceColor color) {
    this.spigotChessGame = spigotChessGame;
    this.chessInventory = chessInventory;
    this.chessInventoryUpdater = chessInventoryUpdater;
    this.chessBoard = chessBoard;
    this.pieceItemBuilder = pieceItemBuilder;
    this.moveCalculator = moveCalculator;
    this.playerId = playerId;
    this.color = color;
  }

  /**
   * Highlights the selected piece.
   *
   * @param beforeSelectedSquare the square of the piece that was selected before the new selection,
   *     or null if not existing
   * @param selectedSquare the square of the newly selected piece
   */
  public void highlightSelectedPiece(@Nullable Square beforeSelectedSquare, Square selectedSquare) {
    unhighlightSelectedPiece(beforeSelectedSquare);

    pieceItemBuilder.highlightPieceItem(
        ChessInventoryUtil.getPieceItemFromSquare(chessInventory, selectedSquare, color),
        PieceHighlightType.SELECTED);
    chessInventoryUpdater.updateLowerPart();
  }

  /**
   * Highlights all available destination squares of the piece on the given square.
   *
   * @param square the square of the piece the available destination squares are being shown of
   */
  public void highlightAvailableMoves(Square square) {
    if (square == null) {
      return;
    }

    Piece piece = chessBoard.getPiece(square);
    if (piece == null || piece.color() != color) {
      spigotChessGame.getChessGame().unselectPiece(playerId);
      unhighlightAvailableMoves();
      return;
    }

    unhighlightAvailableMoves();
    availableSquares =
        new ArrayList<>(moveCalculator.getPossibleMoves(spigotChessGame.getChessGame(), square));

    for (Square availableSquare : availableSquares) {
      highlightSquare(availableSquare, PieceHighlightType.AVAILABLE);
    }

    chessInventoryUpdater.updateLowerPart();
  }

  /**
   * Highlights the draw item of the chess inventory.
   * @param drawItemType the type of the draw item
   */
  public void highlightDrawItem(DrawItemType drawItemType) {
    chessInventory.getChessMenuItemBuilder().highlightDrawItem(chessInventoryUpdater, chessInventory, drawItemType);
  }

  /**
   * Highlights the surrender item of the chess inventory.
   *
   * @param highlight whether the item should be highlighted or unhighlighted
   */
  public void highlightSurrenderItem(boolean highlight) {
    chessInventory.getChessMenuItemBuilder().highlightSurrenderItem(chessInventoryUpdater, chessInventory, highlight);
  }

  /**
   * Highlights a square slot in the chess inventory.
   *
   * @param square the square to highlight
   */
  private void highlightSquare(Square square, PieceHighlightType highlightType) {
    ChessInventorySlot targetSlot = ChessInventoryUtil.toInventorySlot(square, color);
    Piece squarePiece = chessBoard.getPiece(square);
    ItemStack pieceItem = ChessInventoryUtil.getPieceItemFromSquare(chessInventory, square, color);
    if (targetSlot.part() == ChessInventoryPart.LOWER) {
      highlightLowerInvSquare(squarePiece, pieceItem, targetSlot, highlightType);
    } else {
      highlightUpperInvSquare(squarePiece, pieceItem, targetSlot, highlightType);
    }
  }

  /**
   * Highlights a square slot in the lower inventory.
   *
   * @param targetPiece the piece to highlight, or null if square is empty
   * @param targetPieceItem the item of the piece, or null if square is empty
   * @param targetSlot the slot corresponding to the targeted square
   * @param highlightType the highlight color
   */
  private void highlightLowerInvSquare(
      @Nullable Piece targetPiece,
      @Nullable ItemStack targetPieceItem,
      ChessInventorySlot targetSlot,
      PieceHighlightType highlightType) {
    if (targetPiece != null) {
      pieceItemBuilder.highlightPieceItem(targetPieceItem, highlightType);
    } else {
      chessInventory.getLowerInv()[targetSlot.slot()] = highlightType != PieceHighlightType.NONE ?
          pieceItemBuilder.getEmptyHighlight(highlightType) : null;
    }
  }

  /**
   * Highlights a square slot in the upper inventory.
   *
   * @param targetPiece the piece to highlight, or null if square is empty
   * @param targetPieceItem the item of the piece, or null if square is empty
   * @param targetSlot the slot corresponding to the targeted square
   * @param highlightType the highlight color
   */
  private void highlightUpperInvSquare(
      @Nullable Piece targetPiece,
      @Nullable ItemStack targetPieceItem,
      ChessInventorySlot targetSlot,
      PieceHighlightType highlightType) {
    if (targetPiece != null) {
      pieceItemBuilder.highlightPieceItem(targetPieceItem, highlightType);
    } else {
      chessInventory
          .getUpperInv()
          .setItem(targetSlot.slot(), highlightType != PieceHighlightType.NONE ? pieceItemBuilder.getEmptyHighlight(highlightType) : null);
    }
    chessInventoryUpdater.updateLowerPart();
  }

  /** Highlights both start and destination square from the last played move. */
  public void highlightLastPlayedMove() {
    if (chessBoard.getLastPlayedMove() != null) {
      unhighlightSecondLastPlayedMove();
      lastPlayedMove = chessBoard.getLastPlayedMove();
      highlightSquare(lastPlayedMove.from(), PieceHighlightType.SELECTED);
      highlightSquare(lastPlayedMove.to(), PieceHighlightType.SELECTED);
    }
  }

  /** Unhighlights both start and destination square from the second last played move (because now a new move was played). */
  public void unhighlightSecondLastPlayedMove() {
    if (lastPlayedMove != null) {
      highlightSquare(lastPlayedMove.from(), PieceHighlightType.NONE);
      highlightSquare(lastPlayedMove.to(), PieceHighlightType.NONE);
    }
  }

  /**
   * Unhighlights the piece on the given square.
   *
   * @param square the square to unhighlight
   */
  public void unhighlightSelectedPiece(Square square) {
    if (square != null) {
      pieceItemBuilder.highlightPieceItem(
          ChessInventoryUtil.getPieceItemFromSquare(chessInventory, square, color),
          PieceHighlightType.NONE);
      highlightLastPlayedMove();
      chessInventoryUpdater.updateLowerPart();
    }
  }

  /** Unhighlights the saved destination squares. */
  public void unhighlightAvailableMoves() {
    availableSquares.forEach(
        square -> {
          ItemStack item = ChessInventoryUtil.getPieceItemFromSquare(chessInventory, square, color);
          ChessInventorySlot chessInventorySlot = ChessInventoryUtil.toInventorySlot(square, color);

          if (chessBoard.getPiece(square) == null) {
            unhighlightEmptySquare(chessInventorySlot, item);
          } else {
            pieceItemBuilder.highlightPieceItem(item, PieceHighlightType.NONE);
          }
        });
    highlightLastPlayedMove();
    chessInventoryUpdater.updateLowerPart();
    availableSquares.clear();
  }

  /**
   * Unhighlights an empty square by removing the empty textured item.
   *
   * @param chessInventorySlot slot corresponding to the square to unhighlight
   * @param emptyItem the item to remove
   */
  private void unhighlightEmptySquare(ChessInventorySlot chessInventorySlot, ItemStack emptyItem) {
    if (chessInventorySlot.part() == ChessInventoryPart.UPPER) {
      chessInventory.getUpperInv().remove(emptyItem);
    } else {
      chessInventory.getLowerInv()[chessInventorySlot.slot()] = null;
    }
    highlightLastPlayedMove();
  }
}
