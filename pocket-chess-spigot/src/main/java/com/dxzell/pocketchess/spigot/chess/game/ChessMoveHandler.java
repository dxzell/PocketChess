package com.dxzell.pocketchess.spigot.chess.game;

import com.dxzell.pocketchess.api.board.ChessBoard;
import com.dxzell.pocketchess.api.board.Square;
import com.dxzell.pocketchess.api.game.ChessGame;
import com.dxzell.pocketchess.api.move.Move;
import com.dxzell.pocketchess.api.move.MoveCalculator;
import com.dxzell.pocketchess.api.move.MoveResult;
import com.dxzell.pocketchess.api.piece.Piece;
import com.dxzell.pocketchess.api.piece.PieceColor;
import com.dxzell.pocketchess.api.piece.PieceType;
import com.dxzell.pocketchess.common.board.ChessBoardImpl;
import com.dxzell.pocketchess.common.board.SquareUtils;
import com.dxzell.pocketchess.spigot.chess.inventory.item.PromotionItemBuilder;
import com.dxzell.pocketchess.spigot.config.MessageConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles successful moves by updating the chess inventories and the chess games state properly.
 */
public final class ChessMoveHandler {

  private final SpigotChessGame spigotChessGame;
  private final ChessGame chessGame;
  private final MoveCalculator moveCalculator;
  private final ChessInventoryManager inventoryManager;
  private Move whitePromotionMove;
  private Move blackPromotionMove;

  public ChessMoveHandler(
      SpigotChessGame spigotChessGame,
      ChessGame chessGame,
      MoveCalculator moveCalculator,
      ChessInventoryManager inventoryManager) {
    this.spigotChessGame = spigotChessGame;
    this.moveCalculator = moveCalculator;
    this.chessGame = chessGame;
    this.inventoryManager = inventoryManager;
  }

  /**
   * Updates the chess inventories and the chess games state.
   *
   * @param moveResult the result of the played move
   * @param move the played move
   * @param playerId the id of the player who made the move
   * @param messageConfig to get custom messages
   */
  public void handleSuccessfulMove(
      MoveResult moveResult, Move move, UUID playerId, MessageConfig messageConfig) {
    if (moveResult.checkmate()) {
      chessGame.endGame(playerId);
      spigotChessGame.endGame(playerId);
    } else if (moveResult.promotion()) {
      handlePromotion(moveResult, move.from(), move.to(), playerId);
      inventoryManager.updateInfo(messageConfig.getPickPromotion(), playerId);
    } else {
      inventoryManager.updateInventoryChessBoard(move);

      handleCastling(moveResult, move.to());

      handleEnPassant(moveResult, move.to(), playerId);

      finishMove(move.to());
    }
  }

  /**
   * Updates the inventory with an additional move if castle was played.
   *
   * @param moveResult the result of the played move
   * @param destination the square the piece was moved to
   */
  private void handleCastling(MoveResult moveResult, Square destination) {
    if (moveResult.castling()) {
      Square rookFrom;
      Square rookTo;

      if (destination.getColumnIndex() == 6) { // King side castling
        rookFrom = new Square(destination.row(), 'H');
        rookTo = new Square(destination.row(), 'F');
      } else { // Queen side castling
        rookFrom = new Square(destination.row(), 'A');
        rookTo = new Square(destination.row(), 'D');
      }

      inventoryManager.updateInventoryChessBoard(new Move(null, rookFrom, rookTo));
    }
  }

  /**
   * Removes the piece item below the pawn if en passant was played.
   *
   * @param moveResult the result of the played move
   * @param toSquare the square the piece has moved to
   * @param playerId the player who made the move
   */
  private void handleEnPassant(MoveResult moveResult, Square toSquare, UUID playerId) {
    if (moveResult.enPassant()) {
      inventoryManager.removePieceFromInventories(
          SquareUtils.offsetOrNull(
              toSquare, chessGame.getColor(playerId) == PieceColor.WHITE ? -1 : 1, 0));
    }
  }

