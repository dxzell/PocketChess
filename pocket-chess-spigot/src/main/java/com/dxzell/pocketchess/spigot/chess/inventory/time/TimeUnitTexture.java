package com.dxzell.pocketchess.spigot.chess.inventory.time;

import lombok.Getter;

/** Represents the texture model data for time units. */
@Getter
public enum TimeUnitTexture {
  HOUR(3),
  MINUTE(6),
  SECOND(9);

  private final int modelData;

  TimeUnitTexture(int modelData) {
    this.modelData = modelData;
  }

  public static TimeUnitTexture from(TimeUnit timeUnit) {
    return switch (timeUnit) {
      case HOUR -> TimeUnitTexture.HOUR;
      case MINUTE -> TimeUnitTexture.MINUTE;
      case SECOND -> TimeUnitTexture.SECOND;
    };
  }
}
