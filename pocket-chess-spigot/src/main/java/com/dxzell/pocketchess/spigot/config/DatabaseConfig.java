package com.dxzell.pocketchess.spigot.config;

import com.dxzell.pocketchess.spigot.PocketChess;
import com.google.inject.Inject;

/**
 * Provides access to database information.
 */
public final class DatabaseConfig extends Config {

    @Inject
    public DatabaseConfig(PocketChess plugin) {
        super(plugin, "database.yml");
    }

    public int getPort() {
        return config.getInt("port");
    }

    public String getPassword() {
        return config.getString("password");
    }

    public String getUsername() {
        return config.getString("username");
    }

    public String getDatabase() {
        return config.getString("database");
    }

    public String getHost() {
        return config.getString("host");
    }
}
