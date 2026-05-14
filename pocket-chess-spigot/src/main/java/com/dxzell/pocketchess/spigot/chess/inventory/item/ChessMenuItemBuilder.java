package com.dxzell.pocketchess.spigot.chess.inventory.item;

import com.dxzell.pocketchess.spigot.PocketChess;
import com.dxzell.pocketchess.spigot.chess.inventory.ChessInventory;
import com.dxzell.pocketchess.spigot.chess.inventory.ChessInventoryUpdater;
import com.dxzell.pocketchess.spigot.config.MessageConfig;
import com.google.inject.Inject;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Allows creation and highlighting of chess menu items. */
public final class ChessMenuItemBuilder {

  private final PocketChess plugin;
  private final MessageConfig messageConfig;

  private static final Map<ChessMenuItemType, String> CHESS_MENU_KEYS =
      Map.of(
          ChessMenuItemType.INFO,
          "info",
          ChessMenuItemType.SURRENDER,
          "surrender",
          ChessMenuItemType.DRAW,
          "draw");

  @Inject
  public ChessMenuItemBuilder(PocketChess plugin, MessageConfig messageConfig) {
    this.plugin = plugin;
    this.messageConfig = messageConfig;
  }

  /**
   * @param menuType the chess menu item type
   * @return the chess menu item
   */
  public ItemStack createChessMenuItem(ChessMenuItemType menuType) {
    int modelData = menuType.getModelData();
    String itemName =
        menuType == ChessMenuItemType.INFO
            ? messageConfig.getInfoItemName()
            : menuType == ChessMenuItemType.SURRENDER
                ? messageConfig.getSurrenderItemName()
                : messageConfig.getDrawItemName();

    ItemStack chessMenuItem = new ItemStack(Material.PAPER);
    ItemMeta chessMenuItemMeta = chessMenuItem.getItemMeta();
    chessMenuItemMeta.setCustomModelData(modelData);
    chessMenuItemMeta
        .getPersistentDataContainer()
        .set(
            new NamespacedKey(plugin, CHESS_MENU_KEYS.get(menuType)),
            PersistentDataType.BOOLEAN,
            true);
    chessMenuItemMeta.setDisplayName(itemName);
    chessMenuItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

    chessMenuItem.setItemMeta(chessMenuItemMeta);

    return chessMenuItem;
  }

  /**
   * @param menuType the chess menu item type
   * @param chessInventory the chess inventory of the player
   * @return the chess menu item from the chess inventory
   */
  public ItemStack getChessMenuItem(ChessMenuItemType menuType, ChessInventory chessInventory) {
    if (chessInventory == null) {
      throw new IllegalStateException(
          "The given ChessInventory is null and this method therefore cannot run properly.");
    }
    return menuType == ChessMenuItemType.INFO
        ? chessInventory.getLowerInv()[17]
        : menuType == ChessMenuItemType.SURRENDER
            ? chessInventory.getLowerInv()[27]
            : chessInventory.getLowerInv()[34];
  }

  /**
   * @param item the clicked item
   * @param menuType the chess menu type that the item should be
   * @return whether the given clicked item is a chess menu item from the given type
   */
  public boolean isChessMenuItem(ItemStack item, ChessMenuItemType menuType) {
    return item != null
        && item.hasItemMeta()
        && item.getItemMeta()
            .getPersistentDataContainer()
            .has(new NamespacedKey(plugin, CHESS_MENU_KEYS.get(menuType)));
  }

  /**
   * Changes the chess menu item texture to normal (unhighlighted) or golden (highlighted).
   *
   * @param chessInventory the chess inventory of the player
   * @param inventoryUpdater the inventory updater for visual updates
   * @param highlight whether the item should be highlighted or unhighlighted
   */
  public void highlightChessMenuItem(
      ChessInventoryUpdater inventoryUpdater,
      ChessInventory chessInventory,
      ChessMenuItemType menuType,
      boolean highlight) {

    if (menuType == ChessMenuItemType.DRAW) {
      throw new IllegalArgumentException(
          "Use the highlightDrawItem() method to highlight the draw item.");
    }

    ItemStack chessMenuItem = getChessMenuItem(menuType, chessInventory);
    ItemMeta chessMenuItemMeta = chessMenuItem.getItemMeta();

    int currentModelData = chessMenuItemMeta.getCustomModelData();
    int baseModelData = currentModelData >= 100 ? currentModelData / 10 : currentModelData;

    int newModelData = highlight ? baseModelData * 10 + 1 : baseModelData;

    chessMenuItemMeta.setCustomModelData(newModelData);
    chessMenuItem.setItemMeta(chessMenuItemMeta);
    inventoryUpdater.updateLowerPart();
  }

