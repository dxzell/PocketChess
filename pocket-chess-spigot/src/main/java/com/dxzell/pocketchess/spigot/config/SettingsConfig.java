package com.dxzell.pocketchess.spigot.config;

import com.dxzell.pocketchess.spigot.PocketChess;
import com.dxzell.pocketchess.spigot.chess.game.ChessGameEvent;
import com.google.inject.Inject;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;

import javax.annotation.Nullable;

/** Provides access to values of specific settings. */
public class SettingsConfig extends Config {

  @Inject
  public SettingsConfig(PocketChess plugin) {
    super(plugin, "settings.yml");
  }

  @Nullable
  public Sound getSound(ChessGameEvent gameEvent) {
    String path = "chess-game.sounds." + gameEvent.name().toLowerCase();

    String soundName = config.getString(path);

    if (soundName == null || soundName.isBlank()) {
      return null;
    }

    if (soundName.equalsIgnoreCase("no.sound")) {
      return null;
    }

    NamespacedKey key = NamespacedKey.fromString(soundName);
    if (key == null) {
      return null;
    }

    return Registry.SOUNDS.get(key);
  }

  public long getDrawOfferCooldownMillis() {
    return config.getInt(getDrawSettingsPath() + "offer-cooldown-in-seconds") * 1000L;
  }

  public long getDrawOfferExpiresInMillis() {
    return config.getInt(getDrawSettingsPath() + "offer-expires-in-seconds") * 1000L;
  }

  public long getSurrenderConfirmationExpiresInMillis() {
    return config.getInt(getSurrenderSettingsPath() + "offer-expires-in-seconds") * 1000L;
  }

  public long getDuelRequestExpiresInMillis() {
    return config.getInt(getDuelRequestPath() + "duel-request-expires-in-seconds") * 1000L;
  }

  private String getDrawSettingsPath() {
    return "chess-game.draw.";
  }

  private String getDuelRequestPath() {
    return "duel-request.";
  }

  private String getSurrenderSettingsPath() {
    return "chess-game.surrender.";
  }
}
