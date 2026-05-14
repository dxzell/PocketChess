package com.dxzell.pocketchess.spigot.chess.inventory.item;

import lombok.Getter;

/** Represents a chess menu item type. */
@Getter
public enum ChessMenuItemType {
  INFO(12),
  SURRENDER(13),
  DRAW(14);

  private final int modelData;

  ChessMenuItemType(int modelData) {
    this.modelData = modelData;
  }

}