  /**
   * Changes the draw item texture to the texture of the given draw item type.
   *
   * @param chessInventory the chess inventory of the player
   * @param inventoryUpdater the inventory updater for visual updates
   * @param drawItemType the type of the draw item
   */
  public void highlightDrawItem(
      ChessInventoryUpdater inventoryUpdater,
      ChessInventory chessInventory,
      DrawItemType drawItemType) {

    ItemStack chessMenuItem = getChessMenuItem(ChessMenuItemType.DRAW, chessInventory);
    ItemMeta chessMenuItemMeta = chessMenuItem.getItemMeta();

    int currentModelData = chessMenuItemMeta.getCustomModelData();
    int baseModelData = currentModelData >= 100 ? currentModelData / 10 : currentModelData;

    int newModelData =
        drawItemType == DrawItemType.NONE
            ? baseModelData
            : baseModelData * 10 + drawItemType.getModelDataEnding();

    chessMenuItemMeta.setCustomModelData(newModelData);
    chessMenuItem.setItemMeta(chessMenuItemMeta);
    inventoryUpdater.updateLowerPart();
  }

  /**
   * Changes the surrender item texture to the texture of the given surrender item type.
   *
   * @param chessInventory the chess inventory of the player
   * @param inventoryUpdater the inventory updater for visual updates
   * @param highlight whether the surrender item should be highlighted or unhighlighted
   */
  public void highlightSurrenderItem(
      ChessInventoryUpdater inventoryUpdater, ChessInventory chessInventory, boolean highlight) {
    ItemStack chessMenuItem = getChessMenuItem(ChessMenuItemType.SURRENDER, chessInventory);
    ItemMeta chessMenuItemMeta = chessMenuItem.getItemMeta();

    int currentModelData = chessMenuItemMeta.getCustomModelData();
    int baseModelData = currentModelData >= 100 ? currentModelData / 10 : currentModelData;

    int newModelData = !highlight ? baseModelData : baseModelData * 10 + 1;

    chessMenuItemMeta.setCustomModelData(newModelData);
    chessMenuItem.setItemMeta(chessMenuItemMeta);
    inventoryUpdater.updateLowerPart();
  }

  /**
   * Sets the given message to the text of the specified chess menu item.
   *
   * @param message the message to display
   * @param menuItemType the chess menu item type
   * @param chessInventory the inventory to get the chess menu item from
   */
  public void setChessMenuItemMessage(
      String message, ChessMenuItemType menuItemType, ChessInventory chessInventory) {
    ItemStack menuItem = getChessMenuItem(menuItemType, chessInventory);
    ItemMeta menuItemMeta = menuItem.getItemMeta();
    menuItemMeta.setLore(getCutString(message));
    menuItem.setItemMeta(menuItemMeta);
    chessInventory.getChessInventoryUpdater().updateLowerPart();
  }

  /**
   * Cuts the message so it fits into the lore of an item.
   *
   * @param message the message to cut
   * @return the cut message returned as a list
   */
  private List<String> getCutString(String message) {
    List<String> finalText = new ArrayList<>();
    message = ChatColor.translateAlternateColorCodes('&', message);

    String color = ChatColor.GRAY.toString();
    StringBuilder textBuilder = new StringBuilder(color);
    int currentLength = 0;

    for (String word : message.split(" ")) {
      int wordLength = ChatColor.stripColor(word).length();

      if (currentLength + wordLength >= 26) {
        finalText.add(textBuilder.toString());
        color = ChatColor.getLastColors(textBuilder.toString());

        if (color.isEmpty()) {
          color = ChatColor.GRAY.toString();
        }

        textBuilder = new StringBuilder(color);
        currentLength = 0;
      }

      textBuilder.append(word).append(" ");
      currentLength += wordLength + 1;
    }

    if (currentLength > 0) {
      finalText.add(textBuilder.toString());
    }

    return finalText;
  }
}
