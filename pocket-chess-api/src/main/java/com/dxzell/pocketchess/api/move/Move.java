package com.dxzell.pocketchess.api.move;

import com.dxzell.pocketchess.api.board.Square;
import com.dxzell.pocketchess.api.piece.Piece;

/**
 * Represents a played move.
 *
 * @param piece the piece that was moved
 * @param from the old position
 * @param to the new position
 */
public record Move(Piece piece, Square from, Square to) {}
