package com.dxzell.pocketchess.spigot.chess.inventory.time;

/**
 * For each time unit texture there are three different textures. ZERO and ONE add the number in the texture because it will not show in the inventory otherwise. So STANDARD will be used when the amount of that time unit is above 1.
 */
public enum TimeUnitTextureAmount {
    STANDARD,
    ZERO,
    ONE
}
