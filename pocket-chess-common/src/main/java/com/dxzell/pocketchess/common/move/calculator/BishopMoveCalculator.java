package com.dxzell.pocketchess.common.move.calculator;

import com.dxzell.pocketchess.api.board.ChessBoard;
import com.dxzell.pocketchess.api.board.Square;
import com.dxzell.pocketchess.api.piece.Piece;
import com.dxzell.pocketchess.common.move.calculator.type.LineBasedPieceMoveCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates possible bishop moves.
 */
public final class BishopMoveCalculator extends LineBasedPieceMoveCalculator {

    @Override
    public List<Square> getMoves(ChessBoard board, Square pieceSquare, Piece bishop) {
        List<Square> squares = new ArrayList<>();

        addDiagonalLineSquares(board, squares, pieceSquare, bishop.color());

        return squares;
    }
}
