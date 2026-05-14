package com.dxzell.pocketchess.spigot.chess.inventory.item;

import lombok.Getter;

/**
 * Represents a possible draw item type.
 */
@Getter
public enum DrawItemType {
    NONE(0),
    CONFIRM(1),
    ACCEPT(2),
    SENT(3);

    private final int modelDataEnding;

    DrawItemType(int modelDataEnding) {
        this.modelDataEnding = modelDataEnding;
    }
}