  /**
   * Removes the piece item from the destination square.
   *
   * @param moveResult the result of the played move
   * @param fromSquare the square the piece has moved from
   * @param toSquare the square the piece has moved to
   * @param playerId the player who made the move
   */
  private void handlePromotion(
      MoveResult moveResult, Square fromSquare, Square toSquare, UUID playerId) {
    if (moveResult.promotion()) {
      Move move = new Move(null, fromSquare, toSquare);
      if (chessGame.getColor(playerId) == PieceColor.WHITE) {
        whitePromotionMove = move;
      } else {
        blackPromotionMove = move;
      }

      inventoryManager.addPromotionPieceItems(playerId);
    }
  }

  public boolean handlePromotionSelection(
      ChessBoardImpl chessBoard,
      ChessInventoryManager inventoryManager,
      PromotionItemBuilder promotionItemBuilder,
      ItemStack item,
      int clickedSlot,
      UUID playerId) {
    Piece promotionPiece = promotionItemBuilder.fromItem(item);
    if (clickedSlot >= 29 && clickedSlot <= 32 && promotionPiece != null) {
      PieceColor color = promotionPiece.color();
      Square fromSquare =
          color == PieceColor.WHITE ? whitePromotionMove.from() : blackPromotionMove.from();
      Square toSquare =
          color == PieceColor.WHITE ? whitePromotionMove.to() : blackPromotionMove.to();

      chessBoard.setPiece(fromSquare, null);
      chessBoard.setPiece(toSquare, promotionPiece);

      inventoryManager.removePieceFromInventories(fromSquare);
      inventoryManager.setPieceInInventories(toSquare, promotionPiece);

      chessBoard.setLastPlayedMove(new Move(null, fromSquare, toSquare));

      inventoryManager.removePromotionPieceItems(playerId);

      finishMove(toSquare);

      return true;
    }
    return false;
  }

  /**
   * Puts the game in a fresh state, which allows the other player to make a move.
   *
   * @param destination the square the piece was moved to
   */
  private void finishMove(Square destination) {
    if (isDraw(chessGame.getChessBoard()) || isStalemate()) {
      spigotChessGame.endGame(null);
      return;
    }

    if (isCheckmate(
        chessGame.getCurrentTurn().equals(chessGame.getWhitePlayerId())
            ? chessGame.getBlackPlayerId()
            : chessGame.getWhitePlayerId())) {
      spigotChessGame.endGame(chessGame.getCurrentTurn());
      return;
    }

    UUID currentTurn = chessGame.getCurrentTurn();
    UUID otherPlayer = spigotChessGame.getOtherPlayerId(currentTurn);

    if (isInCheck(otherPlayer)) {
      spigotChessGame.getSoundPlayer().playCheckMoveSound(otherPlayer, currentTurn);
    } else {
      spigotChessGame.getSoundPlayer().playMoveSound(currentTurn, otherPlayer);
    }

    if (!inventoryManager.hasInventoryOpen(otherPlayer)) {
      sendMessage(otherPlayer, spigotChessGame.getMessageConfig().getOpponentMoved());
    }

    inventoryManager.unhighlightSelectedPiece(destination);

    inventoryManager.resetAvailableMoves();

    inventoryManager.highlightLastPlayedMove();

    inventoryManager.highlightOtherColorsAvailableMoves(chessGame);

    inventoryManager.updateInventoryTime(chessGame.getTimeLeftMillis(currentTurn));

    chessGame.toggleTurn();

    inventoryManager.unhighlightInventoryTimeTexture(chessGame.getCurrentTurn());

    inventoryManager.cancelInfoTasks();

    inventoryManager.resetInfo();

    spigotChessGame.getDrawHandler().resetTimestamps();
  }

  /**
   * Sends a message to the given player.
   *
   * @param playerId the id of the player
   * @param message the message to send
   */
  private void sendMessage(UUID playerId, String message) {
    Player player = Bukkit.getPlayer(playerId);
    if (player != null) {
      player.sendMessage(message);
    }
  }

