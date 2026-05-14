package com.dxzell.pocketchess.common.move.calculator;

import com.dxzell.pocketchess.api.board.ChessBoard;
import com.dxzell.pocketchess.api.board.Square;
import com.dxzell.pocketchess.api.piece.Piece;
import com.dxzell.pocketchess.api.piece.PieceColor;
import com.dxzell.pocketchess.common.board.SquareUtils;
import com.dxzell.pocketchess.common.move.calculator.type.PieceMoveCalculator;

import java.util.ArrayList;
import java.util.List;

/** Calculates possible king moves. */
public final class KingMoveCalculator extends PieceMoveCalculator {

  @Override
  public List<Square> getMoves(ChessBoard board, Square pieceSquare, Piece king) {
    List<Square> squares = new ArrayList<>();

    addKingMoves(squares, pieceSquare);

    return filterOutInvalidSquares(squares, board, king.color());
  }

  /**
   * Calculates the possible king squares and adds them to the list of possible moves.
   *
   * @param squares the list of possible moves
   * @param pieceSquare the square of the king
   */
  private void addKingMoves(List<Square> squares, Square pieceSquare) {
    squares.add(SquareUtils.offsetOrNull(pieceSquare, 0, 1));
    squares.add(SquareUtils.offsetOrNull(pieceSquare, 1, 0));
    squares.add(SquareUtils.offsetOrNull(pieceSquare, 1, 1));
    squares.add(SquareUtils.offsetOrNull(pieceSquare, -1, 1));
    squares.add(SquareUtils.offsetOrNull(pieceSquare, 1, -1));
    squares.add(SquareUtils.offsetOrNull(pieceSquare, -1, -1));
    squares.add(SquareUtils.offsetOrNull(pieceSquare, 0, -1));
    squares.add(SquareUtils.offsetOrNull(pieceSquare, -1, 0));
  }

  /**
   * Filters out every square that is already occupied by a piece of the same color.
   *
   * @param squares the list of possible moves
   * @param board the associated chess board
   * @param kingColor the color of the king
   * @return the filtered list of possible moves
   */
  private List<Square> filterOutInvalidSquares(
      List<Square> squares, ChessBoard board, PieceColor kingColor) {
    return squares.stream()
        .filter(
            targetSquare ->
                targetSquare != null
                    && (board.getPiece(targetSquare) == null
                        || board.getPiece(targetSquare).color() != kingColor))
        .toList();
  }
}
