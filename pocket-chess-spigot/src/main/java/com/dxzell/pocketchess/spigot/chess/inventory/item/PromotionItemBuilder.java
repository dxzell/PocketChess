package com.dxzell.pocketchess.spigot.chess.inventory.item;

import com.dxzell.pocketchess.api.piece.Piece;
import com.dxzell.pocketchess.api.piece.PieceColor;
import com.dxzell.pocketchess.api.piece.PieceType;
import com.dxzell.pocketchess.spigot.PocketChess;
import com.dxzell.pocketchess.spigot.chess.inventory.piece.PieceTexture;
import com.google.inject.Inject;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.Map;

/** Allows creation of promotion chess piece items. */
public class PromotionItemBuilder {

  private final PocketChess plugin;
  private final static Map<Piece, String> PROMOTION_PIECE_KEYS =
      Map.ofEntries(
          Map.entry(new Piece(PieceType.ROOK, PieceColor.WHITE), "white_rook_promotion"),
          Map.entry(new Piece(PieceType.ROOK, PieceColor.BLACK), "black_rook_promotion"),
          Map.entry(new Piece(PieceType.BISHOP, PieceColor.WHITE), "white_bishop_promotion"),
          Map.entry(new Piece(PieceType.BISHOP, PieceColor.BLACK), "black_bishop_promotion"),
          Map.entry(new Piece(PieceType.KNIGHT, PieceColor.WHITE), "white_knight_promotion"),
          Map.entry(new Piece(PieceType.KNIGHT, PieceColor.BLACK), "black_knight_promotion"),
          Map.entry(new Piece(PieceType.QUEEN, PieceColor.WHITE), "white_queen_promotion"),
          Map.entry(new Piece(PieceType.QUEEN, PieceColor.BLACK), "black_queen_promotion"));

  @Inject
  public PromotionItemBuilder(PocketChess plugin) {
    this.plugin = plugin;
  }

  /**
   * @param piece the requested piece to get the promotion of
   * @return the item representing the requested promotion piece
   */
  public ItemStack createPromotionItem(Piece piece) {
    PieceTexture texture = PieceTexture.fromPromotionPiece(piece);

    if (texture == null) {
      throw new IllegalArgumentException("King and Pawn have no promotion texture.");
    }
    int modelData = texture.getModelData();

    ItemStack pieceItem = new ItemStack(Material.PAPER);
    ItemMeta pieceMeta = pieceItem.getItemMeta();
    pieceMeta.setCustomModelData(modelData);
    pieceMeta.setHideTooltip(true);
    pieceMeta
        .getPersistentDataContainer()
        .set(getPieceKey(piece), PersistentDataType.BOOLEAN, true);
    pieceMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

    pieceItem.setItemMeta(pieceMeta);

    return pieceItem;
  }

  /**
   * @param item the requested item from the chess inventory
   * @return the piece, or null if no piece was clicked
   */
  @Nullable
  public Piece fromItem(ItemStack item) {
    if (item == null || !item.hasItemMeta()) {
      return null;
    }

    for (NamespacedKey key : item.getItemMeta().getPersistentDataContainer().getKeys()) {
      Piece piece = fromNamespacedKey(key);
      if (piece != null) {
        return piece;
      }
    }

    return null;
  }

  /**
   * Gets the piece from the given NamespacedKey, or null when key matches no promotion piece.
   *
   * @param key the NamespacedKey
   * @return the piece associated to the key
   */
  @Nullable
  private Piece fromNamespacedKey(NamespacedKey key) {
    for (Map.Entry<Piece, String> entry : PROMOTION_PIECE_KEYS.entrySet()) {
      if (entry.getValue().equals(key.getKey())) {
        return entry.getKey();
      }
    }

    return null;
  }

  /**
   * Creates a new NamespacedKey for the given promotion piece
   *
   * @param piece the promotion piece to create the key for
   * @return the NamespacedKey for that specific promotion piece
   */
  private NamespacedKey getPieceKey(Piece piece) {
    return new NamespacedKey(plugin, PROMOTION_PIECE_KEYS.get(piece));
  }
}
