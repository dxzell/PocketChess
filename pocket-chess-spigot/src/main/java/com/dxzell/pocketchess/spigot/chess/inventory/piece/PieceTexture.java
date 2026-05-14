package com.dxzell.pocketchess.spigot.chess.inventory.piece;

import com.dxzell.pocketchess.api.piece.Piece;
import com.dxzell.pocketchess.api.piece.PieceColor;
import lombok.Getter;

import javax.annotation.Nullable;

/** Represents the texture model data for chess pieces. */
@Getter
public enum PieceTexture {
  BASIC_WHITE_PAWN(1001),
  BASIC_WHITE_ROOK(1002),
  BASIC_WHITE_KNIGHT(1003),
  BASIC_WHITE_BISHOP(1004),
  BASIC_WHITE_QUEEN(1005),
  BASIC_WHITE_KING(1006),
  BASIC_BLACK_PAWN(1007),
  BASIC_BLACK_ROOK(1008),
  BASIC_BLACK_KNIGHT(1009),
  BASIC_BLACK_BISHOP(1010),
  BASIC_BLACK_QUEEN(1011),
  BASIC_BLACK_KING(1012),
  BASIC_WHITE_QUEEN_PROMOTION(1013),
  BASIC_BLACK_QUEEN_PROMOTION(1014),
  BASIC_WHITE_BISHOP_PROMOTION(1015),
  BASIC_BLACK_BISHOP_PROMOTION(1016),
  BASIC_WHITE_KNIGHT_PROMOTION(1017),
  BASIC_BLACK_KNIGHT_PROMOTION(1018),
  BASIC_WHITE_ROOK_PROMOTION(1019),
  BASIC_BLACK_ROOK_PROMOTION(1020),
  EMPTY_SELECTED(1),
  EMPTY_AVAILABLE(2);

  private final int modelData;

  PieceTexture(int modelData) {
    this.modelData = modelData;
  }

  /**
   * @param piece the chess piece
   * @return the model data for the selected piece
   */
  public static int getSelectedModelData(Piece piece) {
    return Integer.parseInt(fromPiece(piece).getModelData() + "1");
  }

  /**
   * @param piece the chess piece
   * @return the model data for the available piece
   */
  public static int getAvailableModelData(Piece piece) {
    return Integer.parseInt(fromPiece(piece).getModelData() + "2");
  }

  /**
   * @param piece the chess piece
   * @return the PieceTexture of that chess piece
   */
  public static PieceTexture fromPiece(Piece piece) {
    PieceColor color = piece.color();
    return switch (piece.type()) {
      case ROOK ->
          color == PieceColor.WHITE ? PieceTexture.BASIC_WHITE_ROOK : PieceTexture.BASIC_BLACK_ROOK;
      case KNIGHT ->
          color == PieceColor.WHITE
              ? PieceTexture.BASIC_WHITE_KNIGHT
              : PieceTexture.BASIC_BLACK_KNIGHT;
      case BISHOP ->
          color == PieceColor.WHITE
              ? PieceTexture.BASIC_WHITE_BISHOP
              : PieceTexture.BASIC_BLACK_BISHOP;
      case QUEEN ->
          color == PieceColor.WHITE
              ? PieceTexture.BASIC_WHITE_QUEEN
              : PieceTexture.BASIC_BLACK_QUEEN;
      case KING ->
          color == PieceColor.WHITE ? PieceTexture.BASIC_WHITE_KING : PieceTexture.BASIC_BLACK_KING;
      case PAWN ->
          color == PieceColor.WHITE ? PieceTexture.BASIC_WHITE_PAWN : PieceTexture.BASIC_BLACK_PAWN;
    };
  }

  /**
   * @param piece the promotion chess piece
   * @return the PieceTexture of that promotion chess piece
   */
  @Nullable
  public static PieceTexture fromPromotionPiece(Piece piece) {
    PieceColor color = piece.color();
    return switch (piece.type()) {
      case ROOK ->
              color == PieceColor.WHITE ? PieceTexture.BASIC_WHITE_ROOK_PROMOTION : PieceTexture.BASIC_BLACK_ROOK_PROMOTION;
      case KNIGHT ->
              color == PieceColor.WHITE
                      ? PieceTexture.BASIC_WHITE_KNIGHT_PROMOTION
                      : PieceTexture.BASIC_BLACK_KNIGHT_PROMOTION;
      case BISHOP ->
              color == PieceColor.WHITE
                      ? PieceTexture.BASIC_WHITE_BISHOP_PROMOTION
                      : PieceTexture.BASIC_BLACK_BISHOP_PROMOTION;
      case QUEEN ->
              color == PieceColor.WHITE
                      ? PieceTexture.BASIC_WHITE_QUEEN_PROMOTION
                      : PieceTexture.BASIC_BLACK_QUEEN_PROMOTION;
      default -> null;
    };
  }
}
