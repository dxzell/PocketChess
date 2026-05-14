package com.dxzell.pocketchess.spigot.chess.inventory;

/**
 * Allows distinction between both inventories that form the chess board inventory.
 *
 * @param slot the slot in the specific inventory
 * @param part either lower or upper inventory
 */
public record ChessInventorySlot(int slot, ChessInventoryPart part) {}
