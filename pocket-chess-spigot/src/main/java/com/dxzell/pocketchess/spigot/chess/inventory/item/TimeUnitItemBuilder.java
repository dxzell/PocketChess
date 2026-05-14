package com.dxzell.pocketchess.spigot.chess.inventory.item;

import com.dxzell.pocketchess.spigot.PocketChess;
import com.dxzell.pocketchess.spigot.chess.inventory.ChessInventory;
import com.dxzell.pocketchess.spigot.chess.inventory.time.TimeUnit;
import com.dxzell.pocketchess.spigot.chess.inventory.time.TimeUnitTexture;
import com.dxzell.pocketchess.spigot.chess.inventory.time.TimeUnitTextureAmount;
import com.google.inject.Inject;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

/** Allows creation and highlighting of time unit items. */
public final class TimeUnitItemBuilder {

  private final PocketChess plugin;
  private static final Map<TimeUnit, String> TIME_KEYS =
      Map.of(TimeUnit.HOUR, "hour", TimeUnit.MINUTE, "minute", TimeUnit.SECOND, "second");

  @Inject
  public TimeUnitItemBuilder(PocketChess plugin) {
    this.plugin = plugin;
  }

  /**
   * @param timeUnit the requested time unit
   * @return the item representing the requested time unit
   */
  public ItemStack createTimeUnitItem(TimeUnit timeUnit, int amount) {
    TimeUnitTexture timeTexture = TimeUnitTexture.from(timeUnit);
    int modelData = timeTexture.getModelData();

    ItemStack timeUnitItem = new ItemStack(Material.PAPER);
    ItemMeta timeUnitMeta = timeUnitItem.getItemMeta();
    timeUnitMeta.setHideTooltip(true);
    if (amount == 0) {
      timeUnitItem.setAmount(1);
      modelData += 2;
    } else if (amount == 1) {
      timeUnitItem.setAmount(1);
      modelData += 1;
    } else {
      timeUnitItem.setAmount(amount);
    }
    timeUnitMeta.setCustomModelData(modelData);
    timeUnitMeta
        .getPersistentDataContainer()
        .set(getTimeUnitKey(timeUnit), PersistentDataType.BOOLEAN, true);
    timeUnitMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

    timeUnitItem.setItemMeta(timeUnitMeta);

    return timeUnitItem;
  }

  /**
   * @param chessInventory the players chess inventory
   * @param timeUnit the time unit of the requested item
   * @param ownTime whether the own time item or the other players time item should be returned
   * @return the item in the given chess inventory representing the requested time unit
   */
  public ItemStack getTimeUnitItem(
      ChessInventory chessInventory, TimeUnit timeUnit, boolean ownTime) {
    int slot = timeUnit == TimeUnit.HOUR ? 8 : timeUnit == TimeUnit.MINUTE ? 17 : 26;
    if (ownTime) slot += 27;
    return chessInventory.getUpperInv().getItem(slot);
  }

  /**
   * @param timeUnit the time unit
   * @return the corresponding NamespacedKey
   */
  private NamespacedKey getTimeUnitKey(TimeUnit timeUnit) {
    return new NamespacedKey(plugin, TIME_KEYS.get(timeUnit));
  }

  /**
   * Changes the texture of the time unit item.
   *
   * @param item the item to change texture
   * @param textureAmount what texture the item should be changed to
   * @param timeUnit the item unit of the given item
   */
  public void changeTextureAmount(
          ItemStack item, TimeUnitTextureAmount textureAmount, TimeUnit timeUnit, boolean isRunning) {
    int modelData = TimeUnitTexture.from(timeUnit).getModelData();
    int newModelData =
        modelData
            + (textureAmount == TimeUnitTextureAmount.ONE
                ? 1
                : textureAmount == TimeUnitTextureAmount.ZERO ? 2 : 0);
    newModelData = Integer.parseInt(newModelData + (isRunning ? "1" : ""));

    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setCustomModelData(newModelData);
    item.setItemMeta(itemMeta);
  }

  /**
   * Unhighlight the time unit item.
   *
   * @param inventory the inventory to unhighlight the time unit items in
   * @param ownTime whether to unhighlight the own or the other players time unit items
   */
  public void unhighlightTimeUnits(ChessInventory inventory, boolean ownTime) {
    List<ItemStack> timeUnitItems =
        List.of(
            getTimeUnitItem(inventory, TimeUnit.HOUR, ownTime),
            getTimeUnitItem(inventory, TimeUnit.MINUTE, ownTime),
            getTimeUnitItem(inventory, TimeUnit.SECOND, ownTime));
    timeUnitItems.forEach(
        timeUnitItem -> {
          ItemMeta timeUnitItemMeta = timeUnitItem.getItemMeta();
          String modelDataString = timeUnitItemMeta.getCustomModelData() + "";
          timeUnitItemMeta.setCustomModelData(
              Integer.parseInt(modelDataString.substring(0, modelDataString.length() - 1)));
          timeUnitItem.setItemMeta(timeUnitItemMeta);
        });
  }
}
