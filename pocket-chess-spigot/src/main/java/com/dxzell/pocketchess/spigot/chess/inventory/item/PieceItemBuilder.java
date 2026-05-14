package com.dxzell.pocketchess.spigot.chess.inventory.item;

import com.dxzell.pocketchess.api.piece.Piece;
import com.dxzell.pocketchess.api.piece.PieceColor;
import com.dxzell.pocketchess.api.piece.PieceType;
import com.dxzell.pocketchess.spigot.PocketChess;
import com.dxzell.pocketchess.spigot.chess.inventory.piece.PieceHighlightType;
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

/** Allows creation and highlighting of chess piece items. */
public final class PieceItemBuilder {

  private final PocketChess plugin;
  private static final Map<Piece, String> PIECE_KEYS =
      Map.ofEntries(
          Map.entry(new Piece(PieceType.PAWN, PieceColor.WHITE), "white_pawn"),
          Map.entry(new Piece(PieceType.PAWN, PieceColor.BLACK), "black_pawn"),
          Map.entry(new Piece(PieceType.ROOK, PieceColor.WHITE), "white_rook"),
          Map.entry(new Piece(PieceType.ROOK, PieceColor.BLACK), "black_rook"),
          Map.entry(new Piece(PieceType.BISHOP, PieceColor.WHITE), "white_bishop"),
          Map.entry(new Piece(PieceType.BISHOP, PieceColor.BLACK), "black_bishop"),
          Map.entry(new Piece(PieceType.KNIGHT, PieceColor.WHITE), "white_knight"),
          Map.entry(new Piece(PieceType.KNIGHT, PieceColor.BLACK), "black_knight"),
          Map.entry(new Piece(PieceType.KING, PieceColor.WHITE), "white_king"),
          Map.entry(new Piece(PieceType.KING, PieceColor.BLACK), "black_king"),
          Map.entry(new Piece(PieceType.QUEEN, PieceColor.WHITE), "white_queen"),
          Map.entry(new Piece(PieceType.QUEEN, PieceColor.BLACK), "black_queen"));

  @Inject
  public PieceItemBuilder(PocketChess plugin) {
    this.plugin = plugin;
  }

  /**
   * @param piece the requested chess piece
   * @return the item representing the requested chess piece
   */
  public ItemStack createPieceItem(Piece piece) {
    PieceTexture texture = PieceTexture.fromPiece(piece);
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
   * Puts a highlight on the requested piece item by changing the texture.
   *
   * @param item the piece item
   * @param highlightType the highlight type
   */
  public void highlightPieceItem(ItemStack item, PieceHighlightType highlightType) {
    Piece piece = fromItem(item);
    if (piece != null) {
      ItemMeta meta = item.getItemMeta();
      meta.setCustomModelData(
          highlightType == PieceHighlightType.SELECTED
              ? PieceTexture.getSelectedModelData(piece)
              : highlightType == PieceHighlightType.AVAILABLE
                  ? PieceTexture.getAvailableModelData(piece)
                  : PieceTexture.fromPiece(piece).getModelData());
      item.setItemMeta(meta);
    }
  }

  /**
   * @param highlightType the highlight type
   * @return a highlight texture for an empty slot
   */
  public ItemStack getEmptyHighlight(PieceHighlightType highlightType) {
    if (highlightType == PieceHighlightType.NONE) {
      throw new IllegalArgumentException(
          "There is no texture for 'no highlight'. In that case just remove the item from the slot.");
    }

    PieceTexture texture =
        highlightType == PieceHighlightType.SELECTED
            ? PieceTexture.EMPTY_SELECTED
            : PieceTexture.EMPTY_AVAILABLE;
    int modelData = texture.getModelData();

    ItemStack emptySquareItem = new ItemStack(Material.PAPER);
    ItemMeta emptySquareMeta = emptySquareItem.getItemMeta();
    emptySquareMeta.setHideTooltip(true);
    emptySquareMeta.setCustomModelData(modelData);
    emptySquareMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

    emptySquareItem.setItemMeta(emptySquareMeta);

    return emptySquareItem;
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
   * Gets the piece from the given NamespacedKey, or null when key matches no piece.
   *
   * @param key the NamespacedKey
   * @return the piece associated to the key
   */
  @Nullable
  private Piece fromNamespacedKey(NamespacedKey key) {
    for (Map.Entry<Piece, String> entry : PIECE_KEYS.entrySet()) {
      if (entry.getValue().equals(key.getKey())) {
        return entry.getKey();
      }
    }

    return null;
  }

  /**
   * Creates a new NamespacedKey for the given piece
   *
   * @param piece the piece to create the key for
   * @return the NamespacedKey for that specific piece
   */
  private NamespacedKey getPieceKey(Piece piece) {
    return new NamespacedKey(plugin, PIECE_KEYS.get(piece));
  }
}
