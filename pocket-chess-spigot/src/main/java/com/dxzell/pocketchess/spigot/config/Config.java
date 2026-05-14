package com.dxzell.pocketchess.spigot.config;

import com.dxzell.pocketchess.spigot.PocketChess;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/** Provides shared functionality for Config classes. */
public abstract class Config {

  protected final PocketChess plugin;
  protected File file;
  protected YamlConfiguration config;

  protected Config(PocketChess plugin, String configName) {
    this.plugin = plugin;
    this.load(configName);
  }

  /** Attempts to save changes made to the YML file values. */
  protected void save() {
    try {
      config.save(file);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Loads the YML file and creates it if it does not already exist.
   *
   * @param configName the name of the YML file to load
   */
  private void load(String configName) {
    file = new File(plugin.getDataFolder(), configName);

    if (!file.exists()) {
      plugin.saveResource(configName, true);
    }

    config = new YamlConfiguration();
    config.options().parseComments(true);

    try {
      config.load(file);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    updateConfig(configName);
  }

  /**
   * Adds missing keys from the default YML file to the currently saved YML file.
   *
   * @param configName the name of the YML file to update
   */
  private void updateConfig(String configName) {
    try (Reader defConfigStream = new InputStreamReader(plugin.getResource(configName))) {
      if (defConfigStream != null) {
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);

        boolean changed = false;
        for (String key : defConfig.getKeys(true)) {
          if (!config.contains(key)) {
            config.set(key, defConfig.get(key));
            changed = true;
          }
        }

        if (changed) {
          save();
        }
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  protected String getColoredString(String path) {
    String text = config.getString(path);
    return ChatColor.translateAlternateColorCodes(
        '&', text != null ? text : ChatColor.RED + "Path " + path + " not found.");
  }
}
