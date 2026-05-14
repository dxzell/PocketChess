package com.dxzell.pocketchess.common.move.calculator;

import com.dxzell.pocketchess.api.board.ChessBoard;
import com.dxzell.pocketchess.api.board.Square;
import com.dxzell.pocketchess.api.piece.Piece;
import com.dxzell.pocketchess.common.move.calculator.type.LineBasedPieceMoveCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates possible rook moves.
 */
public final class RookMoveCalculator extends LineBasedPieceMoveCalculator {

  @Override
  public List<Square> getMoves(ChessBoard board, Square pieceSquare, Piece rook) {
    List<Square> squares = new ArrayList<>();

    addVerticalLineSquares(board, squares, pieceSquare, rook.color());
    addHorizontalLineSquares(board, squares, pieceSquare, rook.color());

    return squares;
  }
}