  /**
   * @param chessBoard the chess board that is being played on
   * @return whether the current chess board state is a draw
   */
  private boolean isDraw(ChessBoard chessBoard) {
    return isDraw(chessBoard, List.of(PieceType.KING), List.of(PieceType.KING))
        || isDraw(chessBoard, List.of(PieceType.KING, PieceType.BISHOP), List.of(PieceType.KING))
        || isDraw(chessBoard, List.of(PieceType.KING), List.of(PieceType.KING, PieceType.BISHOP))
        || isDraw(chessBoard, List.of(PieceType.KING, PieceType.KNIGHT), List.of(PieceType.KING))
        || isDraw(chessBoard, List.of(PieceType.KING), List.of(PieceType.KING, PieceType.KNIGHT));
  }

  /**
   * @param chessBoard the chess board that is being played on
   * @param whiteDrawPieceTypes the white piece types needed for a draw position
   * @param blackDrawPieceTypes the black piece types needed for a draw position
   * @return whether the current chess board state is a draw
   */
  private boolean isDraw(
      ChessBoard chessBoard,
      List<PieceType> whiteDrawPieceTypes,
      List<PieceType> blackDrawPieceTypes) {

    return hasExactlyPieces(chessBoard, PieceColor.WHITE, whiteDrawPieceTypes)
        && hasExactlyPieces(chessBoard, PieceColor.BLACK, blackDrawPieceTypes);
  }

  /**
   * @param chessBoard the chess board that is being played on
   * @param color the color of the player of the given piece types
   * @param expectedPieceTypes the expected piece types for a draw
   * @return whether the given color pieces are exactly the given expected piece types
   */
  private boolean hasExactlyPieces(
      ChessBoard chessBoard, PieceColor color, List<PieceType> expectedPieceTypes) {
    List<Square> pieceSquares = chessBoard.getColoredPieces(color);
    List<PieceType> remainingExpectedPieceTypes = new ArrayList<>(expectedPieceTypes);
    for (Square pieceSquare : pieceSquares) {
      Piece piece = chessBoard.getPiece(pieceSquare);

      if (piece == null) {
        continue;
      }

      boolean removed = remainingExpectedPieceTypes.remove(piece.type());

      if (!removed) {
        return false;
      }
    }

    return remainingExpectedPieceTypes.isEmpty();
  }

  /**
   * @return whether the chess board is in a stalemate position which follows up to a draw
   */
  private boolean isStalemate() {
    UUID playerId =
        chessGame.getCurrentTurn() == chessGame.getWhitePlayerId()
            ? chessGame.getBlackPlayerId()
            : chessGame.getWhitePlayerId();
    return !hasPossibleMoves(playerId) && !isInCheck(playerId);
  }

  /**
   * @param playerId the id of the player
   * @return whether the given player has been checkmated
   */
  private boolean isCheckmate(UUID playerId) {
    return !hasPossibleMoves(playerId) && isInCheck(playerId);
  }

  /**
   * @param playerId the id of the player
   * @return whether the given player is in check or not
   */
  private boolean isInCheck(UUID playerId) {
    PieceColor playerColor = chessGame.getColor(playerId);
    PieceColor enemyColor = PieceColor.getOtherColor(playerColor);

    Square kingSquare =
        chessGame.getChessBoard().getColoredPieces(playerColor, PieceType.KING).get(0);

    for (Square pieceSquare :
        chessGame.getChessBoard().getColoredPieces(enemyColor, PieceType.values())) {
      for (Square targetSquare :
          moveCalculator.getRawMoves(chessGame.getChessBoard(), pieceSquare)) {
        if (targetSquare.equals(kingSquare)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @param playerId the id of the player
   * @return whether the player still has available moves to play
   */
  private boolean hasPossibleMoves(UUID playerId) {
    for (Square square :
        chessGame
            .getChessBoard()
            .getColoredPieces(chessGame.getColor(playerId), PieceType.values())) {
      if (!moveCalculator.getPossibleMoves(chessGame, square).isEmpty()) {
        return true;
      }
    }
    return false;
  }
}
