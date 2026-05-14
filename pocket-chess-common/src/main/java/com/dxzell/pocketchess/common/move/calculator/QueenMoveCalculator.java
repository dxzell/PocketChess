package com.dxzell.pocketchess.common.move.calculator;

import com.dxzell.pocketchess.api.board.ChessBoard;
import com.dxzell.pocketchess.api.board.Square;
import com.dxzell.pocketchess.api.piece.Piece;
import com.dxzell.pocketchess.common.move.calculator.type.LineBasedPieceMoveCalculator;

import java.util.ArrayList;
import java.util.List;

/** Calculates possible queen moves. */
public final class QueenMoveCalculator extends LineBasedPieceMoveCalculator {

  @Override
  public List<Square> getMoves(ChessBoard board, Square pieceSquare, Piece queen) {
    List<Square> squares = new ArrayList<>();

    addVerticalLineSquares(board, squares, pieceSquare, queen.color());
    addHorizontalLineSquares(board, squares, pieceSquare, queen.color());
    addDiagonalLineSquares(board, squares, pieceSquare, queen.color());

    return squares;
  }
}
